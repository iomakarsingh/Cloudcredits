package com.library.models;

public class User {
    private final int userId;
    private final String username;
    private final String password;
    private final String role;
    private final int memberId;

    public User(int userId, String username, String password, String role, int memberId) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.memberId = memberId;
    }

    // Getters and setters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public int getMemberId() { return memberId; }
} 