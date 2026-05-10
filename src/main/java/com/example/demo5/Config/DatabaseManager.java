package com.example.demo5.Config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Quản lý kết nối database sử dụng HikariCP connection pooling.
 * Cung cấp kết nối hiệu quả và tối ưu cho ứng dụng.
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final HikariDataSource dataSource;

    static {
        try {
            ResourceBundle config = ResourceBundle.getBundle("dbconfig");
            HikariConfig hikariConfig = new HikariConfig();
            
            // Lấy thông tin từ dbconfig.properties
            String url = config.getString("db.url");
            String user = config.getString("db.user");
            String pass = config.getString("db.password");

            hikariConfig.setJdbcUrl(url);
            hikariConfig.setUsername(user);
            hikariConfig.setPassword(pass);
            
            // Cấu hình tối ưu cho HikariCP
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setIdleTimeout(300000);
            hikariConfig.setConnectionTimeout(30000);
            hikariConfig.setMaxLifetime(1800000);
            
            // QUAN TRỌNG: Khởi tạo bất đồng bộ để không block UI lúc khởi động nếu DB chậm
            hikariConfig.setInitializationFailTimeout(-1);
            
            // Cấu hình Driver class nếu cần (MSSQL)
            hikariConfig.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            dataSource = new HikariDataSource(hikariConfig);
            logger.info("Database connection pool (HikariCP) initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool: {}", e.getMessage(), e);
            throw new RuntimeException("Could not initialize database connection pool", e);
        }
    }

    /**
     * Lấy một kết nối từ pool.
     * @return Connection đối tượng kết nối database
     * @throws SQLException nếu không thể lấy kết nối
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Đóng data source khi ứng dụng kết thúc.
     */
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            logger.info("Database connection pool closed.");
        }
    }
}
