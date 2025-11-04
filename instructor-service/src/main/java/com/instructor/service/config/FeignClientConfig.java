package com.instructor.service.config;

import feign.*;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Logs request/response headers, body, and metadata
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return template -> {
            try {
                // Get the current request attributes
                ServletRequestAttributes attributes = (ServletRequestAttributes) 
                    RequestContextHolder.getRequestAttributes();
                
                if (attributes == null) {
                    log.warn("No request attributes available to extract Authorization header");
                    return;
                }
                
                // Get the Authorization header from the current request
                String authorizationHeader = attributes.getRequest().getHeader("Authorization");
                
                if (StringUtils.isBlank(authorizationHeader)) {
                    log.warn("No Authorization header found in the request");
                    return;
                }
                
                // Log the first 20 characters of the token for debugging
                log.debug("Forwarding Authorization header to user-service (first 20 chars): {}", 
                    authorizationHeader.length() > 20 ? authorizationHeader.substring(0, 20) + "..." : authorizationHeader);
                
                // Forward the exact Authorization header
                template.header("Authorization", authorizationHeader);
                
                // Log the request details for debugging
                logRequest(template);
                
            } catch (Exception e) {
                log.error("Error while setting Authorization header in Feign client: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to set Authorization header", e);
            }
        };
    }

    private void logRequest(RequestTemplate template) {
        if (log.isDebugEnabled()) {
            log.debug("Feign Request: {} {}", template.method(), template.url());
            log.debug("Headers: {}", template.headers());
            if (template.body() != null) {
                log.debug("Request Body: {}", new String(template.body(), StandardCharsets.UTF_8));
            }
        }
    }

    public static class FeignErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, Response response) {
            try {
                String requestUrl = response.request().url();
                String responseBody = response.body() != null ? 
                    Util.toString(response.body().asReader(StandardCharsets.UTF_8)) : "";
                
                log.error("Feign Client Error - Method: {}, Status: {}, URL: {}, Response: {}",
                        methodKey, response.status(), requestUrl, responseBody);

                // Handle specific status codes
                if (response.status() == HttpStatus.UNAUTHORIZED.value()) {
                    return new FeignException.Unauthorized(
                        "Authentication failed for " + methodKey,
                        response.request(),
                        responseBody.getBytes(StandardCharsets.UTF_8),
                        response.headers()
                    );
                } else if (response.status() == HttpStatus.NOT_FOUND.value()) {
                    return new FeignException.NotFound(
                        "Resource not found: " + methodKey,
                        response.request(),
                        responseBody.getBytes(StandardCharsets.UTF_8),
                        response.headers()
                    );
                }
                
                // For other errors, use the default decoder
                return defaultErrorDecoder.decode(methodKey, response);
                
            } catch (IOException e) {
                log.error("Error while decoding Feign error response: {}", e.getMessage());
                return new FeignException.InternalServerError(
                    "Error while decoding error response",
                    response.request(),
                    (byte[]) null,
                    response.headers()
                );
            }
        }
    }
}
