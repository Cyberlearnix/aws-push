package com.userservice.userservice.repository;

/**
 * Deprecated placeholder: OTP/session storage moved to Redis (see OtpCacheService).
 *
 * Intentionally NOT extending JpaRepository to prevent Spring from
 * creating a JPA repository bean for a non-entity domain type.
 */
@Deprecated
public interface OtpSessionRepository {
}
