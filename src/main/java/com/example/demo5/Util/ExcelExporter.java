package com.example.demo5.Util;

import com.example.demo5.Model.Loan;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert; // <--- Đã thêm dòng này để sửa lỗi
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ExcelExporter {

    public static void exportLoans(ObservableList<Loan> loans) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Xuất Danh Sách Mượn Trả Ra CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("File CSV (*.csv)", "*.csv")
        );
        fileChooser.setInitialFileName("DanhSachMuonTra.csv");

        // Dùng Stage rỗng để show dialog (vì đang ở View không có Stage)
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
                // BOM để Excel nhận diện tiếng Việt đúng
                writer.write('\ufeff');

                // Header
                writer.println("Mã Phiếu,Bạn Đọc,Tên Sách,Ngày Mượn,Trạng Thái");

                // Data
                for (Loan loan : loans) {
                    writer.println(String.format("%s,%s,%s,%s,%s",
                            loan.getId(),
                            loan.getReaderName(),
                            loan.getBookTitle(),
                            loan.getBorrowDate() != null ? loan.getBorrowDate().toString() : "",
                            loan.getStatus()
                    ));
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Xuất file CSV thành công!\nĐường dẫn: " + file.getAbsolutePath());
                alert.showAndWait();

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Không thể xuất file: " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }
}