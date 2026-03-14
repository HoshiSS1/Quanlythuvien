package com.example.demo5.Model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Loan {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty readerName = new SimpleStringProperty();
    private final StringProperty bookTitle = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> borrowDate = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty("ĐANG MƯỢN"); // Mặc định tiếng Việt
    private final IntegerProperty quantity = new SimpleIntegerProperty();

    // Constants for status
    public static final String STATUS_BORROWED = "ĐANG MƯỢN";
    public static final String STATUS_RETURNED = "ĐÃ TRẢ";

    public Loan(String id, String readerName, String bookTitle, LocalDate borrowDate, String status, int quantity) {
        this.id.set(id);
        this.readerName.set(readerName);
        this.bookTitle.set(bookTitle);
        this.borrowDate.set(borrowDate);
        setStatus(status); // Dùng setter để chuẩn hóa
        this.quantity.set(quantity);
    }

    public StringProperty idProperty() { return id; }
    public String getId() { return id.get(); }

    public StringProperty readerNameProperty() { return readerName; }
    public String getReaderName() { return readerName.get(); }

    public StringProperty bookTitleProperty() { return bookTitle; }
    public String getBookTitle() { return bookTitle.get(); }

    public ObjectProperty<LocalDate> borrowDateProperty() { return borrowDate; }
    public LocalDate getBorrowDate() { return borrowDate.get(); }

    public StringProperty statusProperty() { return status; }
    public String getStatus() { return status.get(); }

    public void setStatus(String newStatus) {
        if (newStatus == null) {
            this.status.set(STATUS_BORROWED);
            return;
        }
        // Chuẩn hóa status về tiếng Việt
        String normalized = newStatus.trim().toUpperCase();
        if (normalized.contains("BORROWED") || normalized.contains("MƯỢN")) {
            this.status.set(STATUS_BORROWED);
        } else if (normalized.contains("RETURNED") || normalized.contains("TRẢ")) {
            this.status.set(STATUS_RETURNED);
        } else {
            this.status.set(normalized); // Giữ nguyên nếu khác
        }
    }

    public IntegerProperty quantityProperty() { return quantity; }
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int qty) { quantity.set(qty); }

    public boolean isReturned() {
        return STATUS_RETURNED.equals(getStatus());
    }

    // Phương thức tiện lợi để lấy class badge cho CSS
    public String getStatusBadgeClass() {
        return isReturned() ? "badge-da-tra" : "badge-dang-muon";
    }
}