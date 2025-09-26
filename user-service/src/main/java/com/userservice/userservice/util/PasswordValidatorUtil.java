package com.userservice.userservice.util;

public class PasswordValidatorUtil {

    // Validates password with rules:
    // - length > 6
    // - contains at least one special character (non-alphanumeric)
    // - must not be the same as the username/fullName
    // - must not be the same as the email or email local-part
    public static void validateOrThrow(String password, String email, String usernameOrFullName) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        String pwd = password.trim();

        if (pwd.length() <= 6) {
            throw new IllegalArgumentException("Password must be more than 6 characters");
        }

        if (!containsSpecialCharacter(pwd)) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }

        // Compare ignoring case and whitespace
        if (usernameOrFullName != null && !usernameOrFullName.isBlank()) {
            String uname = usernameOrFullName.trim();
            if (pwd.equalsIgnoreCase(uname)) {
                throw new IllegalArgumentException("Password must not be the same as your name or username");
            }
        }

        if (email != null && !email.isBlank()) {
            String e = email.trim();
            String local = e;
            int at = e.indexOf('@');
            if (at > 0) {
                local = e.substring(0, at);
            }
            if (pwd.equalsIgnoreCase(e) || pwd.equalsIgnoreCase(local)) {
                throw new IllegalArgumentException("Password must not be the same as your email or email name");
            }
        }
    }

    private static boolean containsSpecialCharacter(String s) {
        // Any non-alphanumeric character
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }
}
