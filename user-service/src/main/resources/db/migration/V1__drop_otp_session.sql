-- Flyway migration: drop legacy OTP table now that OTPs are stored in Redis
-- Safe to run multiple times due to IF EXISTS
DROP TABLE IF EXISTS otp_session;
