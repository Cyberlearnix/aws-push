package com.cyberlearnix.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @RequestMapping("/fallback/fallback")
    public Mono<String> fallback() {
        return Mono.just("Service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/fallback/user-fallback")
    public Mono<String> userFallback() {
        return Mono.just("User service is currently unavailable. Please try again later.");
    }
}
