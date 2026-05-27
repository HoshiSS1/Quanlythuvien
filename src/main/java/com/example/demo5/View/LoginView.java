package com.example.demo5.View;

import com.example.demo5.Service.AuthService;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginView extends StackPane {
    private VBox loginBox;
    private VBox registerBox;
    private Runnable onLoginSuccess;

    // Fields cho Login
    private TextField loginUserField;
    private PasswordField loginPassField;
    private TextField loginPassText;
    private CheckBox loginShowPass;

    // Fields cho Register
    private TextField registerUserField;
    private PasswordField registerPassField;
    private TextField registerPassText;
    private PasswordField confirmPassField;
    private TextField confirmPassText;
    private CheckBox registerShowPass;

    // Constructor
    public LoginView(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;

        // QUAN TRỌNG: Gán class này để CSS Login hoạt động
        this.getStyleClass().clear();
        this.getStyleClass().add("login-root");

        StackPane card = new StackPane();
        card.getStyleClass().add("glass-card"); // Max size sang CSS

        initLoginBox();
        initRegisterBox();

        card.getChildren().addAll(loginBox, registerBox);

        Pane overlay = new Pane();
        overlay.getStyleClass().add("dark-overlay");

        getChildren().addAll(overlay, card);

        // Hiển thị login mặc định
        switchToLogin();
    }

    // Phần header với logo và tiêu đề
    private VBox createBrandingHeader(String titleText, String subTitleText) {
        VBox headerBox = new VBox();
        headerBox.getStyleClass().add("branding-header"); // Spacing, alignment sang CSS

        Label brandIcon = new Label("📖");
        brandIcon.getStyleClass().add("logo-icon");

        Label brandName = new Label("LMS PRESTIGE");
        brandName.getStyleClass().add("logo-text");

        Label title = new Label(titleText);
        title.getStyleClass().add("login-header-title"); // Đã định nghĩa trong CSS mới

        headerBox.getChildren().addAll(brandIcon, brandName, title);
        return headerBox;
    }

    // Phần login
    private void initLoginBox() {
        loginBox = new VBox();
        loginBox.getStyleClass().add("login-box"); // Spacing, padding, alignment sang CSS

        VBox header = createBrandingHeader("CHÀO MỪNG TRỞ LẠI", "Đăng nhập để tiếp tục quản lý thư viện");

        loginUserField = new TextField();
        loginUserField.getStyleClass().add("matte-input");
        loginUserField.setPromptText("Tên đăng nhập");
        // Mật khẩu với chức năng hiện/ẩn
        loginPassField = new PasswordField();
        loginPassText = new TextField();
        loginPassField.getStyleClass().add("matte-input");
        loginPassText.getStyleClass().add("matte-input");
        loginPassField.setPromptText("Mật khẩu");
        loginPassText.setPromptText("Mật khẩu");
        loginPassText.textProperty().bindBidirectional(loginPassField.textProperty());
        loginPassText.setVisible(false);
        loginPassText.setManaged(false);

        StackPane passStack = new StackPane(loginPassField, loginPassText);
        loginShowPass = new CheckBox("Hiện mật khẩu");
        loginShowPass.getStyleClass().add("checkbox-link");
        loginShowPass.selectedProperty().addListener((obs, old, newVal) -> {
            loginPassText.setVisible(newVal);
            loginPassText.setManaged(newVal);
            loginPassField.setVisible(!newVal);
            loginPassField.setManaged(!newVal);
        });
        // Nút đăng nhập
        Button btnLogin = new Button("ĐĂNG NHẬP");
        btnLogin.getStyleClass().add("btn-gradient");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(e -> {
            String u = loginUserField.getText().trim();
            String p = loginPassField.getText();
            // Điều kiện kiểm tra hợp lệ đăng nhập
            if (u.isEmpty() || p.isEmpty()) {
                showError("Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            // Thực hiện đăng nhập nếu thành công
            if (AuthService.login(u, p)) {
                onLoginSuccess.run();
            } else {
                showError("Sai tên đăng nhập hoặc mật khẩu!");
            }
        });
        // Link chuyển sang đăng ký
        Label lblSwitch = new Label("Chưa có tài khoản? Đăng ký ngay");
        lblSwitch.getStyleClass().add("link-text");
        lblSwitch.setOnMouseClicked(e -> switchToRegister());

        StackPane linkWrapper = new StackPane(lblSwitch);
        linkWrapper.getStyleClass().add("link-wrapper"); // Alignment sang CSS
        // tạo khung nhập liệu
        loginBox.getChildren().addAll(
                header,
                new Label("Tên đăng nhập"), loginUserField,
                new Label("Mật khẩu"), passStack,
                loginShowPass,
                new Region(),
                btnLogin,
                linkWrapper);
    }

    // Phần đăng ký
    private void initRegisterBox() {
        registerBox = new VBox();
        registerBox.getStyleClass().add("register-box"); // Spacing, padding, alignment sang CSS
        VBox header = createBrandingHeader("TẠO TÀI KHOẢN MỚI", "Đăng ký để bắt đầu sử dụng hệ thống");
        registerUserField = new TextField();
        registerUserField.getStyleClass().add("matte-input");
        registerUserField.setPromptText("Tên đăng nhập");
        // Trường mật khẩu
        registerPassField = new PasswordField();
        registerPassText = new TextField();
        registerPassField.getStyleClass().add("matte-input");
        registerPassText.getStyleClass().add("matte-input");
        registerPassField.setPromptText("Mật khẩu");
        registerPassText.setPromptText("Mật khẩu");
        registerPassText.textProperty().bindBidirectional(registerPassField.textProperty());
        registerPassText.setVisible(false);
        registerPassText.setManaged(false);
        StackPane passStack = new StackPane(registerPassField, registerPassText);
        // Trường xác nhận mật khẩu
        confirmPassField = new PasswordField();
        confirmPassText = new TextField();
        confirmPassField.getStyleClass().add("matte-input");
        confirmPassText.getStyleClass().add("matte-input");
        confirmPassField.setPromptText("Nhập lại mật khẩu");
        confirmPassText.setPromptText("Nhập lại mật khẩu");
        confirmPassText.textProperty().bindBidirectional(confirmPassField.textProperty());
        confirmPassText.setVisible(false);
        confirmPassText.setManaged(false);

        StackPane confirmStack = new StackPane(confirmPassField, confirmPassText);
        // Checkbox hiện/ẩn mật khẩu
        registerShowPass = new CheckBox("Hiện mật khẩu");
        registerShowPass.getStyleClass().add("checkbox-link");
        registerShowPass.selectedProperty().addListener((obs, old, newVal) -> {
            registerPassText.setVisible(newVal);
            registerPassText.setManaged(newVal);
            registerPassField.setVisible(!newVal);
            registerPassField.setManaged(!newVal);
            // Xác nhận mật khẩu
            confirmPassText.setVisible(newVal);
            confirmPassText.setManaged(newVal);
            confirmPassField.setVisible(!newVal);
            confirmPassField.setManaged(!newVal);
        });
        // Nút đăng ký
        Button btnRegister = new Button("ĐĂNG KÝ");
        btnRegister.getStyleClass().add("btn-gradient");
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        btnRegister.setOnAction(e -> {
            String u = registerUserField.getText().trim();
            String p = registerPassField.getText();
            String c = confirmPassField.getText();
            // Kiểm tra hợp lệ

            if (u.isEmpty() || p.isEmpty()) {
                showError("Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            if (!p.equals(c)) {
                showError("Mật khẩu xác nhận không khớp!");
                return;
            }
            // Thực hiện đăng ký
            if (AuthService.register(u, p)) {
                new Alert(Alert.AlertType.INFORMATION, "Đăng ký thành công! Hãy đăng nhập.").show();
                switchToLogin();
                registerUserField.clear();
                registerPassField.clear();
                confirmPassField.clear();
            } else {
                showError("Tên đăng nhập đã tồn tại!");
            }
        });
        // Link chuyển sang đăng nhập
        Label lblSwitch = new Label("Đã có tài khoản? Đăng nhập ngay");
        lblSwitch.getStyleClass().add("link-text");
        lblSwitch.setOnMouseClicked(e -> switchToLogin());
        // Wrapper để canh giữa
        StackPane linkWrapper = new StackPane(lblSwitch);
        linkWrapper.getStyleClass().add("link-wrapper"); // Alignment sang CSS
        // Thêm tất cả vào registerBox
        registerBox.getChildren().addAll(
                header,
                new Label("Tên đăng nhập"), registerUserField,
                new Label("Mật khẩu"), passStack,
                new Label("Xác nhận mật khẩu"), confirmStack,
                registerShowPass,
                new Region(),
                btnRegister,
                linkWrapper);
    }

    // Chuyển giao diện đăng ký
    private void switchToRegister() {
        loginBox.setVisible(false);
        registerBox.setVisible(true);
    }

    // Chuyển giao diện đang nhập
    private void switchToLogin() {
        registerBox.setVisible(false);
        loginBox.setVisible(true);
    }

    // Hiển thị hộp thoại thông báo lỗi
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
