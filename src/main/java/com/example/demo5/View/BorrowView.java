package com.example.demo5.View;

import com.example.demo5.Model.Book;
import com.example.demo5.Model.Loan;
import com.example.demo5.Model.Reader;
import com.example.demo5.Service.BookService;
import com.example.demo5.Service.EmailService;
import com.example.demo5.Service.LibraryService;
import com.example.demo5.Service.ReaderService;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.*;
import java.time.LocalDate;

public class BorrowView extends BaseEntityView<Loan> {

    private final EmailService emailService = new EmailService();

    public BorrowView() {
        super("QUẢN LÝ MƯỢN TRẢ", LibraryService.getAllLoans());
    }

    // ========== TOOLBAR ==========
    @Override
    protected HBox createToolbar() {
        HBox toolbar = super.createToolbar();

        Button btnReturn = createButton("Trả", "↩", "btn-return");
        btnReturn.setOnAction(e -> handleReturnBook());

        Button btnOverdue = createButton("Quá Hạn", "⚠", "btn-delete");
        btnOverdue.setOnAction(e -> filteredData.setPredicate(l -> l.isOverdue() && !l.isReturned()));

        Button btnAll = createButton("Tất Cả", "📋", "btn-add");
        btnAll.setOnAction(e -> filteredData.setPredicate(l -> true));

        // ✉ NÚT GỬI EMAIL QUÁ HẠN — Style giống btn-export (teal)
        Button btnSendEmail = new Button("✉ Gửi Email Quá Hạn");
        btnSendEmail.getStyleClass().addAll("btn-action", "btn-email-overdue");
        btnSendEmail.setOnAction(e -> handleSendOverdueEmails());

        toolbar.getChildren().addAll(
                btnReturn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                btnOverdue, btnAll,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                btnSendEmail
        );
        return toolbar;
    }

    // ========== CỘT BẢNG ==========
    @Override
    protected void setupTableColumns() {
        TableColumn<Loan, Number> colSTT = new TableColumn<>("STT");
        colSTT.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(table.getItems().indexOf(d.getValue()) + 1));
        colSTT.getStyleClass().add("stt-column");

        TableColumn<Loan, String> colId = new TableColumn<>("Mã Phiếu");
        colId.setCellValueFactory(d -> d.getValue().idProperty());

        TableColumn<Loan, String> colReader = new TableColumn<>("Bạn Đọc");
        colReader.setCellValueFactory(d -> d.getValue().readerNameProperty());

        TableColumn<Loan, String> colBook = new TableColumn<>("Sách");
        colBook.setCellValueFactory(d -> d.getValue().bookTitleProperty());

        TableColumn<Loan, LocalDate> colDate = new TableColumn<>("Ngày Mượn");
        colDate.setCellValueFactory(d -> d.getValue().borrowDateProperty());

        TableColumn<Loan, LocalDate> colReturnDate = new TableColumn<>("Ngày Hẹn Trả");
        colReturnDate.setCellValueFactory(d -> d.getValue().returnDateProperty());

