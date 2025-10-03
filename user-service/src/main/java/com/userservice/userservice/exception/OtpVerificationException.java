package com.userservice.userservice.exception;

public class OtpVerificationException extends RuntimeException {
    private final OtpErrorType errorType;

    public OtpVerificationException(OtpErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public OtpErrorType getErrorType() {
        return errorType;
    }

    public enum OtpErrorType {
        INVALID_OTP,
        EXPIRED_OTP,
        EMAIL_MISMATCH,
        INVALID_SESSION
    }
}
