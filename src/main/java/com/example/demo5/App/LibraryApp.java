package com.example.demo5.App;

import com.example.demo5.View.LoginView;
import com.example.demo5.View.MainDashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LibraryApp extends Application {

    private Stage primaryStage;
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("LMS Prestige - Hệ Thống Quản Lý Thư Viện");

        showLoginScreen();
        primaryStage.show();
    }
    // Hiển thị màn hình đăng nhập
    private void showLoginScreen() {
        LoginView loginView = new LoginView(this::showDashboardScreen);
        Scene scene = new Scene(loginView, 650, 650);
        applyStyles(scene);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }
    // Hiển thị màn hình chính sau khi đăng nhập thành công
    private void showDashboardScreen() {
        MainDashboardView dashboard = new MainDashboardView(this::showLoginScreen);
        Scene scene = new Scene(dashboard, 1000, 700);
        applyStyles(scene);
        primaryStage.setScene(scene);
        primaryStage.setTitle("LMS Prestige - Dashboard");
        primaryStage.centerOnScreen();
    }
    // Áp dụng các kiểu dáng CSS cho Scene
    private void applyStyles(Scene scene) {
        var cssUrl = getClass().getResource("/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
    }

}