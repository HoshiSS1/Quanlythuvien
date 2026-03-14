package com.example.demo5.View;

import com.example.demo5.Model.Book;
import com.example.demo5.Model.Loan;
import com.example.demo5.Model.Reader;
import com.example.demo5.Service.BookService;
import com.example.demo5.Service.LibraryService;
import com.example.demo5.Service.ReaderService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class BorrowView extends BaseEntityView<Loan> {

    public BorrowView() {
        super("QUẢN LÝ MƯỢN TRẢ", LibraryService.getAllLoans());
    }
// Tạo nút "Trả" trên thanh công cụ
    @Override
    protected HBox createToolbar() {
        HBox toolbar = super.createToolbar();
        Button btnReturn = createButton("Trả", "↩", "btn-return");
        btnReturn.setOnAction(e -> handleReturnBook());
        toolbar.getChildren().add(btnReturn);
        return toolbar;
    }
// Thiết lập các cột cho bảng hiển thị phiếu mượn
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

        TableColumn<Loan, java.time.LocalDate> colDate = new TableColumn<>("Ngày Mượn");
        colDate.setCellValueFactory(d -> d.getValue().borrowDateProperty());

        TableColumn<Loan, String> colStatus = new TableColumn<>("Trạng Thái");
        colStatus.setCellValueFactory(d -> d.getValue().statusProperty());
        colStatus.setCellFactory(column -> new TableCell<Loan, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                // Cập nhật giao diện của ô dựa trên trạng thái mượn trả
                super.updateItem(item, empty);
                if (empty || item == null || getTableRow() == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Loan loan = getTableRow().getItem();
                if (loan == null) return;

                Label badge = new Label(item);
                badge.getStyleClass().addAll("badge", loan.getStatusBadgeClass());
                badge.setAlignment(javafx.geometry.Pos.CENTER);

                setText(null);
                setGraphic(badge);
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        colStatus.getStyleClass().add("trang-thai-column");

        TableColumn<Loan, Integer> colQuantity = new TableColumn<>("Số Lượng");
        colQuantity.setCellValueFactory(d -> d.getValue().quantityProperty().asObject());

        table.getColumns().addAll(colSTT, colId, colReader, colBook, colDate, colStatus, colQuantity);

        colSTT.setStyle("-fx-alignment: CENTER;");
        colStatus.setStyle("-fx-alignment: CENTER;");
        colQuantity.setStyle("-fx-alignment: CENTER;");
    }

    @Override
    protected void filterData(String query) {
        if (query == null || query.isEmpty()) {
            filteredData.setPredicate(l -> true);
        } else {
            String low = query.toLowerCase();
            filteredData.setPredicate(l -> l.getId().toLowerCase().contains(low) ||
                    l.getReaderName().toLowerCase().contains(low) ||
                    l.getBookTitle().toLowerCase().contains(low) ||
                    l.getStatus().toLowerCase().contains(low) ||
                    String.valueOf(l.getQuantity()).contains(low));
        }
    }

    @Override
    protected void showEditDialog(Loan existing) {
        if (existing != null) {
            showAlert("Thông báo", "Chỉnh sửa phiếu mượn chưa hỗ trợ. Hãy xóa và tạo mới nếu cần.", AlertType.INFORMATION);
            return;
        }

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Mượn Sách");
        dialog.setHeaderText(null);

        ButtonType btnBorrowType = new ButtonType("Mượn", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnBorrowType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.getStyleClass().add("dialog-grid");

        ComboBox<Reader> cbReader = new ComboBox<>(ReaderService.getReaders());
        cbReader.setPromptText("Chọn bạn đọc...");
        cbReader.getStyleClass().add("combo-box-wide");

        ComboBox<Book> cbBook = new ComboBox<>(BookService.getBooks());
        cbBook.setPromptText("Chọn sách...");
        cbBook.getStyleClass().add("combo-box-wide");

        Spinner<Integer> spinnerQty = new Spinner<>();
        spinnerQty.setValueFactory(new IntegerSpinnerValueFactory(1, 10, 1));
        spinnerQty.setEditable(true);

        grid.add(new Label("Bạn đọc:"), 0, 0); grid.add(cbReader, 1, 0);
        grid.add(new Label("Sách:"), 0, 1); grid.add(cbBook, 1, 1);
        grid.add(new Label("Số lượng:"), 0, 2); grid.add(spinnerQty, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> btn == btnBorrowType);

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            if (cbReader.getValue() != null && cbBook.getValue() != null) {
                boolean success = LibraryService.borrowBook(cbReader.getValue(), cbBook.getValue(), spinnerQty.getValue());
                if (success) {
                    refreshData();
                    showAlert("Thành công", "Đã mượn sách!", AlertType.INFORMATION);
                } else {
                    showAlert("Lỗi", "Không đủ sách trong kho hoặc lỗi hệ thống!", AlertType.ERROR);
                }
            } else {
                showAlert("Lỗi", "Vui lòng chọn đầy đủ thông tin!", AlertType.ERROR);
            }
        }
    }

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
// Cập nhật dữ liệu bảng từ dịch vụ thư viện
    @Override
    protected void refreshData() {
        LibraryService.refreshLoans();
        updateTableData(LibraryService.getAllLoans());
    }
}