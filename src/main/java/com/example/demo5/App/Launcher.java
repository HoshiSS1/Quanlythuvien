package com.example.demo5.App;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // QUAN TRỌNG: Thiết lập các thông số hệ thống TRƯỚC KHI launch JavaFX Toolkit
        // Giúp fix lỗi font hiển thị trên Windows và hiển thị dấu chấm mật khẩu bình thường
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.subpixeltext", "false");
        System.setProperty("javafx.font", "Segoe UI");

        Application.launch(LibraryApp.class, args);
    }
}