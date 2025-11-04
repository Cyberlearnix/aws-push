package com.cyberlearnix.lms.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class RequestLoggingFilter extends AbstractGatewayFilterFactory<RequestLoggingFilter.Config> {
    
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    public RequestLoggingFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Log request
            ServerHttpRequest request = exchange.getRequest();
            
            log.info("\n=== Request ===\n" +
                    "Method: {}\n" +
                    "Path: {}\n" +
                    "Headers: {}\n" +
                    "Query Params: {}",
                    request.getMethod(),
                    request.getPath(),
                    request.getHeaders(),
                    request.getQueryParams()
            );
            
            // Log request body if present
            if (config.includePayload && request.getHeaders().getContentLength() > 0) {
                return exchange.getRequest().getBody()
                    .collectList()
                    .flatMap(dataBuffers -> {
                        byte[] bytes = new byte[dataBuffers.stream().mapToInt(d -> d.readableByteCount()).sum()];
                        int i = 0;
                        for (var buffer : dataBuffers) {
                            int length = buffer.readableByteCount();
                            buffer.read(bytes, i, length);
                            i += length;
                        }
                        String body = new String(bytes);
                        log.info("Request Body: {}", body);
                        return chain.filter(exchange);
                    });
            }
            
            return chain.filter(exchange);
        };
    }
    
    public static class Config {
        private String baseMessage;
        private boolean includeQueryParams;
        private boolean includeClientInfo;
        private boolean includeHeaders;
        private boolean includePayload;
        
        public String getBaseMessage() {
            return baseMessage;
        }
        
        public void setBaseMessage(String baseMessage) {
            this.baseMessage = baseMessage;
        }
        
        public boolean isIncludeQueryParams() {
            return includeQueryParams;
        }
        
        public void setIncludeQueryParams(boolean includeQueryParams) {
            this.includeQueryParams = includeQueryParams;
        }
        
        public boolean isIncludeClientInfo() {
            return includeClientInfo;
        }
        
        public void setIncludeClientInfo(boolean includeClientInfo) {
            this.includeClientInfo = includeClientInfo;
        }
        
        public boolean isIncludeHeaders() {
            return includeHeaders;
        }
        
        public void setIncludeHeaders(boolean includeHeaders) {
            this.includeHeaders = includeHeaders;
        }
        
        public boolean isIncludePayload() {
            return includePayload;
        }
        
        public void setIncludePayload(boolean includePayload) {
            this.includePayload = includePayload;
        }
    }
}
