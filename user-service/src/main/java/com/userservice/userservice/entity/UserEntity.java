package com.userservice.userservice.entity;

import com.cyberlearnix.shared.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_entity",
        indexes = {
                @Index(name = "idx_user_email_unique", columnList = "email", unique = true),
                @Index(name = "idx_user_phone", columnList = "phone")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Size(min = 10, max = 50, message = "Phone number must be between 10 and 50 characters")
    @Column(nullable = false, length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private UserRole role = UserRole.STUDENT;

    private String address;

    private String linkedin;
    private String instagram;
    private String facebook;
    private String internshala;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String designation;

    @Column(length = 100)
    private String qualification;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 100)
    private String specialization;

    private Integer experienceYears;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    @Column(nullable = false)
    private String password; // BCrypt hash

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailVerified = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(length = 500)
    private String biography;

    @Size(max = 50, message = "Language must be at most 50 characters")
    @Column(length = 50)
    private String language;

    @Size(max = 50, message = "Alternate phone must be at most 50 characters")
    @Column(name = "alternate_phone", length = 50)
    private String alternatePhone;

    @Column(length = 100)
    private String photo; // URL or key

    @Size(max = 20, message = "Country code must be at most 20 characters")
    @Column(name = "country_code", length = 20)
    private String countryCode;

    @Builder.Default
    @Column(
            name = "failed_login_attempts",
            nullable = false,
            columnDefinition = "integer not null default 0"
    )
    private int failedLoginAttempts = 0;

    @Column(name = "lockout_until")
    private Instant lockoutUntil;  // null if not locked

    @Column(name = "last_login_at")
    private Instant lastLoginAt;
}
