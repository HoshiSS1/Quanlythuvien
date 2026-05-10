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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryService {
    private static final Logger logger = LoggerFactory.getLogger(LibraryService.class);
    private static final ObservableList<Loan> loans = FXCollections.observableArrayList();

    public static ObservableList<Loan> getAllLoans() {
        refreshLoans();
        return loans;
    }

    // Lấy dữ liệu phiếu mượn từ DB
    public static void refreshLoans() {
        loans.clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Loans ORDER BY id DESC")) {
            while (rs.next()) {
                Date returnDateSql = rs.getDate("returnDate");
                LocalDate returnDate = (returnDateSql != null) ? returnDateSql.toLocalDate() : null;

                loans.add(new Loan(
                        rs.getString("id"),
                        rs.getString("readerName"),
                        rs.getString("bookTitle"),
                        rs.getDate("borrowDate").toLocalDate(),
                        returnDate,
                        rs.getString("status"),
                        rs.getInt("quantity")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error refreshing loans: {}", e.getMessage());
        }
    }

    // Mượn sách
    public static boolean borrowBook(Reader reader, Book book, int qty, int borrowDays) {
        String countSql = "SELECT COUNT(*) FROM Loans WHERE readerName = ? AND bookTitle = ? AND status = ?";
        String checkSql = "SELECT Quantity FROM Books WHERE Id = ?";
        String insertLoanSql = "INSERT INTO Loans (id, readerName, bookTitle, borrowDate, returnDate, status, quantity) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String updateStockSql = "UPDATE Books SET Quantity = Quantity - ? WHERE Id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 0. Chặn quá hạn
                if (hasOverdueLoan(reader.getName())) {
                    logger.warn("User {} has overdue loans, cannot borrow more.", reader.getName());
                    return false;
                }

                // 1. Kiểm tra quy định mượn (đã mượn chưa trả chưa)
                try (PreparedStatement countPs = conn.prepareStatement(countSql)) {
                    countPs.setString(1, reader.getName());
                    countPs.setString(2, book.getTitle());
                    countPs.setString(3, Loan.STATUS_BORROWED);
                    try (ResultSet rs = countPs.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return false; // Đã mượn rồi
                        }
                    }
                }

                // 2. Kiểm tra tồn kho
                try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                    checkPs.setString(1, book.getId().trim());
                    try (ResultSet checkRs = checkPs.executeQuery()) {
                        if (!checkRs.next() || checkRs.getInt("Quantity") < qty) {
                            return false;
                        }
                    }
                }

                // 3. Tạo ID tự động
                int max = 0;
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT id FROM Loans")) {
                    while (rs.next()) {
                        try {
                            String idStr = rs.getString(1);
                            if (idStr != null) {
                                int num = Integer.parseInt(idStr.replaceAll("[^0-9]", ""));
                                if (num > max) max = num;
                            }
                        } catch (Exception e) {}
                    }
                }
                String newId = String.format("LOAN%03d", max + 1);

                // 4. Tính toán ngày trả
                LocalDate borrowDate = LocalDate.now();
                LocalDate returnDate = borrowDate.plusDays(borrowDays);

                // 5. Lưu phiếu mượn
                try (PreparedStatement ps = conn.prepareStatement(insertLoanSql)) {
                    ps.setString(1, newId);
                    ps.setString(2, reader.getName());
                    ps.setString(3, book.getTitle());
                    ps.setDate(4, Date.valueOf(borrowDate));
                    ps.setDate(5, Date.valueOf(returnDate));
                    ps.setString(6, Loan.STATUS_BORROWED);
                    ps.setInt(7, qty);
                    ps.executeUpdate();
                }

                // 6. Trừ kho sách
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

    // Overload
    public static boolean borrowBook(Reader reader, Book book, int qty) {
        return borrowBook(reader, book, qty, 14); 
    }

    // Xóa phiếu mượn
    public static void deleteLoan(Loan loan) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!loan.isReturned()) {
                    String updateStock = "UPDATE Books SET Quantity = Quantity + ? WHERE Title = ?";
                    try (PreparedStatement psStock = conn.prepareStatement(updateStock)) {
                        psStock.setInt(1, loan.getQuantity());
                        psStock.setString(2, loan.getBookTitle());
                        psStock.executeUpdate();
                    }
                }

                try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM Loans WHERE id = ?")) {
                    psDel.setString(1, loan.getId());
                    psDel.executeUpdate();
                }

                conn.commit();
                loans.remove(loan);
                BookService.refreshBooks();
            } catch (SQLException e) {
                conn.rollback();
            }
        } catch (SQLException e) {}
    }

    // Trả sách
    public static boolean returnBook(Loan loan) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE Loans SET status = ? WHERE id = ?")) {
                    ps.setString(1, Loan.STATUS_RETURNED);
                    ps.setString(2, loan.getId());
                    ps.executeUpdate();
                }

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
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // Cập nhật ngày hạn trả
    public static boolean updateLoan(Loan loan) {
        String updateSql = "UPDATE Loans SET returnDate = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setDate(1, Date.valueOf(loan.getReturnDate()));
            ps.setString(2, loan.getId());
            if (ps.executeUpdate() > 0) {
                refreshLoans();
                return true;
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    // Kiểm tra nợ quá hạn (QUÁ HẠN TỨC LÀ returnDate < now)
    public static boolean hasOverdueLoan(String readerName) {
        String sql = "SELECT COUNT(*) FROM Loans WHERE readerName = ? AND status = ? AND returnDate < ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, readerName);
            ps.setString(2, Loan.STATUS_BORROWED);
            ps.setDate(3, java.sql.Date.valueOf(LocalDate.now())); 
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Lấy danh sách phiếu quá hạn kèm email bạn đọc.
     * JOIN bảng Loans với bảng Readers qua readerName = Name.
     */
    public static List<Map<String, String>> getOverdueLoansWithEmail() {
        List<Map<String, String>> result = new ArrayList<>();
        
        // Cực kỳ quan trọng: Lấy trực tiếp từ danh sách bộ nhớ (để đồng nhất 100% logic với bảng UI)
        for (Loan loan : loans) {
            if (loan.isOverdue() && !loan.isReturned()) {
                Map<String, String> entry = new HashMap<>();
                entry.put("loanId", loan.getId());
                entry.put("readerName", loan.getReaderName());
                entry.put("bookTitle", loan.getBookTitle());
                entry.put("borrowDate", loan.getBorrowDate().toString());
                entry.put("returnDate", loan.getReturnDate().toString());
                entry.put("overdueDays", String.valueOf(-loan.getRemainingDays()));
                
                // Lấy email từ ReaderService
                String email = "";
                for (Reader r : ReaderService.getReaders()) {
                    if (r.getName().equalsIgnoreCase(loan.getReaderName())) {
                        email = r.getEmail();
                        break;
                    }
                }
                entry.put("email", email);
                result.add(entry);
            }
        }
        
        return result;
    }
}