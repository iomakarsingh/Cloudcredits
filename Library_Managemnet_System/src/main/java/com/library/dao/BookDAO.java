package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.library.models.Book;
import com.library.util.DatabaseConnection;

public class BookDAO {
    public void addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, author, isbn, quantity, available_quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setInt(4, book.getQuantity());
            stmt.setInt(5, book.getQuantity());  // Initially available = total
            stmt.executeUpdate();
        }
    }

    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                books.add(new Book(
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("isbn"),
                    rs.getInt("quantity"),
                    rs.getInt("available_quantity")
                ));
            }
        }
        return books;
    }

    public Book getBookById(int bookId) throws SQLException {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getInt("quantity"),
                        rs.getInt("available_quantity")
                    );
                }
                return null;
            }
        }
    }

    public void updateBookAvailability(int bookId, int newAvailableQuantity) throws SQLException {
        String sql = "UPDATE books SET available_quantity = ? WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newAvailableQuantity);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void deleteBook(int bookId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);  // Start transaction

            // First delete related borrowings
            String deleteBorrowings = "DELETE FROM borrowings WHERE book_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteBorrowings)) {
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
            }

            // Then delete the book
            String deleteBook = "DELETE FROM books WHERE book_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteBook)) {
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
            }

            conn.commit();  // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new SQLException("Error during rollback: " + ex.getMessage());
                }
            }
            throw new SQLException("Error deleting book: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isBookBorrowed(int bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrowings WHERE book_id = ? AND return_date IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
} 