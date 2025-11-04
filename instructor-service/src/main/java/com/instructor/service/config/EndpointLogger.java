package com.instructor.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class EndpointLogger {
    private static final Logger log = LoggerFactory.getLogger(EndpointLogger.class);

    @Bean
    public CommandLineRunner logEndpoints(RequestMappingHandlerMapping mapping) {
        return args -> {
            log.info("\n\n===== Registered Endpoints =====");
            mapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) -> {
                String httpMethods = requestMappingInfo.getMethodsCondition().getMethods().toString();
                String patterns = requestMappingInfo.getPatternValues().toString();
                log.info("{} {} -> {}", httpMethods, patterns, handlerMethod);
            });
            log.info("==============================\n\n");
        };
    }
}
