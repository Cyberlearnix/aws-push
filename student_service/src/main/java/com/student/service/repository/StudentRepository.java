package com.student.service.repository;

import com.student.service.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    
    Optional<Student> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT s FROM Student s WHERE s.email = :email AND s.isActive = true")
    Optional<Student> findActiveByEmail(@Param("email") String email);
    
    @Query("SELECT s FROM Student s WHERE s.id = :id AND s.isActive = true")
    Optional<Student> findActiveById(@Param("id") UUID id);
}








