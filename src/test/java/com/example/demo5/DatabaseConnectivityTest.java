package com.example.demo5;

import com.example.demo5.Config.DatabaseManager;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseConnectivityTest {

    @Test
    public void testConnection() {
        try (Connection conn = DatabaseManager.getConnection()) {
            assertNotNull(conn, "Connection should not be null");
            assertTrue(conn.isValid(5), "Connection should be valid");
            System.out.println("Database connectivity test passed!");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database connectivity test failed: " + e.getMessage());
        }
    }
}
