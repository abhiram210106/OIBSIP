package com.oasis.exam.service;

import com.oasis.exam.model.User;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles authentication, user session, and profile updates.
 */
public class AuthService {
    private final Map<String, User> userDatabase = new HashMap<>();

    public AuthService() {
        initDefaultUsers();
    }

    private void initDefaultUsers() {
        // Pre-configured student accounts for instant testing
        addUser(new User("student1", "pass123", "Rahul Sharma", "rahul.sharma@oasis.edu", "OASIS-2026-041"));
        addUser(new User("candidate", "exam2026", "Aarav Patel", "aarav.p@oasis.edu", "OASIS-2026-088"));
        addUser(new User("alice", "admin123", "Alice Johnson", "alice.j@oasis.edu", "OASIS-2026-102"));
        addUser(new User("guest", "guest", "Guest Candidate", "guest@oasis.edu", "OASIS-2026-999"));
    }

    public void addUser(User user) {
        userDatabase.put(user.getUsername().toLowerCase(), user);
    }

    public User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        User user = userDatabase.get(username.trim().toLowerCase());
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean updateProfile(User user, String newDisplayName, String currentPassword, String newPassword) {
        if (user == null) return false;
        
        // Verify current password
        if (!user.getPassword().equals(currentPassword)) {
            return false;
        }

        if (newDisplayName != null && !newDisplayName.trim().isEmpty()) {
            user.setDisplayName(newDisplayName.trim());
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPassword(newPassword.trim());
        }

        return true;
    }

    public boolean register(String username, String password, String displayName, String email) {
        if (username == null || username.trim().isEmpty()) return false;
        String key = username.trim().toLowerCase();
        if (userDatabase.containsKey(key)) {
            return false; // already exists
        }
        String roll = "OASIS-2026-" + (100 + (int)(Math.random() * 899));
        User newUser = new User(username.trim(), password, displayName.trim(), email.trim(), roll);
        addUser(newUser);
        return true;
    }
}
