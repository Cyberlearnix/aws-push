package com.student.service.security;

import com.student.service.entity.Student;
import com.student.service.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UUID studentId = UUID.fromString(username);
        Student student = studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(student.getId().toString())
                .password(student.getPassword())
                .authorities(student.getRole().name())
                .accountExpired(false)
                .accountLocked(student.getStatus() != Student.AccountStatus.ACTIVE)
                .credentialsExpired(false)
                .disabled(!student.isActive())
                .build();
    }
}
