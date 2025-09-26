package com.userservice.userservice.entity;

import com.userservice.userservice.enums.UserRole;
import jakarta.persistence.*;
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
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Hibernate 6+ UUID generator
    private UUID id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

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

    @Column(nullable = false)
    private String password; // BCrypt hash

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailVerified = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(length = 500)
    private String biography;

    private String language;

    private String alternatePhone;

    @Column(length = 100)
    private String photo; // URL or key

    // NEW: often present in your responses
    @Column(length = 10)
    private String countryCode;

    // --- Lockout fields ---
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
