package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.library.models.Member;
import com.library.util.DatabaseConnection;

public class MemberDAO {
    public int addMember(Member member) throws SQLException {
        String sql = "INSERT INTO members (name, email, phone, join_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setDate(4, new java.sql.Date(member.getJoinDate().getTime()));
            
            stmt.executeUpdate();
            
            // Get the generated member_id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int memberId = rs.getInt(1);
                    member.setMemberId(memberId); // Update the member object with the new ID
                    return memberId;
                }
                throw new SQLException("Failed to get generated member ID");
            }
        }
    }

    public List<Member> getAllMembers() throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                members.add(new Member(
                    rs.getInt("member_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getDate("join_date")
                ));
            }
        }
        return members;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void deleteMember(int memberId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);  // Start transaction

            // First delete related borrowings
            String deleteBorrowings = "DELETE FROM borrowings WHERE member_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteBorrowings)) {
                stmt.setInt(1, memberId);
                stmt.executeUpdate();
            }

            // Then delete related user account
            String deleteUser = "DELETE FROM users WHERE member_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteUser)) {
                stmt.setInt(1, memberId);
                stmt.executeUpdate();
            }

            // Finally delete the member
            String deleteMember = "DELETE FROM members WHERE member_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteMember)) {
                stmt.setInt(1, memberId);
                stmt.executeUpdate();
            }

            conn.commit();  // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
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
} 