package com.example.demo5.Model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;

public class Book {
    private final StringProperty id = new SimpleStringProperty(this, "id");
    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final StringProperty author = new SimpleStringProperty(this, "author");
    private final StringProperty category = new SimpleStringProperty(this, "category", "Khác");
    private final IntegerProperty quantity = new SimpleIntegerProperty(this, "quantity");

    // Đổi thành ReadOnlyStringProperty trực tiếp từ binding
    private final StringBinding status;

    public Book(String id, String title, String author, String category, int quantity) {
        this.id.set(id);
        this.title.set(title);
        this.author.set(author);
        this.category.set(category != null && !category.trim().isEmpty() ? category.trim() : "Khác");
        this.quantity.set(quantity);

        // Tạo binding trực tiếp và gán cho status (property read-only)
        this.status = Bindings.createStringBinding(() ->
                        this.quantity.get() > 0 ? "Sẵn sàng" : "Hết hàng",
                this.quantity // dependencies
        );
    }
    public Book() {
        this.status = Bindings.createStringBinding(() ->
                        this.quantity.get() > 0 ? "Sẵn sàng" : "Hết hàng",
                this.quantity // dependencies
        );
    }

    // Properties
    public StringProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty authorProperty() { return author; }
    public StringProperty categoryProperty() { return category; }
    public IntegerProperty quantityProperty() { return quantity; }
    public StringBinding statusProperty() { return status; } // Không cần wrapper nữa

    // Getters
    public String getId() { return id.get(); }
    public String getTitle() { return title.get(); }
    public String getAuthor() { return author.get(); }
    public String getCategory() { return category.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getStatus() { return status.get(); }

    public void setQuantity(int i) {
        this.quantity.set(i);
    }
    @Override
    public String toString() {
        return title.get() + " - " + author.get() + " (Còn: " + quantity.get() + " cuốn)";
    }
}