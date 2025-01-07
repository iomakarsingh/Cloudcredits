package com.library.service;

import java.sql.SQLException;
import java.util.List;

import com.library.dao.BookDAO;
import com.library.dao.BorrowingDAO;
import com.library.dao.MemberDAO;
import com.library.models.Book;
import com.library.models.Member;

public class LibraryService {
    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;
    private final BorrowingDAO borrowingDAO;

    public LibraryService() {
        this.bookDAO = new BookDAO();
        this.memberDAO = new MemberDAO();
        this.borrowingDAO = new BorrowingDAO();
    }

    public void addBook(Book book) throws Exception {
        try {
            bookDAO.addBook(book);
        } catch (SQLException e) {
            throw new Exception("Error adding book: " + e.getMessage());
        }
    }

    public int addMember(Member member) throws Exception {
        try {
            return memberDAO.addMember(member);
        } catch (SQLException e) {
            throw new Exception("Error adding member: " + e.getMessage());
        }
    }

    public void borrowBook(int memberId, int bookId) throws Exception {
        try {
            Book book = bookDAO.getBookById(bookId);
            if (book == null) {
                throw new Exception("Book not found");
            }
            if (book.getAvailableQuantity() <= 0) {
                throw new Exception("Book is not available for borrowing");
            }
            
            borrowingDAO.borrowBook(memberId, bookId);
            bookDAO.updateBookAvailability(bookId, book.getAvailableQuantity() - 1);
        } catch (SQLException e) {
            throw new Exception("Error borrowing book: " + e.getMessage());
        }
    }

    public void returnBook(int memberId, int bookId) throws Exception {
        try {
            Book book = bookDAO.getBookById(bookId);
            if (book == null) {
                throw new Exception("Book not found");
            }
            borrowingDAO.returnBook(memberId, bookId);
            bookDAO.updateBookAvailability(bookId, book.getAvailableQuantity() + 1);
        } catch (SQLException e) {
            throw new Exception("Error returning book: " + e.getMessage());
        }
    }

    public List<Book> getAllBooks() throws Exception {
        try {
            return bookDAO.getAllBooks();
        } catch (SQLException e) {
            throw new Exception("Error getting books: " + e.getMessage());
        }
    }

    public List<Member> getAllMembers() throws Exception {
        try {
            return memberDAO.getAllMembers();
        } catch (SQLException e) {
            throw new Exception("Error getting members: " + e.getMessage());
        }
    }

    public void deleteMember(int memberId) throws Exception {
        try {
            // First check if member has any active borrowings
            if (borrowingDAO.hasActiveBorrowings(memberId)) {
                throw new Exception("Cannot delete member: Member has books currently borrowed");
            }
            memberDAO.deleteMember(memberId);
        } catch (SQLException e) {
            throw new Exception("Error deleting member: " + e.getMessage());
        }
    }

    public void deleteBook(int bookId) throws Exception {
        try {
            // First check if book has any active borrowings
            if (borrowingDAO.hasActiveBookBorrowings(bookId)) {
                throw new Exception("Cannot delete book: Book is currently borrowed");
            }
            bookDAO.deleteBook(bookId);
        } catch (SQLException e) {
            throw new Exception("Error deleting book: " + e.getMessage());
        }
    }

    public boolean isBookBorrowed(int bookId) throws Exception {
        try {
            return bookDAO.isBookBorrowed(bookId);
        } catch (SQLException e) {
            throw new Exception("Error checking book status: " + e.getMessage());
        }
    }
} 