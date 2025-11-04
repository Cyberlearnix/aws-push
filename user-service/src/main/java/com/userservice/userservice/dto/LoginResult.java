package com.userservice.userservice.dto;

import java.util.Map;

/**
 * Represents the result of a login attempt.
 * @param ok Whether the login was successful
 * @param response The response data if login was successful
 * @param errorMessage Error message if login failed
 * @param locked Whether the account is locked
 */
public record LoginResult(boolean ok, Map<String, Object> response, String errorMessage, boolean locked) {
    public static LoginResult success(Map<String, Object> response) {
        return new LoginResult(true, response, null, false);
    }

    public static LoginResult invalid(String errorMessage) {
        return new LoginResult(false, null, errorMessage, false);
    }

    public static LoginResult locked(String errorMessage) {
        return new LoginResult(false, null, errorMessage, true);
    }
}
