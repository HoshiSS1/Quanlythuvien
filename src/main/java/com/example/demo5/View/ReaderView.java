package com.example.demo5.View;

import com.example.demo5.Model.Reader;
import com.example.demo5.Service.ReaderService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class ReaderView extends BaseEntityView<Reader> {

    public ReaderView() {
        super("QUẢN LÝ BẠN ĐỌC", ReaderService.getReaders());  // Pass title và data
    }
// tạo columns bảng bạn đọc
    @Override
    protected void setupTableColumns() {
        TableColumn<Reader, Number> colSTT = new TableColumn<>("STT");
        colSTT.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(table.getItems().indexOf(d.getValue()) + 1));
        colSTT.getStyleClass().add("stt-column");  // Width sang CSS

        TableColumn<Reader, String> colId = new TableColumn<>("Mã SV");
        colId.setCellValueFactory(d -> d.getValue().idProperty());

        TableColumn<Reader, String> colName = new TableColumn<>("Họ Tên");
        colName.setCellValueFactory(d -> d.getValue().nameProperty());

        TableColumn<Reader, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());

        TableColumn<Reader, String> colPhone = new TableColumn<>("SĐT");
        colPhone.setCellValueFactory(d -> d.getValue().phoneProperty());

        table.getColumns().addAll(colSTT, colId, colName, colEmail, colPhone);
    }
// điều kiện lọc bạn đọc
    @Override
    protected void filterData(String query) {
        if (query == null || query.isEmpty()) {
            filteredData.setPredicate(r -> true);
        } else {
            String low = query.toLowerCase();
            filteredData.setPredicate(r -> r.getId().toLowerCase().contains(low) ||
                    r.getName().toLowerCase().contains(low) ||
                    r.getEmail().toLowerCase().contains(low) ||
                    r.getPhone().toLowerCase().contains(low));
        }
    }
// thêm hoặc sửa bạn đọc
    @Override
    protected void showEditDialog(Reader existing) {
        Dialog<Reader> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm Bạn Đọc" : "Sửa Bạn Đọc");
        dialog.setHeaderText(null);
        ButtonType btnTypeSave = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnTypeSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.getStyleClass().add("dialog-grid");  // Hgap, vgap, padding sang CSS

        TextField txtId = new TextField(existing != null ? existing.getId() : "");
        if (existing != null) txtId.setDisable(true);
        TextField txtName = new TextField(existing != null ? existing.getName() : "");
        TextField txtEmail = new TextField(existing != null ? existing.getEmail() : "");
        TextField txtPhone = new TextField(existing != null ? existing.getPhone() : "");

        grid.add(new Label("Mã SV:"), 0, 0); grid.add(txtId, 1, 0);
        grid.add(new Label("Họ Tên:"), 0, 1); grid.add(txtName, 1, 1);
        grid.add(new Label("Email:"), 0, 2); grid.add(txtEmail, 1, 2);
        grid.add(new Label("SĐT:"), 0, 3); grid.add(txtPhone, 1, 3);
// kiểm tra dữ liệu nhập
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == btnTypeSave) {
                String id = txtId.getText().trim();
                String name = txtName.getText().trim();
                String email = txtEmail.getText().trim();
                String phone = txtPhone.getText().trim();
                if (id.isEmpty() || name.isEmpty()) {
                    showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!", AlertType.ERROR);
                    return null;
                }
                if (!phone.matches("\\d{10}")) {
                    showAlert("Lỗi", "SĐT phải 10 số!", AlertType.ERROR);
                    return null;
                }
                return new Reader(id, name, email, phone);
            }
            return null;
        });
// điều kiện sửa hoặc thêm
        Optional<Reader> result = dialog.showAndWait();
        result.ifPresent(r -> {
            boolean success;
            if (existing == null) {
                success = ReaderService.addReader(r);
            } else {
                success = ReaderService.updateReader(existing, r);
            }
            if (success) {
                refreshData();
                showAlert("Thành công", existing == null ? "Đã thêm bạn đọc!" : "Đã sửa bạn đọc!", AlertType.INFORMATION);
            } else {
                showAlert("Lỗi", "Mã SV đã tồn tại! Không thể thêm/sửa.", AlertType.ERROR);
            }
        });
    }
// xóa bạn đọc
    @Override
    protected void handleDelete() {
        Reader selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (confirm("Xác nhận", "Xóa bạn đọc: " + selected.getName() + "?")) {
                ReaderService.deleteReader(selected);
                refreshData();
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn dòng cần xóa!", AlertType.WARNING);
        }
    }
// câp nhật lại dữ liệu từ service
    @Override
    protected void refreshData() {
        ReaderService.refreshReaders();
        updateTableData(ReaderService.getReaders());
    }
}