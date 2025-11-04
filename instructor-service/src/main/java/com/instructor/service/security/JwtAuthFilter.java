package com.instructor.service.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    private String parseJwt(String header) {
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private boolean isPublicEndpoint(String path, String method) {
        // Add your public endpoints here
        return (
            // Swagger/OpenAPI endpoints
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/swagger-ui/") ||
            path.startsWith("/swagger-ui.html") ||
            path.startsWith("/webjars/") ||
            
            // Actuator endpoints
            path.startsWith("/actuator") ||
            
            // Public API endpoints (if any)
            ("GET".equals(method) && path.equals("/api/health")) ||
            ("POST".equals(method) && path.equals("/api/auth/login"))
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) 
            throws ServletException, IOException {
        
        final String path = request.getRequestURI();
        final String authHeader = request.getHeader("Authorization");
        final String method = request.getMethod();
        
        log.debug("[JwtAuthFilter] {} {} | Authorization: {}", 
            method, 
            path, 
            authHeader == null ? "MISSING" : "present (len=" + authHeader.length() + ")");
            
        // Skip filter for public endpoints
        if (isPublicEndpoint(path, method)) {
            log.debug("Skipping JWT validation for public endpoint: {} {}", method, path);
            filterChain.doFilter(request, response);
            return;
        }

        // Check for Authorization header
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            sendErrorResponse(response, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            return;
        }

        try {
            String jwt = parseJwt(authHeader);
            log.debug("Extracted JWT token (first 10 chars): {}", 
                    jwt != null && jwt.length() > 10 ? jwt.substring(0, 10) + "..." : "[invalid]");
            
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                try {
                    // Extract username from token
                    String username = jwtUtil.extractUsername(jwt);
                    log.debug("Extracted username from JWT: {}", username);
                    
                    // Store the token in the request attributes for use in other components
                    request.setAttribute("token", jwt);
                    
                    // Create authentication object with the token as credentials
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            username, 
                            jwt,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_INSTRUCTOR"))
                        );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set the authentication in the security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Successfully authenticated user: {}", username);
                    
                    // Continue with the filter chain
                    filterChain.doFilter(request, response);
                    return;
                    
                } catch (Exception e) {
                    log.error("Error processing JWT token: {}", e.getMessage(), e);
                    SecurityContextHolder.clearContext();
                    sendErrorResponse(response, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                    return;
                }
            } else {
                log.warn("Invalid or missing JWT token");
                sendErrorResponse(response, "Invalid or missing JWT token", HttpStatus.UNAUTHORIZED);
                return;
            }
            
        } catch (Exception ex) {
            log.error("JWT processing failed: {}", ex.getMessage(), ex);
            sendErrorResponse(response, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            return;
        }
    }
    
    private boolean isPublicPath(String path) {
        // Only allow specific public endpoints
        return path.startsWith("/actuator/health") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/swagger-ui.html") ||
               path.equals("/error") ||
               path.equals("/favicon.ico") ||
               path.matches("/v3/api-docs.*") ||
               path.startsWith("/webjars/") ||
               path.startsWith("/swagger-resources");
    }
    
    private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        log.warn("Sending error response - Status: {}, Message: {}", status, message);
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String jsonResponse = String.format(
            "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
            status.value(),
            status.getReasonPhrase(),
            message
        );
        response.getWriter().write(jsonResponse);
    }
}
