package com.instructor.service.repository;

import com.instructor.service.entity.CourseEntity;
import com.instructor.service.entity.InstructorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    /**
     * Find courses by instructor
     */
    List<CourseEntity> findByInstructor(InstructorEntity instructor);

    /**
     * Find courses by instructor ID
     */
    List<CourseEntity> findByInstructorId(Long instructorId);

    /**
     * Find published courses by instructor
     */
    List<CourseEntity> findByInstructorAndPublishedTrue(InstructorEntity instructor);

    /**
     * Find all published courses
     */
    List<CourseEntity> findByPublishedTrue();

    /**
     * Find courses by category
     */
    List<CourseEntity> findByCategoryAndPublishedTrue(String category);

    /**
     * Find courses by level
     */
    List<CourseEntity> findByLevelAndPublishedTrue(String level);

    /**
     * Search courses by title containing keyword
     */
    List<CourseEntity> findByTitleContainingIgnoreCaseAndPublishedTrue(String keyword);

    /**
     * Find courses with pagination
     */
    Page<CourseEntity> findByPublishedTrue(Pageable pageable);

    /**
     * Find courses by instructor with pagination
     */
    Page<CourseEntity> findByInstructor(InstructorEntity instructor, Pageable pageable);

    /**
     * Custom query to find courses with modules count
     */
    @Query("SELECT c FROM CourseEntity c LEFT JOIN FETCH c.modules m WHERE c.published = true ORDER BY c.createdAt DESC")
    List<CourseEntity> findPublishedCoursesWithModules();

    /**
     * Find courses by price range
     */
    @Query("SELECT c FROM CourseEntity c WHERE c.price >= :minPrice AND c.price <= :maxPrice AND c.published = true")
    List<CourseEntity> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    /**
     * Count courses by instructor
     */
    long countByInstructor(InstructorEntity instructor);

    /**
     * Count published courses by instructor
     */
    long countByInstructorAndPublishedTrue(InstructorEntity instructor);
}