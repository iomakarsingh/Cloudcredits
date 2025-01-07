package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.library.util.DatabaseConnection;

public class BorrowingDAO {
    public void borrowBook(int memberId, int bookId) throws SQLException {
        String sql = "INSERT INTO borrowings (member_id, book_id, borrow_date) VALUES (?, ?, CURRENT_DATE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
        }
    }

    public void returnBook(int memberId, int bookId) throws SQLException {
        String sql = "UPDATE borrowings SET return_date = CURRENT_DATE " +
                    "WHERE member_id = ? AND book_id = ? AND return_date IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
        }
    }

    public boolean hasActiveBorrowings(int memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrowings WHERE member_id = ? AND return_date IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean hasActiveBookBorrowings(int bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrowings WHERE book_id = ? AND return_date IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
} 