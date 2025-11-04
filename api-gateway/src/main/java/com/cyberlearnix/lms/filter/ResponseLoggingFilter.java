package com.cyberlearnix.lms.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ResponseLoggingFilter {
    
    private static final Logger log = LoggerFactory.getLogger(ResponseLoggingFilter.class);
    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory;
    
    public ResponseLoggingFilter(ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory) {
        this.modifyResponseBodyFilterFactory = modifyResponseBodyFilterFactory;
    }
    
    public GatewayFilter apply() {
        return modifyResponseBodyFilterFactory.apply(new ModifyResponseBodyGatewayFilterFactory.Config()
            .setInClass(String.class)
            .setOutClass(String.class)
            .setRewriteFunction(String.class, String.class, (exchange, body) -> {
                logResponse(exchange, body);
                return Mono.justOrEmpty(body);
            }));
    }
    
    public String logResponse(ServerWebExchange exchange, String body) {
        log.info("\n=== Response ===\n" +
                "Status: {}\n" +
                "Headers: {}\n" +
                "Body: {}",
                exchange.getResponse().getStatusCode(),
                exchange.getResponse().getHeaders(),
                body
        );
        return body;
    }
}
