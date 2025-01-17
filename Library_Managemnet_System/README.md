# Library Management System

A robust Java-based desktop application for managing library operations with a user-friendly GUI interface.

## Features

### User Management
- **Dual User Roles**: Supports both Admin and Member roles
- **Secure Authentication**: Password hashing and secure login system
- **User Registration**: New member registration with email validation
- **Password Requirements**: Strong password policy enforcement

### Book Management
- **Add Books**: Add new books with details (title, author, ISBN, quantity)
- **Delete Books**: Remove books from the system (with safeguards for borrowed books)
- **Book Inventory**: Track total and available quantities
- **Book Listing**: View all books with their current status

### Member Management (Admin Only)
- **Member Registration**: Add new library members
- **Member Directory**: View all registered members
- **Member Deletion**: Remove members (with safeguards for active borrowers)
- **Member Details**: Track member information (name, email, phone, join date)

### Borrowing System
- **Book Borrowing**: Members can borrow available books
- **Book Returns**: Process book returns
- **Availability Tracking**: Real-time tracking of book availability
- **Borrowing History**: Maintain records of all transactions

### User Interface
- **Modern GUI**: Clean and intuitive Swing-based interface
- **Tabbed Interface**: Easy navigation between different functions
- **Role-Based Access**: Dynamic UI adapting to user roles
- **Interactive Tables**: Display and manage data in tabulated format
- **Responsive Design**: User-friendly layout and controls

## Technical Stack

### Backend
- **Language**: Java 8
- **Database**: MySQL 8.0
- **Connection Pool**: Built-in connection management
- **Security**: SHA-256 password hashing

### Frontend
- **GUI Framework**: Java Swing
- **Layout Managers**: GridBagLayout, BorderLayout
- **Custom Components**: Styled JButtons, JPanels, JTables

### Build Tools
- **Build System**: Maven
- **Dependencies**: MySQL Connector/J 8.0.33

### Database Schema
- Users Table: User authentication and role management
- Books Table: Book inventory and availability
- Members Table: Member information and tracking
- Borrowings Table: Transaction records

## Setup Instructions

1. **Prerequisites**
   - Java JDK 8 or higher
   - MySQL 8.0 or higher
   - Maven

2. **Database Setup**
   ```bash
   mysql -u root -p < database_setup.sql
   ```

3. **Configure Database Connection**
   - Update credentials in `DatabaseConnection.java` if needed:
     ```java
     private static final String URL = "jdbc:mysql://localhost:3306/library_db";
     private static final String USER = "library_user";
     private static final String PASSWORD = "library123";
     ```

4. **Build and Run**
   ```bash
   mvn clean package
   java -jar target/library-management-1.0-SNAPSHOT.jar
   ```

## Default Login Credentials
- **Admin Account**
  - Username: admin
  - Password: admin123


## Security Features
- Password hashing using SHA-256
- Input validation for all forms
- Transaction management for data integrity
- Role-based access control

## Contributing
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License
This project is licensed under the MIT License - see the LICENSE file for details. 
