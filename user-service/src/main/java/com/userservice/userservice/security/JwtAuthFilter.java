package com.userservice.userservice.security;

import com.userservice.userservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // Skip JWT checks entirely on public routes
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true; // preflight
        return p.startsWith("/api/email-auth/")
                || p.startsWith("/api/auth/login-password")
                || p.startsWith("/api/auth/refresh-token")
                || p.startsWith("/api/auth/logout")
                || p.startsWith("/api/users/forgot-password")
                || p.startsWith("/api/users/reset-password")
                || p.matches("/api/users/\\w+-\\w+-\\w+-\\w+-\\w+") // UUID pattern
                || p.startsWith("/v3/api-docs/")
                || p.startsWith("/swagger-ui/")
                || "/swagger-ui.html".equals(p)
                || p.startsWith("/webjars/")
                || "/error".equals(p);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String requestId = java.util.UUID.randomUUID().toString().substring(0, 8);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String remoteAddr = request.getRemoteAddr();

        System.out.println(String.format("[%s] 🔍 JWT Filter - Processing %s %s from %s", requestId, method, uri, remoteAddr));
        System.out.println(String.format("[%s] 🔍 JWT Filter - Auth Header: %s", requestId,
                (authHeader != null ? authHeader.substring(0, Math.min(30, authHeader.length())) + "..." : "null")));

        // No token → continue (let security rules decide access)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println(String.format("[%s] 🔍 JWT Filter - No valid Bearer token, continuing to security chain...", requestId));
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token string after "Bearer "
        final String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            System.out.println(String.format("[%s] 🔍 JWT Filter - Empty token after Bearer, continuing...", requestId));
            filterChain.doFilter(request, response);
            return;
        }
        
        System.out.println(String.format("[%s] 🔍 JWT Filter - Extracted token (length: %d)", requestId, token.length()));

        try {
            System.out.println(String.format("[%s] 🔍 JWT Filter - Parsing JWT token claims...", requestId));
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token); // e.g., "ADMIN", "STUDENT", etc.
            System.out.println(String.format("[%s] 🔍 JWT Filter - Extracted email: %s, role: %s", requestId, maskEmail(email), role));

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println(String.format("[%s] 🔍 JWT Filter - Setting Spring Security authentication...", requestId));
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                System.out.println(String.format("[%s] 🔍 JWT Filter - Granted authorities: %s", requestId, authorities));
                
                var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                System.out.println(String.format("[%s] ✅ JWT Filter - Authentication set successfully for user: %s with role: ROLE_%s", requestId, maskEmail(email), role));
            } else {
                System.out.println(String.format("[%s] 🔍 JWT Filter - Email null or already authenticated (email: %s, auth: %s)", 
                    requestId, email, SecurityContextHolder.getContext().getAuthentication() != null ? "present" : "null"));
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println(String.format("[%s] ⚠️ JWT Filter - Token expired: %s", requestId, e.getMessage()));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token expired\"}");
            return;
        } catch (Exception e) {
            System.out.println(String.format("[%s] ❌ JWT Filter - Token validation error: %s", requestId, e.getMessage()));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid token\"}");
            return;
        }

        System.out.println(String.format("[%s] 🔍 JWT Filter - Continuing to next filter in chain", requestId));
        filterChain.doFilter(request, response);
    }
    
    private String maskEmail(String email) {
        if (email == null || email.length() <= 3) return email;
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return email;
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
