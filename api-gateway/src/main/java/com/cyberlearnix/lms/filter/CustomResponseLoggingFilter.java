package com.cyberlearnix.lms.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class CustomResponseLoggingFilter extends AbstractGatewayFilterFactory<CustomResponseLoggingFilter.Config> {
    
    private static final Logger log = LoggerFactory.getLogger(CustomResponseLoggingFilter.class);
    
    public CustomResponseLoggingFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Log request details
            ServerHttpRequest request = exchange.getRequest();
            log.info("\n=== Request ===\n" +
                    "Method: {}\n" +
                    "Path: {}\n" +
                    "Query Params: {}\n" +
                    "Headers: {}",
                    request.getMethod(),
                    request.getPath(),
                    request.getQueryParams(),
                    request.getHeaders());
            
            // Log response details
            ServerHttpResponse response = exchange.getResponse();
            
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                log.info("\n=== Response ===\n" +
                        "Status: {}\n" +
                        "Headers: {}",
                        response.getStatusCode(),
                        response.getHeaders());
            }));
        };
    }
    
    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.emptyList();
    }
    
    public static class Config {
        // Add configuration properties here if needed
    }
}
