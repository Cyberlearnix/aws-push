package com.cyberlearnix.lms.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final String TOKEN_PREFIX = "Bearer ";
    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = resolveToken(exchange.getRequest());
        if (StringUtils.hasText(token)) {
            System.out.println("JWT Token found in request");
            if (tokenProvider.validateToken(token)) {
                System.out.println("JWT Token is valid");
                try {
                    Authentication authentication = getAuthentication(token);
                    System.out.println("Authentication object created: " + authentication);
                    System.out.println("Authorities: " + authentication.getAuthorities());
                    
                    SecurityContext context = new SecurityContextImpl(authentication);
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
                } catch (Exception e) {
                    System.err.println("Error during authentication: " + e.getMessage());
                    e.printStackTrace();
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            } else {
                System.err.println("Invalid JWT Token");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        } else {
            System.out.println("No JWT Token found in request");
        }
        return chain.filter(exchange);
    }

    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    private Authentication getAuthentication(String token) {
        String username = tokenProvider.getUsernameFromToken(token);
        List<String> roles = tokenProvider.getRolesFromToken(token);
        
        List<SimpleGrantedAuthority> authorities = (roles != null ? roles.stream()
                .map(role -> {
                    // Handle both formats: with or without ROLE_ prefix
                    String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    return new SimpleGrantedAuthority(roleName);
                })
                .collect(Collectors.toList()) : Collections.emptyList());

        System.out.println("Authenticated user: " + username + " with roles: " + authorities);

        return new UsernamePasswordAuthenticationToken(
                username, 
                null, 
                authorities
        );
    }
}
