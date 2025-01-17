package com.library.ui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.library.dao.UserDAO;
import com.library.models.User;

public class LoginDialog extends JDialog {
    private final UserDAO userDAO;
    private User authenticatedUser;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        userDAO = new UserDAO();
        initializeUI();
    }

    @SuppressWarnings("UseSpecificCatch")
    private void initializeUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username field
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        add(usernameField, gbc);

        // Password field
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register New Member");
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        // Login action
        loginButton.addActionListener(e -> {
            try {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                
                authenticatedUser = userDAO.authenticate(username, password);
                if (authenticatedUser != null) {
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Invalid username or password",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error during login: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // Register action
        registerButton.addActionListener(e -> {
            RegisterDialog registerDialog = new RegisterDialog(null);
            registerDialog.setVisible(true);
            if (registerDialog.getRegisteredUser() != null) {
                usernameField.setText(registerDialog.getRegisteredUser().getUsername());
                passwordField.setText("");
            }
        });

        pack();
        setLocationRelativeTo(getParent());
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
} 