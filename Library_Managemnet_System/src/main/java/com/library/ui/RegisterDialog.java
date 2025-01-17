package com.library.ui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.library.dao.UserDAO;
import com.library.models.Member;
import com.library.models.User;
import com.library.util.DatabaseConnection;

public class RegisterDialog extends JDialog {
    private final UserDAO userDAO;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private User registeredUser;

    public RegisterDialog(Frame parent) {
        super(parent, "Register New Member", true);
        userDAO = new UserDAO();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Member details
        addField("Name:", nameField = new JTextField(20), gbc, 0);
        addField("Email:", emailField = new JTextField(20), gbc, 1);
        addField("Phone:", phoneField = new JTextField(20), gbc, 2);

        // Login credentials
        addField("Username:", usernameField = new JTextField(20), gbc, 3);
        addField("Password:", passwordField = new JPasswordField(20), gbc, 4);
        addField("Confirm Password:", confirmPasswordField = new JPasswordField(20), gbc, 5);

        // Register button
        JButton registerButton = new JButton("Register");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        add(registerButton, gbc);

        registerButton.addActionListener(e -> register());

        pack();
        setLocationRelativeTo(getParent());
    }

    private void addField(String label, JTextField field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        add(new JLabel(label), gbc);

        gbc.gridx = 1;
        add(field, gbc);
    }

    @SuppressWarnings("UseSpecificCatch")
    private void register() {
        try {
            // Validate all fields are filled
            if (nameField.getText().trim().isEmpty() || 
                emailField.getText().trim().isEmpty() || 
                phoneField.getText().trim().isEmpty() || 
                usernameField.getText().trim().isEmpty() || 
                passwordField.getPassword().length == 0) {
                
                JOptionPane.showMessageDialog(this, 
                    "All fields are required!", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate email format
            if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid email address!", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate phone number (adjust regex as needed)
            if (!phoneField.getText().matches("\\d{10}")) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid 10-digit phone number!", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate password match
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, 
                    "Passwords do not match!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!userDAO.isPasswordValid(password)) {
                JOptionPane.showMessageDialog(this, 
                    "Password must contain at least:\n" +
                    "- 8 characters\n" +
                    "- One uppercase letter\n" +
                    "- One lowercase letter\n" +
                    "- One number\n" +
                    "- One special character",
                    "Invalid Password", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create member and user in a transaction
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false);  // Start transaction

                // Create member
                Member member = new Member(
                    nameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    new Date()
                );
                
                // Add member and get ID
                String memberSql = "INSERT INTO members (name, email, phone, join_date) VALUES (?, ?, ?, ?)";
                int memberId;
                try (PreparedStatement stmt = conn.prepareStatement(memberSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, member.getName());
                    stmt.setString(2, member.getEmail());
                    stmt.setString(3, member.getPhone());
                    stmt.setDate(4, new java.sql.Date(member.getJoinDate().getTime()));
                    stmt.executeUpdate();
                    
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            memberId = rs.getInt(1);
                        } else {
                            throw new SQLException("Failed to get member ID");
                        }
                    }
                }

                // Create user account
                String userSql = "INSERT INTO users (username, password, role, member_id) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                    stmt.setString(1, usernameField.getText().trim());
                    stmt.setString(2, userDAO.hashPassword(password));
                    stmt.setString(3, "MEMBER");
                    stmt.setInt(4, memberId);
                    stmt.executeUpdate();
                }

                // If we got here, commit the transaction
                conn.commit();
                
                registeredUser = new User(0, usernameField.getText(), password, "MEMBER", memberId);
                JOptionPane.showMessageDialog(this, 
                    "Registration successful!\nPlease login with your credentials.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } catch (SQLException ex) {
                // If anything goes wrong, rollback the transaction
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException e) {
                        throw new SQLException("Error during rollback: " + e.getMessage());
                    }
                }
                if (ex.getMessage().contains("Duplicate entry")) {
                    if (ex.getMessage().contains("email")) {
                        JOptionPane.showMessageDialog(this,
                            "This email is already registered!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    } else if (ex.getMessage().contains("username")) {
                        JOptionPane.showMessageDialog(this,
                            "This username is already taken!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Error during registration: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Error during registration: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        throw new SQLException("Error closing connection: " + e.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error during registration: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public User getRegisteredUser() {
        return registeredUser;
    }
} 