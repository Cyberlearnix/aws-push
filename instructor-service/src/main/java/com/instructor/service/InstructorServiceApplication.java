package com.instructor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@EnableAsync
@SpringBootApplication(scanBasePackages = {
    "com.instructor.service",
    "com.instructor.service.controller",
    "com.instructor.service.service"
})
@EnableFeignClients
@EnableCaching
@EnableRetry
@RestController
public class InstructorServiceApplication {

    @GetMapping("/test")
    public String test() {
        return "Test endpoint is working! Application is running.";
    }
    
    @GetMapping("/test/course")
    public String testCourseEndpoint() {
        return "Course test endpoint is accessible!";
    }

    @GetMapping("/mappings")
    public List<String> listAllMappings(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        return requestMappingHandlerMapping.getHandlerMethods().keySet().stream()
                .map(mapping -> {
                    String methods = mapping.getMethodsCondition().getMethods().stream()
                            .map(Enum::name)
                            .collect(Collectors.joining(", "));
                    return String.format("%s %s", methods, mapping.getPatternsCondition().getPatterns());
                })
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        var ctx = SpringApplication.run(InstructorServiceApplication.class, args);
        log.info("\n===== Registered Controllers =====");
        String[] beanNames = ctx.getBeanNamesForAnnotation(RestController.class);
        for (String beanName : beanNames) {
            log.info("Found controller: {}", beanName);
        }
        log.info("================================");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> onApplicationReady(FilterChainProxy filterChainProxy) {
        return event -> {
            log.info("\n===== Security Filter Chains =====");
            List<SecurityFilterChain> filterChains = filterChainProxy.getFilterChains();
            for (int i = 0; i < filterChains.size(); i++) {
                log.info("\nFilter Chain {}:", i + 1);
                filterChains.get(i).getFilters().forEach(filter -> 
                    log.info("- {}", filter.getClass().getName())
                );
            }
            log.info("================================\n");
        };
    }
}
