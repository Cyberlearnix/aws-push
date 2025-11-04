package com.instructor.service.repository;

import com.instructor.service.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    /**
     * Find instructor by email
     */
    Optional<Instructor> findByEmail(String email);

    /**
     * Find instructor by user ID
     */
    @Query("SELECT i FROM Instructor i WHERE i.userId = :userId")
    Optional<Instructor> findByUserUuid(@Param("userId") UUID userId);

    /**
     * Check if instructor exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Check if instructor exists by user ID
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Instructor i WHERE i.userId = :userId")
    boolean existsByUserUuid(@Param("userId") UUID userId);

    /**
     * Find all active instructors
     */
    List<Instructor> findByActiveTrue();
    /**
     * Check if instructor exists by user ID
     */
    boolean existsByUserId(UUID userId);
}