package com.instructor.service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "CyberLearnIX Instructor Service API",
        version = "1.0",
        description = "REST API for Instructor Management in CyberLearnIX LMS",
        contact = @Contact(
            name = "CyberLearnIX Support",
            email = "support@cyberlearnix.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://choosealicense.com/licenses/mit/"
        )
    ),
    servers = @Server(
        url = "http://localhost:8083",
        description = "Development server"
    )
)
public class SwaggerConfig {
    // SpringDoc will auto-configure the OpenAPI documentation
}
