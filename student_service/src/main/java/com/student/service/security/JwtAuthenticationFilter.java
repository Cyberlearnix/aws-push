package com.student.service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // For testing purposes
    public JwtUtil getJwtUtil() {
        return this.jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        // Quick temporary debug: log incoming Authorization header
        String auth = request.getHeader("Authorization");
        logger.debug("Incoming Authorization header for {} {}: {}", method, requestPath, auth);

        try {
            // Skip authentication for public endpoints
            if (isPublicEndpoint(requestPath, method)) {
                logger.debug("Skipping authentication for public {} {}", method, requestPath);
                filterChain.doFilter(request, response);
                return;
            }

            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                logger.warn("No Bearer token found for {} {}", method, requestPath);
                filterChain.doFilter(request, response);
                return;
            }

            String token = header.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token);

                    logger.info("Authenticated user: {} with role: {} for {} {}", 
                        username, role, method, requestPath);

                    // Create authorities based on role
                    List<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_" + role));
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Successfully set authentication for user: {}", username);
                } else {
                    logger.warn("Invalid JWT token for {} {}", method, requestPath);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                    return;
                }
            } catch (JwtException ex) {
                logger.debug("Invalid JWT: {}", ex.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
                return;
            }
        } catch (Exception e) {
            logger.error("Error processing JWT token", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred during authentication");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestPath, String method) {
        // Only allow specific public endpoints
        return requestPath.startsWith("/api/students/public") ||
               requestPath.startsWith("/api/auth/") ||
               requestPath.startsWith("/api/courses/public") ||
               requestPath.startsWith("/actuator/") ||
               requestPath.startsWith("/api/students/certificates/verify/") ||
               requestPath.equals("/api/students/login");
    }


    private boolean hasRequiredRole(String role) {
        // Allow access for STUDENT, INSTRUCTOR, and ADMIN roles
        return "STUDENT".equals(role) || "INSTRUCTOR".equals(role) || "ADMIN".equals(role);
    }
}








