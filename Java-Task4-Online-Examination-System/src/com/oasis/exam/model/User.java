package com.oasis.exam.model;

/**
 * Represents a student or candidate in the examination system.
 */
public class User {
    private String username;
    private String password;
    private String displayName;
    private String email;
    private String rollNumber;

    public User(String username, String password, String displayName, String email, String rollNumber) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.email = email;
        this.rollNumber = rollNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getInitials() {
        if (displayName == null || displayName.trim().isEmpty()) {
            return "ST";
        }
        String[] parts = displayName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
