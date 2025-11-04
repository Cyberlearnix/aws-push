package com.instructor.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.http.HttpStatus;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true
)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final SecurityContextDebugFilter securityContextDebugFilter;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
            // Swagger UI v3 (OpenAPI)
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/configuration/ui",
            "/configuration/security",
            "/v2/api-docs",
            "/v3/api-docs.yaml",
            "/swagger-resources",
            "/swagger-ui/index.html",
            "/swagger-ui.html",
            "/swagger-ui/index.html/**",
            "/swagger-ui.html/**",
            "/swagger-ui/**",
            "/v3/api-docs/swagger-config",
            "/v3/api-docs/swagger-config/**",
            "/favicon.ico",
            "/error",
            "/actuator/health",
            "/actuator/info"
        );
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public JwtAuthFilter authenticationJwtTokenFilter() {
        return new JwtAuthFilter(jwtUtil, userDetailsService);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");
        
        http
            // Disable CSRF as we're using JWT
            .csrf(AbstractHttpConfigurer::disable)
            
            // Set session management to stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                corsConfig.setAllowedOrigins(java.util.List.of("*"));
                corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                corsConfig.setAllowedHeaders(java.util.List.of("*"));
                corsConfig.setAllowCredentials(true);
                corsConfig.setMaxAge(3600L);
                corsConfig.addAllowedHeader("Content-Type");
                corsConfig.addAllowedHeader("Authorization");
                corsConfig.addAllowedHeader("X-Requested-With");
                corsConfig.addAllowedHeader("Accept");
                corsConfig.addAllowedHeader("Origin");
                return corsConfig;
            }))
            
            // Configure request authorization
            .authorizeHttpRequests(auth -> {
                // Public endpoints (no authentication required)
                auth.requestMatchers(
                    "/actuator/health",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/webjars/**",
                    "/swagger-resources/**"
                ).permitAll();
                
                // Secure all other endpoints
                auth.anyRequest().authenticated();
            })
            
            // Add our custom filters
            // Add our custom filters
            .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(securityContextDebugFilter, JwtAuthFilter.class)
            
            // Configure exception handling
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    log.warn("Authentication error: {}", authException.getMessage());
                    response.setContentType("application/json");
                    response.setStatus(org.springframework.http.HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().write(
                        String.format("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\"}", 
                        authException.getMessage())
                    );
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.warn("Access denied: {}", accessDeniedException.getMessage());
                    response.setContentType("application/json");
                    response.setStatus(org.springframework.http.HttpStatus.FORBIDDEN.value());
                    response.getWriter().write(
                        String.format("{\"status\": 403, \"error\": \"Forbidden\", \"message\": \"%s\"}", 
                        accessDeniedException.getMessage())
                    );
                })
            );
            
        // Log the security configuration
        log.info("Security configuration applied");
        
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
