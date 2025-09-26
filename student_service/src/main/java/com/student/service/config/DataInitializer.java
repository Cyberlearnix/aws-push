package com.student.service.config;

import com.student.service.entity.Student;
import com.student.service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        if (studentRepository.count() == 0) {
            createSampleStudents();
        }
    }
    
    private void createSampleStudents() {
        log.info("Creating sample students...");
        
        Student student1 = new Student();
        student1.setEmail("student1@example.com");
        student1.setFirstName("John");
        student1.setLastName("Doe");
        student1.setPassword(passwordEncoder.encode("password123"));
        student1.setRole(Student.Role.STUDENT);
        student1.setActive(true);
        studentRepository.save(student1);
        
        Student student2 = new Student();
        student2.setEmail("student2@example.com");
        student2.setFirstName("Jane");
        student2.setLastName("Smith");
        student2.setPassword(passwordEncoder.encode("password123"));
        student2.setRole(Student.Role.STUDENT);
        student2.setActive(true);
        studentRepository.save(student2);
        
        Student instructor = new Student();
        instructor.setEmail("instructor@example.com");
        instructor.setFirstName("Dr. Sarah");
        instructor.setLastName("Johnson");
        instructor.setPassword(passwordEncoder.encode("password123"));
        instructor.setRole(Student.Role.INSTRUCTOR);
        instructor.setActive(true);
        studentRepository.save(instructor);
        
        Student admin = new Student();
        admin.setEmail("admin@example.com");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setRole(Student.Role.ADMIN);
        admin.setActive(true);
        studentRepository.save(admin);
        
        log.info("Sample students created successfully");
    }
}


