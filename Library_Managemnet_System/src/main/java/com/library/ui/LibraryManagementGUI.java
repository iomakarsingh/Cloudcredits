package com.library.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.library.models.Book;
import com.library.models.Member;
import com.library.models.User;
import com.library.service.LibraryService;

public class LibraryManagementGUI extends JFrame {
    private final LibraryService libraryService;
    private JTabbedPane tabbedPane;
    private JTable booksTable;
    private JTable membersTable;
    private User currentUser;

    public LibraryManagementGUI() {
        libraryService = new LibraryService();
        showLoginDialog();
        if (currentUser != null) {
            initializeUI();
        } else {
            System.exit(0);
        }
    }

    private void showLoginDialog() {
        LoginDialog loginDialog = new LoginDialog(this);
        loginDialog.setVisible(true);
        currentUser = loginDialog.getAuthenticatedUser();
    }

    private void initializeUI() {
        setTitle("Library Management System - " + 
            (currentUser.getRole().equals("ADMIN") ? "Administrator" : "Member"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Set custom colors
        Color primaryColor = new Color(51, 102, 153);   // Dark blue
        Color accentColor = new Color(240, 240, 240);   // Light gray
        Color buttonColor = new Color(70, 130, 180);    // Steel blue
        Color textColor = Color.WHITE;

        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));  // Added gaps
        mainPanel.setBackground(accentColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // Add padding

        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(primaryColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Add welcome message
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setForeground(textColor);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        // Create top panel for logout button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);
        JButton logoutButton = createStyledButton("Logout", buttonColor, textColor);
        logoutButton.addActionListener(e -> logout());
        topPanel.add(logoutButton);
        headerPanel.add(topPanel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Style the tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));
        tabbedPane.setBackground(accentColor);
        
        // Add tabs with styled panels
        tabbedPane.addTab("Books", createStyledPanel(createBooksPanel()));
        if ("ADMIN".equals(currentUser.getRole())) {
            tabbedPane.addTab("Members", createStyledPanel(createMembersPanel()));
        }
        tabbedPane.addTab("Borrowing", createStyledPanel(createBorrowingPanel()));
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);

        // Show/hide components based on user role
        if ("MEMBER".equals(currentUser.getRole())) {
            // For members, hide delete book button and show only their borrowing history
            JPanel booksPanel = (JPanel)tabbedPane.getComponentAt(tabbedPane.indexOfTab("Books"));
            Component[] components = booksPanel.getComponents();
            for (Component c : components) {
                if (c instanceof JPanel) {
                    JPanel buttonPanel = (JPanel)c;
                    for (Component b : buttonPanel.getComponents()) {
                        if (b instanceof JButton) {
                            JButton button = (JButton)b;
                            if (button.getText().equals("Delete Book") || 
                                button.getText().equals("Add Book")) {
                                buttonPanel.remove(button);
                            }
                        }
                    }
                }
            }

            // Modify borrowing panel to only show current member's ID
            JPanel borrowingPanel = (JPanel)tabbedPane.getComponentAt(tabbedPane.indexOfTab("Borrowing"));
            Component[] borrowComponents = borrowingPanel.getComponents();
            for (Component c : borrowComponents) {
                if (c instanceof JTextField) {
                    JTextField field = (JTextField)c;
                    if (field.getName() != null && field.getName().equals("memberIdField")) {
                        field.setText(String.valueOf(currentUser.getMemberId()));
                        field.setEditable(false);
                    }
                }
            }
        }
    }

    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // Create table

