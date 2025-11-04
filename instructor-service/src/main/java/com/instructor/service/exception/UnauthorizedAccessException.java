package com.instructor.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UnauthorizedAccessException extends ResponseStatusException {
    public UnauthorizedAccessException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
