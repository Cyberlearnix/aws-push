package com.instructor.service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SecurityContextDebugFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) 
            throws ServletException, IOException {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("SecurityContextDebugFilter - Before chain - Auth: {}", 
            auth != null ? auth.getName() + " (" + auth.getAuthorities() + ")" : "null");
            
        try {
            filterChain.doFilter(request, response);
        } finally {
            auth = SecurityContextHolder.getContext().getAuthentication();
            log.debug("SecurityContextDebugFilter - After chain - Auth: {}", 
                auth != null ? auth.getName() + " (" + auth.getAuthorities() + ")" : "null");
        }
    }
}