        booksTable = new JTable();
        styleTable(booksTable);
        JScrollPane scrollPane = new JScrollPane(booksTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create buttons panel with styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        Color buttonColor = new Color(70, 130, 180);

        // Only show Add/Delete buttons for admin
        if ("ADMIN".equals(currentUser.getRole())) {
            JButton addButton = createStyledButton("Add Book", buttonColor, Color.WHITE);
            JButton deleteButton = createStyledButton("Delete Book", buttonColor, Color.WHITE);
            buttonPanel.add(addButton);
            buttonPanel.add(deleteButton);
            
            // Add action listeners
            addButton.addActionListener(e -> showAddBookDialog());
            deleteButton.addActionListener(e -> deleteSelectedBook());
        }

        // Refresh button for all users
        JButton refreshButton = createStyledButton("Refresh", buttonColor, Color.WHITE);
        buttonPanel.add(refreshButton);
        refreshButton.addActionListener(e -> refreshBooksTable());

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // Create table

        membersTable = new JTable();
        styleTable(membersTable);  // Apply the same styling as books table
        JScrollPane scrollPane = new JScrollPane(membersTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create buttons panel with styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        Color buttonColor = new Color(70, 130, 180);

        // Create styled buttons
        JButton addButton = createStyledButton("Add Member", buttonColor, Color.WHITE);
        JButton deleteButton = createStyledButton("Delete Member", buttonColor, Color.WHITE);
        JButton refreshButton = createStyledButton("Refresh", buttonColor, Color.WHITE);

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        addButton.addActionListener(e -> {
            RegisterDialog registerDialog = new RegisterDialog(this);
            registerDialog.setVisible(true);
            refreshMembersTable();  // Refresh after adding
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = membersTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a member to delete", 
                    "Warning", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            String memberId = membersTable.getValueAt(selectedRow, 0).toString();
            String memberName = membersTable.getValueAt(selectedRow, 1).toString();

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete member: " + memberName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    libraryService.deleteMember(Integer.parseInt(memberId));
                    refreshMembersTable();
                    JOptionPane.showMessageDialog(this,
                        "Member deleted successfully!\n\nDeleted Member Details:\n" +
                        "ID: " + memberId + "\nName: " + memberName,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error deleting member: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        refreshButton.addActionListener(e -> refreshMembersTable());

        // Initial table load
        refreshMembersTable();

        return panel;
    }

    private JPanel createBorrowingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Member ID
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Member ID:"), gbc);
        JTextField memberIdField = new JTextField(10);
        memberIdField.setName("memberIdField");
        gbc.gridx = 1;
        panel.add(memberIdField, gbc);

        // If member is logged in, set and lock their ID
        if ("MEMBER".equals(currentUser.getRole())) {
            memberIdField.setText(String.valueOf(currentUser.getMemberId()));
            memberIdField.setEditable(false);
        }

        // Book ID
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Book ID:"), gbc);
        JTextField bookIdField = new JTextField(10);
        gbc.gridx = 1;
        panel.add(bookIdField, gbc);

        // Borrow/Return Buttons with styling
        Color buttonColor = new Color(70, 130, 180);
        JButton borrowButton = createStyledButton("Borrow Book", buttonColor, Color.WHITE);
        JButton returnButton = createStyledButton("Return Book", buttonColor, Color.WHITE);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(borrowButton, gbc);
        gbc.gridx = 1;
        panel.add(returnButton, gbc);

        // Show All Books Button
        JButton showBooksButton = createStyledButton("Show All Books", buttonColor, Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(showBooksButton, gbc);

        // Show All Members Button (only for admin)
        if ("ADMIN".equals(currentUser.getRole())) {
            JButton showMembersButton = createStyledButton("Show All Members", buttonColor, Color.WHITE);
            gbc.gridx = 1; gbc.gridy = 3;
            panel.add(showMembersButton, gbc);

            showMembersButton.addActionListener(e -> {
                try {
                    List<Member> members = libraryService.getAllMembers();
                    if (members.isEmpty()) {
                        JOptionPane.showMessageDialog(this, 
                            "No members registered in the library.", 
                            "Library Members", 
                            JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    
                    StringBuilder message = new StringBuilder("All Members:\n\n");
                    for (Member member : members) {
                        message.append(String.format("ID: %d\nName: %s\nEmail: %s\n" +
                                "Phone: %s\nJoin Date: %s\n\n",
                            member.getMemberId(), member.getName(), member.getEmail(),
                            member.getPhone(), member.getJoinDate().toString()));
                    }
                    JTextArea textArea = new JTextArea(message.toString());
                    textArea.setEditable(false);
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    scrollPane.setPreferredSize(new Dimension(400, 300));
                    JOptionPane.showMessageDialog(this, scrollPane, "All Members", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Error: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        // Add action listeners
        borrowButton.addActionListener(e -> {
            try {
                int memberId = Integer.parseInt(memberIdField.getText().trim());
                int bookId = Integer.parseInt(bookIdField.getText().trim());
                
                libraryService.borrowBook(memberId, bookId);
                refreshBooksTable();
                JOptionPane.showMessageDialog(this, 
                    "Book borrowed successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                bookIdField.setText("");  // Clear book ID field after successful borrow
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter valid Member ID and Book ID", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        returnButton.addActionListener(e -> {
            try {
                int memberId = Integer.parseInt(memberIdField.getText().trim());
                int bookId = Integer.parseInt(bookIdField.getText().trim());
                
                libraryService.returnBook(memberId, bookId);
                refreshBooksTable();
                JOptionPane.showMessageDialog(this, 
                    "Book returned successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                bookIdField.setText("");  // Clear book ID field after successful return
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter valid Member ID and Book ID", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        showBooksButton.addActionListener(e -> {
            try {
                List<Book> books = libraryService.getAllBooks();
                if (books.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "No books available in the library.", 
                        "Library Books", 
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                StringBuilder message = new StringBuilder("All Books:\n\n");
                for (Book book : books) {
                    message.append(String.format("ID: %d\nTitle: %s\nAuthor: %s\n" +
                            "ISBN: %s\nTotal Quantity: %d\nAvailable: %d\n\n",
                        book.getBookId(), book.getTitle(), book.getAuthor(),
                        book.getIsbn(), book.getQuantity(), book.getAvailableQuantity()));
                }
                JTextArea textArea = new JTextArea(message.toString());
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                JOptionPane.showMessageDialog(this, scrollPane, "All Books", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add fields
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Title:"), gbc);
        JTextField titleField = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Author:"), gbc);
        JTextField authorField = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("ISBN:"), gbc);
        JTextField isbnField = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(isbnField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Quantity:"), gbc);
        JTextField quantityField = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(quantityField, gbc);

        JButton saveButton = createStyledButton("Save", new Color(70, 130, 180), Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            try {
                // Validate input
                if (titleField.getText().trim().isEmpty() || 
                    authorField.getText().trim().isEmpty() || 
                    isbnField.getText().trim().isEmpty() || 
                    quantityField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "All fields are required!",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int quantity;
                try {
                    quantity = Integer.parseInt(quantityField.getText().trim());
                    if (quantity <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid positive number for quantity",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Book book = new Book(
                    titleField.getText().trim(),
                    authorField.getText().trim(),
                    isbnField.getText().trim(),
                    quantity
                );
                libraryService.addBook(book);
                dialog.dispose();
                refreshBooksTable();
                
                String message = String.format(
                    "Book added successfully!\n\nDetails:\nTitle: %s\nAuthor: %s\nISBN: %s\nQuantity: %s",
                    book.getTitle(), book.getAuthor(), book.getIsbn(), book.getQuantity()
                );
                JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void refreshBooksTable() {
        try {
            List<Book> books = libraryService.getAllBooks();
            String[][] data = new String[books.size()][6];
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                data[i] = new String[]{
                    String.valueOf(book.getBookId()),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    String.valueOf(book.getQuantity()),
                    String.valueOf(book.getAvailableQuantity())
                };
            }
            String[] columns = {"ID", "Title", "Author", "ISBN", "Total Qty", "Available"};
            booksTable.setModel(new DefaultTableModel(data, columns));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error refreshing books: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshMembersTable() {
        // Only proceed if user is admin
        if (!"ADMIN".equals(currentUser.getRole())) {
            return;
        }
        
        try {
            List<Member> members = libraryService.getAllMembers();
            String[][] data = new String[members.size()][5];
            for (int i = 0; i < members.size(); i++) {
                Member member = members.get(i);
                data[i] = new String[]{
                    String.valueOf(member.getMemberId()),
                    member.getName(),
                    member.getEmail(),
                    member.getPhone(),
                    member.getJoinDate().toString()
                };
            }
            String[] columns = {"ID", "Name", "Email", "Phone", "Join Date"};
            membersTable.setModel(new DefaultTableModel(data, columns));
        } catch (Exception e) {
            // Only show error for admin users
            if ("ADMIN".equals(currentUser.getRole())) {
                JOptionPane.showMessageDialog(this, 
                    "Error refreshing members: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a book to delete", 
                "Warning", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookId = booksTable.getValueAt(selectedRow, 0).toString();
        String bookTitle = booksTable.getValueAt(selectedRow, 1).toString();

        try {
            if (libraryService.isBookBorrowed(Integer.parseInt(bookId))) {
                JOptionPane.showMessageDialog(this,
                    "Cannot delete book: " + bookTitle + "\nThis book is currently borrowed.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete book: " + bookTitle + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                libraryService.deleteBook(Integer.parseInt(bookId));
                refreshBooksTable();
                JOptionPane.showMessageDialog(this,
                    "Book deleted successfully!\n\nDeleted Book Details:\nID: " + bookId + "\nTitle: " + bookTitle,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error deleting book: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();  // Close current window
            SwingUtilities.invokeLater(() -> {
                LibraryManagementGUI newGui = new LibraryManagementGUI();
                newGui.setVisible(true);
                newGui.refreshBooksTable();
                if ("ADMIN".equals(newGui.currentUser.getRole())) {
                    newGui.refreshMembersTable();
                }
            });
        }
    }

    // Helper method to create styled buttons
    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("override")
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            @SuppressWarnings("override")
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    // Helper method to create styled panels
    private JPanel createStyledPanel(JPanel contentPanel) {
        JPanel styledPanel = new JPanel(new BorderLayout(10, 10));
        styledPanel.setBackground(Color.WHITE);
        styledPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        styledPanel.add(contentPanel, BorderLayout.CENTER);
        return styledPanel;
    }

    // Update table styling
    private void styleTable(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setRowHeight(25);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryManagementGUI gui = new LibraryManagementGUI();
            gui.setVisible(true);
            gui.refreshBooksTable();
            // Only refresh members table if user is admin
            if ("ADMIN".equals(gui.currentUser.getRole())) {
                gui.refreshMembersTable();
            }
        });
    }
} 