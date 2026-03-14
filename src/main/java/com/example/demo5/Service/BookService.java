package com.example.demo5.Service;

import com.example.demo5.Config.DatabaseManager;
import com.example.demo5.Model.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private static ObservableList<Book> books = FXCollections.observableArrayList();

    public static ObservableList<Book> getBooks() {
        if (books.isEmpty()) {
            refreshBooks(); // Nếu danh sách rỗng thì tải
        }
        return books;
    }
// Tải lại danh sách sách từ database
    public static void refreshBooks() {
        books.clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Books")) {
            while (rs.next()) {
                books.add(new Book(
                        rs.getString("Id"),
                        rs.getString("Title"),
                        rs.getString("Author"),
                        rs.getString("Category"),
                        rs.getInt("Quantity")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error refreshing books: {}", e.getMessage());
        }
    }
// Thêm sách mới, trả về false nếu trùng ID
    public static boolean addBook(Book book) {
        String checkSql = "SELECT COUNT(*) FROM Books WHERE Id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
            checkPstmt.setString(1, book.getId());
            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // Trùng ID
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking book ID {}: {}", book.getId(), e.getMessage());
            return false;
        }

        String sql = "INSERT INTO Books (Id, Title, Author, Category, Quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getId());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getCategory());
            pstmt.setInt(5, book.getQuantity());
            pstmt.executeUpdate();
            books.add(book);
            return true;
        } catch (SQLException e) {
            logger.error("Error adding book {}: {}", book.getId(), e.getMessage());
            return false;
        }
    }
// Cập nhật thông tin sách, trả về false nếu trùng ID mới
    public static boolean updateBook(Book oldBook, Book newBook) {
        if (!oldBook.getId().equals(newBook.getId())) { // Nếu thay ID, check duplicate
            String checkSql = "SELECT COUNT(*) FROM Books WHERE Id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setString(1, newBook.getId());
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Trùng ID mới
                    }
                }
            } catch (SQLException e) {
                logger.error("Error checking new book ID {}: {}", newBook.getId(), e.getMessage());
                return false;
            }
        }

        String sql = "UPDATE Books SET Id = ?, Title = ?, Author = ?, Category = ?, Quantity = ? WHERE Id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newBook.getId());
            pstmt.setString(2, newBook.getTitle());
            pstmt.setString(3, newBook.getAuthor());
            pstmt.setString(4, newBook.getCategory());
            pstmt.setInt(5, newBook.getQuantity());
            pstmt.setString(6, oldBook.getId());
            pstmt.executeUpdate();
            refreshBooks(); // Tải lại để đồng bộ
            return true;
        } catch (SQLException e) {
            logger.error("Error updating book {}: {}", oldBook.getId(), e.getMessage());
            return false;
        }
    }
// Xóa sách
    public static void deleteBook(Book book) {
        String sql = "DELETE FROM Books WHERE Id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getId());
            pstmt.executeUpdate();
            books.remove(book);
        } catch (SQLException e) {
            logger.error("Error deleting book {}: {}", book.getId(), e.getMessage());
        }
    }
}