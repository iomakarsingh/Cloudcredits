package com.library.ui;

import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.library.models.Book;
import com.library.models.Member;
import com.library.service.LibraryService;

public class LibraryManagementSystem {
    private final static LibraryService libraryService = new LibraryService();
    private final static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            try {
                System.out.println("\n=== Library Management System ===");
                System.out.println("1. Add Book");
                System.out.println("2. Add Member");
                System.out.println("3. Borrow Book");
                System.out.println("4. Return Book");
                System.out.println("5. List All Books");
                System.out.println("6. List All Members");
                System.out.println("7. Exit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        addBook();
                        break;
                    case 2:
                        addMember();
                        break;
                    case 3:
                        borrowBook();
                        break;
                    case 4:
                        returnBook();
                        break;
                    case 5:
                        listBooks();
                        break;
                    case 6:
                        listMembers();
                        break;
                    case 7:
                        System.out.println("Goodbye!");
                        System.exit(0);
                    default:
                        System.out.println("Invalid option!");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine(); // Clear the scanner buffer
            }
        }
    }

    private static void addBook() throws Exception {
        try {
            System.out.println("\n=== Add New Book ===");
            System.out.print("Enter title: ");
            String title = scanner.nextLine();
            
            System.out.print("Enter author: ");
            String author = scanner.nextLine();
            
            System.out.print("Enter ISBN: ");
            String isbn = scanner.nextLine();
            
            System.out.print("Enter quantity: ");
            int quantity = scanner.nextInt();
            
            Book book = new Book(title, author, isbn, quantity);
            libraryService.addBook(book);
            System.out.println("Book added successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void addMember() throws Exception {
        try {
            System.out.println("\n=== Add New Member ===");
            System.out.print("Enter name: ");
            String name = scanner.nextLine();
            
            System.out.print("Enter email: ");
            String email = scanner.nextLine();
            
            System.out.print("Enter phone: ");
            String phone = scanner.nextLine();
            
            Member member = new Member(0, name, email, phone, new Date());
            libraryService.addMember(member);
            System.out.println("Member added successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void borrowBook() throws Exception {
        try {
            System.out.println("\n=== Borrow Book ===");
            System.out.print("Enter member ID: ");
            int memberId = scanner.nextInt();
            
            System.out.print("Enter book ID: ");
            int bookId = scanner.nextInt();
            
            libraryService.borrowBook(memberId, bookId);
            System.out.println("Book borrowed successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void returnBook() throws Exception {
        try {
            System.out.println("\n=== Return Book ===");
            System.out.print("Enter member ID: ");
            int memberId = scanner.nextInt();
            
            System.out.print("Enter book ID: ");
            int bookId = scanner.nextInt();
            
            libraryService.returnBook(memberId, bookId);
            System.out.println("Book returned successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void listBooks() throws Exception {
        try {
            System.out.println("\n=== All Books ===");
            List<Book> books = libraryService.getAllBooks();
            if (books.isEmpty()) {
                System.out.println("No books found.");
                return;
            }
            
            System.out.printf("%-5s %-30s %-20s %-15s %-10s %-10s%n", 
                "ID", "Title", "Author", "ISBN", "Total", "Available");
            System.out.println("-".repeat(90));
            
            for (Book book : books) {
                System.out.printf("%-5d %-30s %-20s %-15s %-10d %-10d%n",
                    book.getBookId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    book.getQuantity(),
                    book.getAvailableQuantity());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void listMembers() throws Exception {
        try {
            System.out.println("\n=== All Members ===");
            List<Member> members = libraryService.getAllMembers();
            if (members.isEmpty()) {
                System.out.println("No members found.");
                return;
            }
            
            System.out.printf("%-5s %-20s %-25s %-15s %-15s%n", 
                "ID", "Name", "Email", "Phone", "Join Date");
            System.out.println("-".repeat(80));
            
            for (Member member : members) {
                System.out.printf("%-5d %-20s %-25s %-15s %-15s%n",
                    member.getMemberId(),
                    member.getName(),
                    member.getEmail(),
                    member.getPhone(),
                    member.getJoinDate());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
} 
