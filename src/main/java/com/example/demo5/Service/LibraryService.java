package com.example.demo5.Service;

import com.example.demo5.Config.DatabaseManager;
import com.example.demo5.Model.Book;
import com.example.demo5.Model.Loan;
import com.example.demo5.Model.Reader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;

public class LibraryService {
    private static final Logger logger = LoggerFactory.getLogger(LibraryService.class);
    private static final ObservableList<Loan> loans = FXCollections.observableArrayList();
    public static ObservableList<Loan> getAllLoans() {
        refreshLoans();
        return loans;
    }
//    Lấy dữ liệu phiếu mượn từ DB, bao gồm cả cột số lượng
    public static void refreshLoans() {
        loans.clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Loans ORDER BY id DESC")) {
            while (rs.next()) {
                loans.add(new Loan(
                        rs.getString("id"),
                        rs.getString("readerName"),
                        rs.getString("bookTitle"),
                        rs.getDate("borrowDate").toLocalDate(),
                        rs.getString("status"),
                        rs.getInt("quantity") // Lấy thêm số lượng từ cột mới
                ));
            }
        } catch (SQLException e) {
            logger.error("Error refreshing loans: {}", e.getMessage());
        }
    }
//    Mượn sách với số lượng cụ thể
    public static boolean borrowBook(Reader reader, Book book, int qty) {
        String checkSql = "SELECT Quantity FROM Books WHERE Id = ?";
        String insertLoanSql = "INSERT INTO Loans (id, readerName, bookTitle, borrowDate, status, quantity) VALUES (?, ?, ?, ?, ?, ?)";
        String updateStockSql = "UPDATE Books SET Quantity = Quantity - ? WHERE Id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Kiểm tra tồn kho
                try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                    checkPs.setString(1, book.getId().trim());
                    try (ResultSet checkRs = checkPs.executeQuery()) {
                        if (!checkRs.next() || checkRs.getInt("Quantity") < qty) {
                            return false;
                        }
                    }
                }

                // Tạo mã ID tự động
                int max = 0;
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT id FROM Loans")) {
                    while (rs.next()) {
                        try {
                            int num = Integer.parseInt(rs.getString(1).replaceAll("[^0-9]", ""));
                            if (num > max) max = num;
                        } catch (Exception e) {}
                    }
                }
                String newId = String.format("LOAN%03d", max + 1);

                // Lưu phiếu mượn
                try (PreparedStatement ps = conn.prepareStatement(insertLoanSql)) {
                    ps.setString(1, newId);
                    ps.setString(2, reader.getName());
                    ps.setString(3, book.getTitle());
                    ps.setDate(4, Date.valueOf(LocalDate.now()));
                    ps.setString(5, Loan.STATUS_BORROWED);
                    ps.setInt(6, qty);
                    ps.executeUpdate();
                }

                // Trừ kho sách
                try (PreparedStatement ps2 = conn.prepareStatement(updateStockSql)) {
                    ps2.setInt(1, qty);
                    ps2.setString(2, book.getId().trim());
                    ps2.executeUpdate();
                }

                conn.commit();
                refreshLoans();
                BookService.refreshBooks();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Transaction failed during borrowBook: {}", e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Database connection error during borrowBook: {}", e.getMessage());
            return false;
        }
    }
    // Xóa phiếu mượn, nếu chưa trả thì cộng lại số lượng vào kho
    public static void deleteLoan(Loan loan) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Nếu phiếu chưa trả, khi xóa phải cộng lại số lượng vào kho
                if (!loan.isReturned()) {
                    String updateStock = "UPDATE Books SET Quantity = Quantity + ? WHERE Title = ?";
                    try (PreparedStatement psStock = conn.prepareStatement(updateStock)) {
                        psStock.setInt(1, loan.getQuantity());
                        psStock.setString(2, loan.getBookTitle());
                        psStock.executeUpdate();
                    }
                }

                // 2. Xóa phiếu mượn
                try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM Loans WHERE id = ?")) {
                    psDel.setString(1, loan.getId());
                    psDel.executeUpdate();
                }

                conn.commit();
                loans.remove(loan);
                BookService.refreshBooks();
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Transaction failed during deleteLoan: {}", e.getMessage());
            }
        } catch (SQLException e) {
            logger.error("Database connection error during deleteLoan: {}", e.getMessage());
        }
    }
    //  Trả sách
    public static boolean returnBook(Loan loan) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Cập nhật trạng thái phiếu thành 'Returned'
                try (PreparedStatement ps = conn.prepareStatement("UPDATE Loans SET status = ? WHERE id = ?")) {
                    ps.setString(1, Loan.STATUS_RETURNED);
                    ps.setString(2, loan.getId());
                    ps.executeUpdate();
                }

                // 2. Cộng trả lại số lượng sách vào kho
                String updateStock = "UPDATE Books SET Quantity = Quantity + ? WHERE Title = ?";
                try (PreparedStatement psStock = conn.prepareStatement(updateStock)) {
                    psStock.setInt(1, loan.getQuantity());
                    psStock.setString(2, loan.getBookTitle());
                    psStock.executeUpdate();
                }

                conn.commit();
                loan.setStatus(Loan.STATUS_RETURNED);
                BookService.refreshBooks();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Transaction failed during returnBook: {}", e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Database connection error during returnBook: {}", e.getMessage());
            return false;
        }
    }
}