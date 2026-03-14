package com.example.demo5.View;

import com.example.demo5.Model.Book;
import com.example.demo5.Service.BookService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class BookView extends BaseEntityView<Book> {
// Constructor
    public BookView() {
        super("KHO SÁCH", BookService.getBooks());  // Pass title và data
    }
// Table Columns
    @Override
    protected void setupTableColumns() {
        TableColumn<Book, Number> colSTT = new TableColumn<>("STT");
        colSTT.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(table.getItems().indexOf(d.getValue()) + 1));
        colSTT.getStyleClass().add("stt-column");  // Width sang CSS

        TableColumn<Book, String> colId = new TableColumn<>("Mã Sách");
        colId.setCellValueFactory(d -> d.getValue().idProperty());

        TableColumn<Book, String> colTitle = new TableColumn<>("Tên Sách");
        colTitle.setCellValueFactory(d -> d.getValue().titleProperty());

        TableColumn<Book, String> colAuthor = new TableColumn<>("Tác Giả");
        colAuthor.setCellValueFactory(d -> d.getValue().authorProperty());

        TableColumn<Book, String> colCategory = new TableColumn<>("Thể Loại");
        colCategory.setCellValueFactory(d -> d.getValue().categoryProperty());

        TableColumn<Book, Integer> colQuantity = new TableColumn<>("Số Lượng");
        colQuantity.setCellValueFactory(d -> d.getValue().quantityProperty().asObject());

        TableColumn<Book, String> colStatus = new TableColumn<>("Trạng Thái");
        colStatus.setCellValueFactory(d -> d.getValue().statusProperty());

        table.getColumns().addAll(colSTT, colId, colTitle, colAuthor, colCategory, colQuantity, colStatus);
    }
// Search Filter
    @Override
    protected void filterData(String query) {
        if (query == null || query.isEmpty()) {
            filteredData.setPredicate(b -> true);
        } else {
            String low = query.toLowerCase();
            filteredData.setPredicate(b -> b.getId().toLowerCase().contains(low) ||
                    b.getTitle().toLowerCase().contains(low) ||
                    b.getAuthor().toLowerCase().contains(low) ||
                    b.getCategory().toLowerCase().contains(low) ||
                    String.valueOf(b.getQuantity()).contains(low));
        }
    }
// Add/Edit Dialog
    @Override
    protected void showEditDialog(Book existing) {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm Sách" : "Sửa Sách");
        ButtonType btnSaveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtId = new TextField(existing != null ? existing.getId() : "");
        if (existing != null) txtId.setDisable(true);
        TextField txtTitle = new TextField(existing != null ? existing.getTitle() : "");
        TextField txtAuthor = new TextField(existing != null ? existing.getAuthor() : "");
        TextField txtCategory = new TextField(existing != null ? existing.getCategory() : "");
        TextField txtQty = new TextField(existing != null ? String.valueOf(existing.getQuantity()) : "0");

        grid.add(new Label("Mã Sách:"), 0, 0); grid.add(txtId, 1, 0);
        grid.add(new Label("Tên Sách:"), 0, 1); grid.add(txtTitle, 1, 1);
        grid.add(new Label("Tác Giả:"), 0, 2); grid.add(txtAuthor, 1, 2);
        grid.add(new Label("Thể Loại:"), 0, 3); grid.add(txtCategory, 1, 3);
        grid.add(new Label("Số Lượng:"), 0, 4); grid.add(txtQty, 1, 4);

        dialog.getDialogPane().setContent(grid);
        // Xử lý kết quả
        dialog.setResultConverter(d -> {
            if (d == btnSaveType) {
                try {
                    String id = txtId.getText().trim();
                    String title = txtTitle.getText().trim();
                    String author = txtAuthor.getText().trim();
                    String category = txtCategory.getText().trim();
                    int qty = Integer.parseInt(txtQty.getText().trim());
                    if (id.isEmpty() || title.isEmpty() || author.isEmpty()) {
                        showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!", AlertType.ERROR);
                        return null;
                    }
                    return new Book(id, title, author, category, qty);
                } catch (NumberFormatException e) {
                    showAlert("Lỗi", "Số lượng phải là số nguyên!", AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        Optional<Book> result = dialog.showAndWait();
        result.ifPresent(book -> {
            boolean success;
            if (existing == null) {
                success = BookService.addBook(book);
            } else {
                success = BookService.updateBook(existing, book);
            }
            if (success) {
                refreshData();
                showAlert("Thành công", existing == null ? "Đã thêm sách!" : "Đã sửa sách!", AlertType.INFORMATION);
            } else {
                showAlert("Lỗi", "Mã sách đã tồn tại! Không thể thêm/sửa.", AlertType.ERROR);
            }
        });
    }

    @Override
    protected void handleDelete() {
        Book selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (confirm("Xác nhận xóa", "Bạn có chắc muốn xóa sách: " + selected.getTitle() + "?")) {
                BookService.deleteBook(selected);
                refreshData();
                showAlert("Thành công", "Đã xóa sách thành công!", AlertType.INFORMATION);
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn sách cần xóa!", AlertType.WARNING);
        }
    }
// Refresh Data
    @Override
    protected void refreshData() {
        BookService.refreshBooks();
        updateTableData(BookService.getBooks());
    }

}