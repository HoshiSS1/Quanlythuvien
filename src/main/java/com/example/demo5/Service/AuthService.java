package com.example.demo5.Service;

import com.example.demo5.Config.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Base64;
import java.security.SecureRandom;
import java.security.MessageDigest;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Dịch vụ xác thực người dùng
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // bits

    public static boolean login(String username, String password) {
        // Lower case username để case-insensitive
        String lowerUsername = username.toLowerCase();
        String sql = "SELECT Password FROM Users WHERE LOWER(Username) = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lowerUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString("Password");
                    return verifyPassword(password, stored);
                }
            }
        } catch (SQLException e) {
            logger.error("SQL error during login for user {}: {}", lowerUsername, e.getMessage());
        }
        return false;
    }

    // Đăng ký người dùng mới, trả về false nếu username đã tồn tại
    public static boolean register(String username, String password) {
        // Lower case username để case-insensitive
        String lowerUsername = username.toLowerCase();
        if (userExists(lowerUsername)) return false;
        // Hash mật khẩu trước khi lưu (store = base64(salt):base64(hash))
        String stored = generateStoredPassword(password);
        String sql = "INSERT INTO Users (Username, Password) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lowerUsername);
            pstmt.setString(2, stored);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("SQL error during register for user {}: {}", lowerUsername, e.getMessage());
            return false;
        }
    }

    // Kiểm tra xem người dùng đã tồn tại chưa
    private static boolean userExists(String lowerUsername) {
        String sql = "SELECT 1 FROM Users WHERE LOWER(Username) = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lowerUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.warn("SQL error during userExists for user {}: {}", lowerUsername, e.getMessage());
            return false;
        }
    }

    // --- Password hashing helpers using PBKDF2 ---
    private static String generateStoredPassword(String password) {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        if (hash == null) throw new IllegalStateException("Failed to generate password hash");
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    private static boolean verifyPassword(String password, String stored) {
        if (stored == null || !stored.contains(":")) return false;
        String[] parts = stored.split(":");
        if (parts.length != 2) return false;
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] hash = Base64.getDecoder().decode(parts[1]);
        byte[] computed = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        if (computed == null) return false;
        return MessageDigest.isEqual(hash, computed);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            logger.error("Error while hashing password: {}", ex.getMessage());
            return null;
        }
    }
}