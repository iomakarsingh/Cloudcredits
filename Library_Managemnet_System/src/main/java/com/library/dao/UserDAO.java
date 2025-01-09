package com.library.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import com.library.models.User;
import com.library.util.DatabaseConnection;

public class UserDAO {
    public User authenticate(String username, String password) throws SQLException {
        String hashedPassword = hashPassword(password);
        System.out.println("Login attempt:");
        System.out.println("Username: " + username);
        System.out.println("Input Password: " + password);
        System.out.println("Hashed Password: " + hashedPassword);
        
        String sql = "SELECT * FROM users WHERE username = ?";  // First check just username
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                System.out.println("Found user. Stored password hash: " + storedPassword);
                
                if (storedPassword.equals(hashedPassword)) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getInt("member_id")
                    );
                }
            } else {
                System.out.println("No user found with username: " + username);
            }
            return null;
        }
    }

    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, member_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashPassword(user.getPassword()));
            stmt.setString(3, user.getRole());
            stmt.setInt(4, user.getMemberId());
            stmt.executeUpdate();
        }
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public boolean isPasswordValid(String password) {
        // Password must be at least 8 characters long
        if (password.length() < 8) return false;
        
        // Must contain at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) return false;
        
        // Must contain at least one lowercase letter
        if (!password.matches(".*[a-z].*")) return false;
        
        // Must contain at least one number
        if (!password.matches(".*\\d.*")) return false;
        // Must contain at least one special character
        
        return password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    }
} 