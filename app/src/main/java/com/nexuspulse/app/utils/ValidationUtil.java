package com.nexuspulse.app.utils;

import android.util.Patterns;

public class ValidationUtil {

    public static boolean isValidEmail(String email) {
        return email != null && !email.isEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= Constants.MIN_PASSWORD_LENGTH;
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    public static boolean isValidDisplayName(String displayName) {
        return displayName != null && !displayName.trim().isEmpty() &&
                displayName.length() <= 50;
    }

    public static boolean isValidPostContent(String content) {
        return content != null && !content.trim().isEmpty() &&
                content.length() <= Constants.MAX_POST_LENGTH;
    }

    public static String getEmailError(String email) {
        if (email == null || email.isEmpty()) {
            return "Email cannot be empty";
        }
        if (!isValidEmail(email)) {
            return "Invalid email format";
        }
        return null;
    }

    public static String getPasswordError(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }
        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            return "Password must be at least 6 characters";
        }
        return null;
    }

    public static String getUsernameError(String username) {
        if (username == null || username.isEmpty()) {
            return "Username cannot be empty";
        }
        if (username.length() < Constants.MIN_USERNAME_LENGTH) {
            return "Username must be at least 3 characters";
        }
        if (username.length() > Constants.MAX_USERNAME_LENGTH) {
            return "Username must be less than 20 characters";
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return "Username can only contain letters, numbers, and underscores";
        }
        return null;
    }

    public static String getDisplayNameError(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return "Display name cannot be empty";
        }
        if (displayName.length() > 50) {
            return "Display name must be less than 50 characters";
        }
        return null;
    }
}
