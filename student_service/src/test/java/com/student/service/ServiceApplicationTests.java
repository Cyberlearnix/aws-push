package com.student.service;

import com.student.service.config.TestDatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDatabaseConfig.class)
class ServiceApplicationTests {

    @Test
    void contextLoads() {
        // Test will pass if the application context loads successfully
    }
}
