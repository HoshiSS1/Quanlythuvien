package com.example.demo5.View;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public abstract class BaseEntityView<T> extends VBox {
    protected TableView<T> table;
    protected FilteredList<T> filteredData;
    protected ObservableList<T> dataList;

    public BaseEntityView(String titleText, ObservableList<T> data) {
        this.dataList = data;
        this.getStyleClass().add("view-container");
        this.setPadding(new Insets(10, 0, 0, 0));
        this.setSpacing(15);
        this.setFillWidth(true);
        VBox.setVgrow(this, Priority.ALWAYS);

        filteredData = new FilteredList<>(dataList, p -> true);

        HBox toolbar = createToolbar();
        table = new TableView<>();
        table.getStyleClass().add("modern-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(table, Priority.ALWAYS);

        setupTableColumns();

        SortedList<T> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        this.getChildren().addAll(toolbar, table);
    }
// Tạo thanh công cụ với các nút và ô tìm kiếm
    protected HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");  // Thêm class toolbar

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Tìm kiếm...");
        txtSearch.getStyleClass().add("search-field");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        txtSearch.textProperty().addListener((obs, old, val) -> {
            filterData(val.toLowerCase().trim());
        });


        Button btnAdd = createButton("Thêm", "➕", "btn-add");
        btnAdd.setOnAction(e -> showEditDialog(null));

        Button btnEdit = createButton("Sửa", "✏", "btn-edit");
        btnEdit.setOnAction(e -> {
            T selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showEditDialog(selected);
            else showAlert("Thông báo", "Vui lòng chọn dòng!", Alert.AlertType.WARNING);
        });

        Button btnDelete = createButton("Xóa", "🗑", "btn-delete");
        btnDelete.setOnAction(e -> handleDelete());

        toolbar.getChildren().addAll(txtSearch, btnAdd, btnEdit, btnDelete);
        return toolbar;
    }
// Tạo nút với văn bản, biểu tượng và lớp kiểu dáng
    protected Button createButton(String text, String icon, String styleClass) {
        Button btn = new Button(icon + " " + text);
        btn.getStyleClass().addAll("btn-action", styleClass);
        return btn;
    }
// Hiển thị hộp thoại cảnh báo hoặc thông tin
    protected void showAlert(String t, String c, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(t);
        alert.setHeaderText(null);
        alert.setContentText(c);
        alert.showAndWait();
    }
// Hiển thị hộp thoại xác nhận và trả về kết quả
    protected boolean confirm(String t, String c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(t);
        alert.setHeaderText(null);
        alert.setContentText(c);
        return alert.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
    }

    // Phương thức mới để refresh dữ liệu
    protected void refreshData() {
        table.refresh();
    }

    // Phương thức để cập nhật dữ liệu trong bảng sau khi thay đổi
    protected void updateTableData(ObservableList<T> newData) {
        this.dataList = newData;
        this.filteredData = new FilteredList<>(dataList, p -> true);
        SortedList<T> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);
    }
// Phương thức trừu tượng để các lớp con triển khai
    protected abstract void setupTableColumns();
    protected abstract void filterData(String query);
    protected abstract void showEditDialog(T existing);
    protected abstract void handleDelete();
}