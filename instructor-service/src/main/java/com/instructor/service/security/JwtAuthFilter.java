package com.instructor.service.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            // Allow unauthenticated access only to actuator; block others under /instructors
            if (path.startsWith("/actuator")) {
                filterChain.doFilter(request, response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        try {
            String token = header.substring(7);
            Claims claims = jwtUtil.parseClaims(token);
            String role = claims.get("role", String.class);
            String subject = claims.getSubject();

            // Enforce instructor-only access for these APIs
            if (!"INSTRUCTOR".equalsIgnoreCase(role) && path.startsWith("/instructors")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access denied: INSTRUCTOR role required");
                return;
            }

            var auth = new UsernamePasswordAuthenticationToken(subject, null,
                    role != null ? List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())) : List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token: " + ex.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
