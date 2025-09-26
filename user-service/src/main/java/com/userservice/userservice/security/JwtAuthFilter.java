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

        System.out.println("🔍 JWT Filter - Processing: " + request.getRequestURI());
        System.out.println("🔍 JWT Filter - Auth Header: " +
                (authHeader != null ? authHeader.substring(0, Math.min(30, authHeader.length())) + "..." : "null"));

        // No token → continue (let security rules decide access)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("🔍 JWT Filter - No valid Bearer token, continuing...");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token string after "Bearer "
        final String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            System.out.println("🔍 JWT Filter - Empty token after Bearer, continuing...");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token); // e.g., "ADMIN", "STUDENT", etc.
            System.out.println("🔍 JWT Filter - Extracted email: " + email + ", role: " + role);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("✅ JWT Filter - Authentication set successfully");
            } else {
                System.out.println("🔍 JWT Filter - Email null or already authenticated");
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("⚠️ JWT Filter - Token expired: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token expired\"}");
            return;
        } catch (Exception e) {
            System.out.println("❌ JWT Filter - Token validation error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
