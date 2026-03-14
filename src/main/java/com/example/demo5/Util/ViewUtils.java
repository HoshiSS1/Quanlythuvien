package com.example.demo5.Util;

import javafx.scene.control.*;

// Các tiện ích chung cho giao diện người dùng
public class ViewUtils {
    // Tạo nút với kiểu dáng chuẩn
    public static Button createButton(String text, String icon, String styleClass) {
        Button btn = new Button(icon + " " + text);
        btn.getStyleClass().addAll("btn-action", styleClass);
        btn.setPrefHeight(38);
        return btn;
    }
    // Hiển thị hộp thoại cảnh báo hoặc thông tin
    public static void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    // Hiển thị hộp thoại xác nhận và trả về kết quả
    public static boolean confirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

}
