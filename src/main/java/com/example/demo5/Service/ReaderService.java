package com.example.demo5.Service;

import com.example.demo5.Config.DatabaseManager;
import com.example.demo5.Model.Reader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReaderService {
    private static final Logger logger = LoggerFactory.getLogger(ReaderService.class);
    private static ObservableList<Reader> readers = FXCollections.observableArrayList(); // Lưu trữ tạm thời danh sách bạn đọc

    public static ObservableList<Reader> getReaders() {
        refreshReaders(); // Luôn refresh để chắc chắn
        return readers;
    }
// Thêm bạn đọc mới, trả về false nếu trùng ID
    public static boolean addReader(Reader reader) {
        String checkSql = "SELECT COUNT(*) FROM Readers WHERE Id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
            checkPstmt.setString(1, reader.getId());
            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // Trùng ID
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking reader ID {}: {}", reader.getId(), e.getMessage());
            return false;
        }
// Thêm vào database
        String sql = "INSERT INTO Readers (Id, Name, Email, Phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reader.getId());
            pstmt.setString(2, reader.getName());
            pstmt.setString(3, reader.getEmail());
            pstmt.setString(4, reader.getPhone());
            pstmt.executeUpdate();
            readers.add(reader);
            return true;
        } catch (SQLException e) {
            logger.error("Error adding reader {}: {}", reader.getId(), e.getMessage());
            return false;
        }
    }
// Cập nhật thông tin bạn đọc, trả về false nếu trùng ID mới
    public static boolean updateReader(Reader oldReader, Reader newReader) {
        if (!oldReader.getId().equals(newReader.getId())) { // Nếu thay ID, check duplicate
            String checkSql = "SELECT COUNT(*) FROM Readers WHERE Id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setString(1, newReader.getId());
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Trùng ID mới
                    }
                }
            } catch (SQLException e) {
                logger.error("Error checking new reader ID {}: {}", newReader.getId(), e.getMessage());
                return false;
            }
        }

        String sql = "UPDATE Readers SET Id = ?, Name = ?, Email = ?, Phone = ? WHERE Id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newReader.getId());
            pstmt.setString(2, newReader.getName());
            pstmt.setString(3, newReader.getEmail());
            pstmt.setString(4, newReader.getPhone());
            pstmt.setString(5, oldReader.getId());
            pstmt.executeUpdate();
            int index = readers.indexOf(oldReader);
            if (index != -1) readers.set(index, newReader);
            return true;
        } catch (SQLException e) {
            logger.error("Error updating reader {}: {}", oldReader.getId(), e.getMessage());
            return false;
        }
    }
// Xóa bạn đọc
    public static void deleteReader(Reader reader) {
        String sql = "DELETE FROM Readers WHERE Id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reader.getId());
            pstmt.executeUpdate();
            readers.remove(reader);
        } catch (SQLException e) {
            logger.error("Error deleting reader {}: {}", reader.getId(), e.getMessage());
        }
    }
// Tải lại danh sách bạn đọc từ database
    public static void refreshReaders() {
        readers.clear();
        String sql = "SELECT * FROM Readers";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                readers.add(new Reader(
                        rs.getString("Id"),
                        rs.getString("Name"),
                        rs.getString("Email"),
                        rs.getString("Phone")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error refreshing readers: {}", e.getMessage());
        }
    }
}