        TableColumn<Loan, String> colRemaining = new TableColumn<>("Ngày Còn Lại");
        colRemaining.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOverdueMessage()));
        colRemaining.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Loan loan = getTableRow().getItem();
                    if (loan != null && loan.isOverdue() && !loan.isReturned()) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        TableColumn<Loan, String> colAction = new TableColumn<>("Hành Động");
        colAction.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActionSuggestion()));
        colAction.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    Loan loan = getTableRow().getItem();
                    if (loan != null && loan.isOverdue() && !loan.isReturned()) {
                        getStyleClass().add("action-warning");
                    } else {
                        getStyleClass().add("action-label");
                    }
                }
            }
        });

        TableColumn<Loan, String> colStatus = new TableColumn<>("Trạng Thái");
        colStatus.setCellValueFactory(d -> d.getValue().statusProperty());
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getTableRow() == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Loan loan = getTableRow().getItem();
                if (loan == null) return;

                String text = item;
                if (loan.isOverdue() && !loan.isReturned()) text = "QUÁ HẠN";

                Label badge = new Label(text);
                badge.getStyleClass().addAll("badge", loan.getStatusBadgeClass());
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(120);

                setText(null);
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        table.getColumns().addAll(colSTT, colId, colReader, colBook, colDate, colReturnDate, colRemaining, colStatus, colAction);

        colSTT.setStyle("-fx-alignment: CENTER;");
        colStatus.setStyle("-fx-alignment: CENTER;");
        colReturnDate.setStyle("-fx-alignment: CENTER;");
        colRemaining.setStyle("-fx-alignment: CENTER;");
    }

    // ========== LỌC DỮ LIỆU ==========
    @Override
    protected void filterData(String query) {
        if (query == null || query.isEmpty()) {
            filteredData.setPredicate(l -> true);
        } else {
            String low = query.toLowerCase();
            filteredData.setPredicate(l ->
                    l.getId().toLowerCase().contains(low) ||
                    l.getReaderName().toLowerCase().contains(low) ||
                    l.getBookTitle().toLowerCase().contains(low) ||
                    l.getStatus().toLowerCase().contains(low) ||
                    String.valueOf(l.getQuantity()).contains(low));
        }
    }

    // ========== DIALOG MƯỢN / SỬA ==========
    @Override
    protected void showEditDialog(Loan existing) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Mượn Sách" : "Sửa Phiếu Mượn");
        dialog.setHeaderText(existing == null
                ? "Mỗi bạn đọc chỉ được mượn 1 cuốn cho mỗi đầu sách."
                : "Cập nhật ngày hạn trả cho phiếu " + existing.getId());

        ButtonType btnConfirmType = new ButtonType(existing == null ? "Mượn" : "Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirmType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.getStyleClass().add("dialog-grid");
        grid.setHgap(10);
        grid.setVgap(15);

        ComboBox<Reader> cbReader = new ComboBox<>(ReaderService.getReaders());
        cbReader.setPromptText("Chọn bạn đọc...");
        cbReader.getStyleClass().add("combo-box-wide");

        ComboBox<Book> cbBook = new ComboBox<>(BookService.getBooks());
        cbBook.setPromptText("Chọn sách...");
        cbBook.getStyleClass().add("combo-box-wide");

        Spinner<Integer> spinnerQty = new Spinner<>();
        spinnerQty.setValueFactory(new IntegerSpinnerValueFactory(1, 10, 1));
        spinnerQty.setDisable(true);

        Spinner<Integer> spinnerDays = new Spinner<>();
        spinnerDays.setValueFactory(new IntegerSpinnerValueFactory(-90, 90, 14));
        spinnerDays.setEditable(true);

        Label lblWarning = new Label("");
        lblWarning.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        lblWarning.setWrapText(true);
        lblWarning.setMaxWidth(350);

        Node btnConfirm = dialog.getDialogPane().lookupButton(btnConfirmType);

        if (existing == null) {
            btnConfirm.setDisable(true);
            cbReader.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    boolean hasOverdue = LibraryService.hasOverdueLoan(newVal.getName());
                    if (hasOverdue) {
                        lblWarning.setText("⚠ Chú ý: Bạn đọc này đang có phiếu quá hạn chưa trả, không thể mượn thêm!");
                        btnConfirm.setDisable(true);
                    } else {
                        lblWarning.setText("");
                        btnConfirm.setDisable(false);
                    }
                } else {
                    lblWarning.setText("");
                    btnConfirm.setDisable(true);
                }
            });
        }

        if (existing != null) {
            cbReader.getItems().stream()
                    .filter(r -> r.getName().equals(existing.getReaderName()))
                    .findFirst().ifPresent(cbReader::setValue);
            cbReader.setDisable(true);

            cbBook.getItems().stream()
                    .filter(b -> b.getTitle().equals(existing.getBookTitle()))
                    .findFirst().ifPresent(cbBook::setValue);
            cbBook.setDisable(true);

            spinnerQty.getValueFactory().setValue(existing.getQuantity());

            long currentDays = java.time.temporal.ChronoUnit.DAYS.between(existing.getBorrowDate(), existing.getReturnDate());
            spinnerDays.getValueFactory().setValue((int) currentDays);
        }

        grid.add(new Label("Bạn đọc:"), 0, 0);    grid.add(cbReader, 1, 0);
        grid.add(new Label("Sách:"), 0, 1);         grid.add(cbBook, 1, 1);
        grid.add(new Label("Số lượng:"), 0, 2);     grid.add(spinnerQty, 1, 2);
        grid.add(new Label("Số ngày mượn:"), 0, 3); grid.add(spinnerDays, 1, 3);
        grid.add(lblWarning, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> btn == btnConfirmType);

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            if (cbReader.getValue() != null && cbBook.getValue() != null) {
                if (existing == null) {
                    boolean success = LibraryService.borrowBook(cbReader.getValue(), cbBook.getValue(), spinnerQty.getValue(), spinnerDays.getValue());
                    if (success) {
                        refreshData();
                        showAlert("Thành công", "Đã mượn sách!", AlertType.INFORMATION);
                    } else {
                        showAlert("Lỗi mượn sách", "Bạn đọc đang có nợ quá hạn, đã mượn đầu sách này, hoặc sách đã hết.", AlertType.ERROR);
                    }
                } else {
                    LocalDate newReturnDate = existing.getBorrowDate().plusDays(spinnerDays.getValue());
                    existing.returnDateProperty().set(newReturnDate);
                    if (LibraryService.updateLoan(existing)) {
                        refreshData();
                        if (spinnerDays.getValue() == 0) {
                            showAlert("Cảnh báo", "Phiếu mượn đã hết hạn hôm nay (0 ngày)!", AlertType.WARNING);
                        } else {
                            showAlert("Thành công", "Đã cập nhật phiếu mượn!", AlertType.INFORMATION);
                        }
                    } else {
                        showAlert("Lỗi", "Không thể cập nhật phiếu mượn!", AlertType.ERROR);
                    }
                }
            } else {
                showAlert("Lỗi", "Vui lòng chọn đầy đủ thông tin!", AlertType.ERROR);
            }
        }
    }

    // ========== XÓA ==========
    @Override
    protected void handleDelete() {
        Loan selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn phiếu để xóa!", AlertType.WARNING);
            return;
        }
        if (confirm("Xác nhận", "Xóa phiếu mượn này?")) {
            LibraryService.deleteLoan(selected);
            refreshData();
            showAlert("Thành công", "Đã xóa phiếu mượn!", AlertType.INFORMATION);
        }
    }

    // ========== TRẢ SÁCH ==========
    protected void handleReturnBook() {
        Loan selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn phiếu mượn để trả sách!", AlertType.WARNING);
            return;
        }
        if (selected.isReturned()) {
            showAlert("Thông báo", "Sách này đã được trả rồi!", AlertType.INFORMATION);
            return;
        }
        if (LibraryService.returnBook(selected)) {
            refreshData();
            showAlert("Thành công", "Đã cập nhật trạng thái trả sách!", AlertType.INFORMATION);
        } else {
            showAlert("Lỗi", "Không thể trả sách!", AlertType.ERROR);
        }
    }

    // ========== REFRESH ==========
    @Override
    protected void refreshData() {
        LibraryService.refreshLoans();
        updateTableData(LibraryService.getAllLoans());
    }

    // ========================== GỬI EMAIL THÔNG BÁO QUÁ HẠN ==========================

    private void handleSendOverdueEmails() {
        // Lấy danh sách phiếu quá hạn kèm email
        List<Map<String, String>> overdueList = LibraryService.getOverdueLoansWithEmail();

        if (overdueList.isEmpty()) {
            showAlert("Thông báo", "✅ Hiện không có phiếu quá hạn nào. Không cần gửi thông báo!", AlertType.INFORMATION);
            return;
        }

        // Nhóm theo bạn đọc
        Map<String, List<Map<String, String>>> grouped = new LinkedHashMap<>();
        for (Map<String, String> entry : overdueList) {
            String key = entry.get("readerName");
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
        }

        long readersWithEmail = grouped.values().stream()
                .filter(loans -> {
                    String e = loans.get(0).get("email");
                    return e != null && !e.isBlank();
                }).count();

        // ===== TẠO STAGE GIAO DIỆN MỚI (THAY THẾ DIALOG CŨ) =====
        Stage stage = new Stage();
        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        VBox rootBox = new VBox();
        rootBox.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 25, 0, 0, 10);");

        // --- Header Kéo Thả ---
        HBox header = new HBox();
        header.setPadding(new Insets(20, 25, 20, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #0d9488, #0f766e); -fx-background-radius: 16 16 0 0;");
        
        Label lblHeaderTitle = new Label("💌 Gửi Email Thông Báo Quá Hạn");
        lblHeaderTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: white;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnClose.setOnMouseEntered(e -> btnClose.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 20;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;"));
        btnClose.setOnAction(e -> stage.close());
        
        header.getChildren().addAll(lblHeaderTitle, spacer, btnClose);

        // Kéo thả cửa sổ
        final double[] xOffset = {0};
        final double[] yOffset = {0};
        header.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        header.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset[0]);
            stage.setY(event.getScreenY() - yOffset[0]);
        });

        // --- Thống kê ---
        Label lblTitle = new Label("📋 Tổng quan phiếu quá hạn:");
        lblTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f766e;");

        Label lblStats = new Label(String.format(
                "📌 %d phiếu quá hạn  •  👥 %d bạn đọc  •  📧 %d có email",
                overdueList.size(), grouped.size(), readersWithEmail));
        lblStats.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155; -fx-padding: 4 0 8 0;");

        // --- Bảng tóm tắt theo bạn đọc ---
        javafx.collections.ObservableList<Map<String, String>> summaryData =
                javafx.collections.FXCollections.observableArrayList();

        for (Map.Entry<String, List<Map<String, String>>> g : grouped.entrySet()) {
            List<Map<String, String>> loans = g.getValue();
            Map<String, String> row = new LinkedHashMap<>();
            row.put("readerName", g.getKey());
            row.put("email", loans.get(0).getOrDefault("email", ""));
            row.put("loanCount", String.valueOf(loans.size()));
            String books = loans.stream()
                    .map(l -> l.get("bookTitle"))
                    .reduce((a, b) -> a + ", " + b).orElse("");
            row.put("books", books);
            String maxOverdue = loans.stream()
                    .map(l -> l.get("overdueDays"))
                    .mapToLong(Long::parseLong)
                    .max().orElse(0) + "";
            row.put("maxOverdue", maxOverdue);
            summaryData.add(row);
        }

        TableView<Map<String, String>> tblSummary = new TableView<>(summaryData);
        tblSummary.setPrefHeight(220);
        tblSummary.getStyleClass().add("modern-table");
        tblSummary.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Map<String, String>, String> colReader = new TableColumn<>("Bạn Đọc");
        colReader.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get("readerName")));
        colReader.setPrefWidth(160);
        colReader.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Map<String, String>, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get("email")));
        colEmail.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(item == null ? null : new Label("⚠ Chưa có"));
                    if (getGraphic() != null) getGraphic().setStyle("-fx-text-fill: #e67e22; -fx-font-style: italic;");
                    setText(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().addAll("badge", "badge-teal");
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });
        colEmail.setPrefWidth(180);
        colEmail.setStyle("-fx-alignment: CENTER;");

        TableColumn<Map<String, String>, String> colCount = new TableColumn<>("Số phiếu");
        colCount.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get("loanCount")));
        colCount.setStyle("-fx-alignment: CENTER;");
        colCount.setPrefWidth(85);

        TableColumn<Map<String, String>, String> colBooks = new TableColumn<>("Sách quá hạn");
        colBooks.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get("books")));
        colBooks.setPrefWidth(200);

        TableColumn<Map<String, String>, String> colMax = new TableColumn<>("Ngày quá hạn");
        colMax.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get("maxOverdue") + " ngày"));
        colMax.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().addAll("badge", "badge-qua-han");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        colMax.setPrefWidth(120);
        colMax.setStyle("-fx-alignment: CENTER;");

        tblSummary.getColumns().addAll(colReader, colEmail, colCount, colBooks, colMax);

        // --- Cảnh báo ---
        long noEmailCount = grouped.values().stream()
                .filter(loans -> {
                    String e = loans.get(0).get("email");
                    return e == null || e.isBlank();
                }).count();

        VBox warningBox = new VBox();
        if (noEmailCount > 0) {
            Label lblWarn = new Label("⚠ " + noEmailCount + " bạn đọc chưa có email — hệ thống sẽ tự động bỏ qua.");
            lblWarn.setStyle("-fx-background-color: #fff7ed; -fx-text-fill: #ea580c; -fx-padding: 8 15; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 13px; -fx-border-color: #fed7aa; -fx-border-radius: 8;");
            lblWarn.setMaxWidth(Double.MAX_VALUE);
            warningBox.getChildren().add(lblWarn);
        }

        Label lblNote = new Label("💡 Mỗi bạn đọc chỉ nhận 1 email duy nhất, trong đó liệt kê tất cả sách quá hạn.");
        lblNote.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-font-style: italic; -fx-padding: 5 0;");

        // --- SMTP Config ---
        VBox smtpCard = new VBox(15);
        smtpCard.setPadding(new Insets(20));
        smtpCard.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #e2e8f0;");

        Label lblSmtpTitle = new Label("⚙ Cấu Hình Máy Chủ Gửi (Gmail SMTP)");
        lblSmtpTitle.setStyle("-fx-text-fill: #0f766e; -fx-font-weight: 800; -fx-font-size: 15px;");

        GridPane smtpGrid = new GridPane();
        smtpGrid.setHgap(15);
        smtpGrid.setVgap(12);

        String inputStyle = "-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 12; -fx-font-size: 14px;";

        TextField tfHost = new TextField(emailService.getSmtpHost());
        tfHost.setPrefWidth(220); tfHost.setStyle(inputStyle);
        TextField tfPort = new TextField(String.valueOf(emailService.getSmtpPort()));
        tfPort.setPrefWidth(80); tfPort.setStyle(inputStyle);
        TextField tfEmail = new TextField(emailService.getFromEmail());
        tfEmail.setPromptText("ví dụ: thuviencuato@gmail.com");
        tfEmail.setPrefWidth(300); tfEmail.setStyle(inputStyle);
        PasswordField tfPassword = new PasswordField();
        tfPassword.setText(emailService.getAppPassword());
        tfPassword.setPromptText("Mật khẩu ứng dụng (App Password 16 ký tự)");
        tfPassword.setPrefWidth(300); tfPassword.setStyle(inputStyle);

        Label lHost = new Label("Host:"); lHost.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        Label lPort = new Label("Port:"); lPort.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        Label lEmail = new Label("Email:"); lEmail.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        Label lPass = new Label("Mật khẩu:"); lPass.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");

        smtpGrid.add(lHost, 0, 0);   smtpGrid.add(tfHost, 1, 0);
        smtpGrid.add(lPort, 2, 0);   smtpGrid.add(tfPort, 3, 0);
        smtpGrid.add(lEmail, 0, 1);  smtpGrid.add(tfEmail, 1, 1, 3, 1);
        smtpGrid.add(lPass, 0, 2);   smtpGrid.add(tfPassword, 1, 2, 3, 1);

        smtpCard.getChildren().addAll(lblSmtpTitle, smtpGrid);

        // --- Container Nội dung ---
        VBox content = new VBox(15, lblTitle, lblStats, tblSummary, warningBox, lblNote, smtpCard);
        content.setPadding(new Insets(20, 25, 10, 25));

        // --- Footer Buttons ---
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(15, 25, 20, 25));
        footer.setStyle("-fx-background-color: white; -fx-background-radius: 0 0 16 16;");

        Button btnCancel = new Button("Thoát");
        btnCancel.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 10 25; -fx-font-size: 14px; -fx-cursor: hand;");
        btnCancel.setOnMouseEntered(e -> btnCancel.setStyle("-fx-background-color: #e2e8f0; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-padding: 10 25; -fx-font-size: 14px; -fx-cursor: hand;"));
        btnCancel.setOnMouseExited(e -> btnCancel.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 10 25; -fx-font-size: 14px; -fx-cursor: hand;"));
        btnCancel.setOnAction(e -> stage.close());

        Button btnSend = new Button("🚀 Gửi Email Ngay (" + readersWithEmail + ")");
        if (readersWithEmail == 0) {
            btnSend.setDisable(true);
            btnSend.setStyle("-fx-background-color: #cbd5e1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-font-size: 14px; -fx-background-radius: 8;");
        } else {
            btnSend.setStyle("-fx-background-color: linear-gradient(to right, #0d9488, #0f766e); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(13, 148, 136, 0.4), 10, 0, 0, 4);");
            btnSend.setOnMouseEntered(e -> {
                btnSend.setStyle("-fx-background-color: linear-gradient(to right, #14b8a6, #0d9488); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(13, 148, 136, 0.6), 12, 0, 0, 4);");
                btnSend.setTranslateY(-1);
            });
            btnSend.setOnMouseExited(e -> {
                btnSend.setStyle("-fx-background-color: linear-gradient(to right, #0d9488, #0f766e); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(13, 148, 136, 0.4), 10, 0, 0, 4);");
                btnSend.setTranslateY(0);
            });
        }
        
        btnSend.setOnAction(e -> {
            // Validate SMTP
            String host = tfHost.getText().trim();
            String email = tfEmail.getText().trim();
            String password = tfPassword.getText().trim();
            int port;
            try {
                port = Integer.parseInt(tfPort.getText().trim());
            } catch (NumberFormatException ex) {
                showAlert("Lỗi", "Port phải là số!", AlertType.ERROR);
                return;
            }
            if (email.isBlank() || password.isBlank()) {
                showAlert("Lỗi", "Vui lòng nhập Email và Mật khẩu trong phần Cấu Hình SMTP!", AlertType.ERROR);
                return;
            }

            // Lưu SMTP config
            emailService.saveConfig(host, port, email, password);
            
            stage.close();
            
            // ===== BẮT ĐẦU QUÁ TRÌNH GỬI =====
            Stage progressStage = new Stage();
            progressStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            progressStage.setAlwaysOnTop(true);

            VBox progressBox = new VBox(15);
            progressBox.setAlignment(Pos.CENTER);
            progressBox.setPadding(new Insets(30, 40, 30, 40));
            progressBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-background-radius: 20; -fx-border-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 30, 0, 0, 10);");
            
            Label lblProgress = new Label("⏳ Đang kết nối máy chủ thư...");
            lblProgress.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(450);
            progressBar.setPrefHeight(20);
            progressBar.getStyleClass().add("modern-progress-bar");

            progressBox.getChildren().addAll(lblProgress, progressBar);

            Scene progressScene = new Scene(progressBox);
            progressScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            progressStage.setScene(progressScene);
            progressStage.show();

            // Background thread 
            new Thread(() -> {
                try {
                    Map<String, Object> result = emailService.sendBatchOverdueNotifications(
                        overdueList,
                        (current, total) -> {
                            Platform.runLater(() -> {
                                lblProgress.setText(String.format("✈ Đang gửi email %d / %d...", current, total));
                                progressBar.setProgress((double) current / total);
                            });
                        }
                    );

                    Platform.runLater(() -> {
                        progressStage.close();
                        showEmailResult(result);
                    });
                } catch (Throwable t) {
                    Platform.runLater(() -> {
                        progressStage.close();
                        showAlert("Lỗi Hệ Thống", "Chi tiết lỗi: " + t.getMessage(), AlertType.ERROR);
                    });
                }
            }).start();
        });

        footer.getChildren().addAll(btnCancel, btnSend);

        rootBox.getChildren().addAll(header, content, footer);
        rootBox.setPrefWidth(800);

        Scene scene = new Scene(rootBox);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        if (getClass().getResource("/style.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        }
        stage.setScene(scene);
        stage.showAndWait();
    }

    /** Hiển thị kết quả gửi email */
    @SuppressWarnings("unchecked")
    private void showEmailResult(Map<String, Object> result) {
        int successReaders = (int) result.get("successReaders");
        int failReaders = (int) result.get("failReaders");
        int totalLoans = (int) result.get("totalLoans");
        int totalReaders = (int) result.get("totalReaders");
        List<String> details = (List<String>) result.get("details");

        Stage resultStage = new Stage();
        resultStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        VBox rootBox = new VBox(15);
        rootBox.setPadding(new Insets(25));
        rootBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 5); -fx-border-color: #e2e8f0; -fx-border-width: 1;");

        Label lblTitle = new Label("Kết Quả Gửi Email Thông Báo");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f766e;");

        Label lblHeader = new Label(String.format(
                "Đã gửi email thông báo cho %d/%d bạn đọc (%d phiếu quá hạn).",
                successReaders, totalReaders, totalLoans));
        lblHeader.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");

        // Kết quả tổng quan
        Label lblSummary;
        if (failReaders == 0 && successReaders > 0) {
            lblSummary = new Label("🎉 Tất cả email đã được gửi thành công!");
            lblSummary.getStyleClass().add("stat-card-green");
            lblSummary.setMaxWidth(Double.MAX_VALUE);
            lblSummary.setAlignment(Pos.CENTER);
        } else if (successReaders > 0) {
            lblSummary = new Label(String.format("✅ %d thành công  •  ❌ %d thất bại", successReaders, failReaders));
            lblSummary.getStyleClass().add("stat-card-green"); // Or a warnings style if exists
            lblSummary.setMaxWidth(Double.MAX_VALUE);
            lblSummary.setAlignment(Pos.CENTER);
        } else {
            lblSummary = new Label("❌ Không gửi được email nào. Kiểm tra lại cấu hình SMTP.");
            lblSummary.getStyleClass().add("stat-card-red");
            lblSummary.setMaxWidth(Double.MAX_VALUE);
            lblSummary.setAlignment(Pos.CENTER);
        }

        TextArea taDetails = new TextArea(String.join("\n", details));
        taDetails.setEditable(false);
        taDetails.setWrapText(true);
        taDetails.setPrefHeight(160);
        taDetails.setStyle("-fx-font-size: 13px; -fx-font-family: 'Segoe UI'; -fx-control-inner-background: #f8fafc; -fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");

        Button btnClose = new Button("Đóng");
        btnClose.setStyle("-fx-background-color: #0f766e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 25; -fx-background-radius: 6; -fx-cursor: hand;");
        btnClose.setOnAction(e -> resultStage.close());
        
        HBox btnBox = new HBox(btnClose);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        rootBox.getChildren().addAll(lblTitle, lblHeader, lblSummary, new Label("Chi tiết:"), taDetails, btnBox);

        Scene scene = new Scene(rootBox);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        if (getClass().getResource("/style.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        }
        resultStage.setScene(scene);
        resultStage.showAndWait();
    }
}