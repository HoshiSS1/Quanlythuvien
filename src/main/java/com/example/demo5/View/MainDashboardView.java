package com.example.demo5.View;
import com.example.demo5.Model.Book;
import com.example.demo5.Model.Loan;
import com.example.demo5.Model.Reader;
import com.example.demo5.Service.BookService;
import com.example.demo5.Service.LibraryService;
import com.example.demo5.Service.ReaderService;
import javafx.collections.FXCollections;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MainDashboardView extends StackPane {
    private final Runnable onLogout;
    private Button activeButton;
    private VBox mainContentArea;
    private Label lblPageTitle;
    // enum : xác định các view khác nhau
    private enum ViewType { DASHBOARD, BOOKS, READERS, LOANS }
    private ViewType currentView = ViewType.DASHBOARD;

    private final BookView bookView = new BookView();
    private final ReaderView readerView = new ReaderView();
    private final BorrowView borrowView = new BorrowView();
    // Constructor
    public MainDashboardView(Runnable onLogout) {
        this.onLogout = onLogout;

        // QUAN TRỌNG: Gán class này để CSS Dashboard hoạt động
        this.getStyleClass().clear();
        this.getStyleClass().add("dashboard-root");

        var cssUrl = getClass().getResource("/style.css");
        if (cssUrl != null) getStylesheets().add(cssUrl.toExternalForm());

        buildUI();
    }

    // Trong phương thức buildUI() : Xây dựng giao diện chính
    private void buildUI() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // 1. SIDEBAR (Nơi chứa menu và các nút chức năng chính)
        root.setLeft(createSidebar());

        // 2. PHẦN CHÍNH
        VBox rightSection = new VBox();
        rightSection.getChildren().add(createTopBar());
// Khu vực nội dung chính có thể cuộn
        mainContentArea = new VBox();
        mainContentArea.getStyleClass().add("main-content-area");  // Spacing, padding sang CSS
// Đặt trong ScrollPane để cuộn khi nội dung vượt quá khung nhìn
        ScrollPane scrollPane = new ScrollPane(mainContentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-scroll-pane");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
// Thêm ScrollPane vào phần phải
        rightSection.getChildren().add(scrollPane);
        root.setCenter(rightSection);
// Hiển thị dashboard mặc định
        showDashboard();
        getChildren().add(root);
    }
    // Trong phương thức createSidebar() : Tạo thanh bên (sidebar)
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");  // Spacing, width, padding sang CSS
// --- KHU VỰC LOGO ---
        VBox logoBox = new VBox();
        logoBox.getStyleClass().add("logo-box");  // Spacing, alignment, padding sang CSS
        Label brandIcon = new Label("📖");
        brandIcon.getStyleClass().add("logo-icon");
        Label brandName = new Label("LMS PRESTIGE");
        brandName.getStyleClass().add("logo-text");
        logoBox.getChildren().addAll(brandIcon, brandName); // Thêm logo và tên thương hiệu
        // --- KHU VỰC MENU CHÍNH ---
        Button btnHome = createMenuButton("🏠  Tổng Quan", ViewType.DASHBOARD);
        Button btnBooks = createMenuButton("📚  Quản Lý Sách", ViewType.BOOKS);
        Button btnReaders = createMenuButton("👥  Bạn Đọc", ViewType.READERS);
        Button btnLoans = createMenuButton("📑  Mượn Trả", ViewType.LOANS);
        // --- KHU VỰC GIỮA GIỮA SIDEBAR ---
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        // --- KHU VỰC CHỨC NĂNG DƯỚI CÙNG SIDEBAR ---
        VBox bottomActions = new VBox();
        bottomActions.getStyleClass().add("bottom-actions");  // Spacing, padding sang CSS
        // Nút xuất báo cáo CSV
        Button btnExport = new Button("📄 Xuất Báo Cáo CSV");
        btnExport.getStyleClass().add("btn-export"); // Dùng class xanh lá trong CSS
        btnExport.setMaxWidth(Double.MAX_VALUE);
        btnExport.setOnAction(e -> exportReport());
        // Nút đăng xuất
        Button btnLogout = new Button("Đăng Xuất");
        btnLogout.getStyleClass().add("btn-logout-sidebar");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setOnAction(e -> onLogout.run());
        // Thêm nút vào khu vực chức năng dưới cùng
        bottomActions.getChildren().addAll(btnExport, btnLogout);
        sidebar.getChildren().addAll(logoBox, btnHome, btnBooks, btnReaders, btnLoans, spacer, bottomActions);

        activeButton = btnHome;
        activeButton.getStyleClass().add("menu-active");

        return sidebar;
    }
    // Trong phương thức createMenuButton()
    private Button createMenuButton(String text, ViewType type) {
        Button btn = new Button(text);
        btn.getStyleClass().add("menu-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        // Xử lý sự kiện khi nhấn nút menu
        btn.setOnAction(e -> {
            if (activeButton != null) activeButton.getStyleClass().remove("menu-active");
            btn.getStyleClass().add("menu-active");
            activeButton = btn;
// Chuyển view tương ứng
            switch (type) {
                case DASHBOARD -> showDashboard();
                case BOOKS     -> showBookView();
                case READERS   -> showReaderView();
                case LOANS     -> showBorrowView();
            }
        });
        return btn;
    }
    // Hiển thị các view tương ứng
    private void showBookView() {
        currentView = ViewType.BOOKS;
        lblPageTitle.setText("QUẢN LÝ KHO SÁCH");
        mainContentArea.getChildren().setAll(bookView);
    }
    // Hiển thị các view tương ứng
    private void showReaderView() {
        currentView = ViewType.READERS;
        lblPageTitle.setText("QUẢN LÝ BẠN ĐỌC");
        mainContentArea.getChildren().setAll(readerView);
    }
    // Hiển thị các view tương ứng
    private void showBorrowView() {
        currentView = ViewType.LOANS;
        lblPageTitle.setText("QUẢN LÝ MƯỢN TRẢ");
        mainContentArea.getChildren().setAll(borrowView);
    }
    // Tạo thanh top bar
    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");  // Spacing, alignment, padding sang CSS
// Tiêu đề trang
        lblPageTitle = new Label("Tổng Quan Hệ Thống");
        lblPageTitle.getStyleClass().add("page-title");
// Thêm tiêu đề trang vào thanh trên cùng
        topBar.getChildren().add(lblPageTitle);
        return topBar;
    }
    // Hiển thị view với tiêu đề và nội dung cụ thể
    private void showView(String title, Node view) {
        lblPageTitle.setText(title);
        mainContentArea.getChildren().setAll(view);
    }
    // Hiển thị dashboard với các thống kê và biểu đồ
    private void showDashboard() {
        lblPageTitle.setText("Tổng Quan Hệ Thống");
        mainContentArea.getChildren().clear();
// Lấy dữ liệu thống kê
        var books = BookService.getBooks();
        var readers = ReaderService.getReaders();
        var loans = LibraryService.getAllLoans();
// Tính toán các con số thống kê
        long totalBooks = books.stream().mapToLong(Book::getQuantity).sum();
        long uniqueTitles = books.size();
        long borrowed = loans.stream().filter(l -> !l.isReturned()).count();
        long totalReaders = readers.size();
// Tạo lưới thẻ thống kê
        GridPane grid = new GridPane();
        grid.getStyleClass().add("stat-grid");  // Hgap, vgap sang CSS
        grid.add(createStatCard("Tổng Số Cuốn Sách", String.valueOf(totalBooks), "stat-blue"), 0, 0);
        grid.add(createStatCard("Đầu Sách Khác Nhau", String.valueOf(uniqueTitles), "stat-green"), 1, 0);
        grid.add(createStatCard("Sách Đang Được Mượn", String.valueOf(borrowed), "stat-red"), 2, 0);
        grid.add(createStatCard("Tổng Số Bạn Đọc", String.valueOf(totalReaders), "stat-orange"), 3, 0);
// Tạo container cho biểu đồ
        HBox chartsContainer = new HBox();
        chartsContainer.getStyleClass().add("charts-container");  // Spacing, alignment sang CSS
// Tạo và thêm biểu đồ vào container
        VBox pieBox = createChartWrapper(createPieChart(books, loans), "Tình Trạng Kho Sách");
        VBox barBox = createChartWrapper(createBarChart(books), "Phân Loại Theo Thể Loại");
        HBox.setHgrow(pieBox, Priority.ALWAYS);
        HBox.setHgrow(barBox, Priority.ALWAYS);
        chartsContainer.getChildren().addAll(pieBox, barBox);
        // Thêm lưới và biểu đồ vào khu vực chính
        mainContentArea.getChildren().addAll(grid, chartsContainer);
    }
    // Tạo thẻ thống kê
    private VBox createStatCard(String title, String value, String styleClass) {
        VBox card = new VBox();
        card.getStyleClass().addAll("stat-card", styleClass);  // Spacing sang CSS
        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("stat-value");
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("stat-label");
        card.getChildren().addAll(lblValue, lblTitle);
        return card;
    }
    // Tạo wrapper cho biểu đồ với tiêu đề
    private VBox createChartWrapper(Chart chart, String title) {
        VBox wrapper = new VBox();
        wrapper.getStyleClass().add("dashboard-card");  // Spacing sang CSS
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("chart-title-text");
        wrapper.getChildren().addAll(lblTitle, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        return wrapper;
    }
    // Tạo biểu đồ tròn
    private PieChart createPieChart(java.util.List<Book> books, java.util.List<Loan> loans) {
        long inStock = books.stream().mapToLong(Book::getQuantity).sum();
        long borrowed = loans.stream().filter(l -> !l.isReturned()).count();
// Giả sử mỗi phiếu mượn đại diện cho 1 cuốn sách đã mượn
        PieChart pie = new PieChart(FXCollections.observableArrayList(
                new PieChart.Data("Trong Kho", inStock),
                new PieChart.Data("Đang Mượn", borrowed)
        ));
        pie.setLegendSide(Side.BOTTOM);
        pie.setLabelsVisible(true);
        return pie;
    }
    // Tạo biểu đồ cột
    private BarChart<String, Number> createBarChart(java.util.List<Book> books) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bar = new BarChart<>(xAxis, yAxis);
        bar.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Integer> catMap = new HashMap<>();
        books.forEach(b -> catMap.put(b.getCategory(), catMap.getOrDefault(b.getCategory(), 0) + b.getQuantity()));
        catMap.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));

        bar.getData().add(series);
        return bar;
    }
    // Xuất báo cáo hệ thống ra file CSV
    private void exportReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo Cáo Hệ Thống");
        fileChooser.setInitialFileName("BaoCao_" + currentView + "_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
// Mở hộp thoại lưu file
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {

                osw.write('\ufeff');
                StringBuilder sb = new StringBuilder();
// Viết tiêu đề và dữ liệu tương ứng
                switch (currentView) {
                    case BOOKS -> {
                        sb.append("Mã Sách,Tên Sách,Tác Giả,Thể Loại,Số Lượng\n");
                        for (Book b : BookService.getBooks())
                            sb.append(String.format("%s,%s,%s,%s,%d\n", b.getId(), b.getTitle(), b.getAuthor(), b.getCategory(), b.getQuantity()));
                    }
                    case READERS -> {
                        sb.append("Mã SV,Họ Tên,Email,Số Điện Thoại\n");
                        for (Reader r : ReaderService.getReaders())
                            sb.append(String.format("%s,%s,%s,%s\n", r.getId(), r.getName(), r.getEmail(), r.getPhone()));
                    }
                    case LOANS -> {
                        sb.append("Mã Phiếu,Bạn Đọc,Sách,Ngày Mượn,Trạng Thái\n");
                        for (Loan l : LibraryService.getAllLoans())
                            sb.append(String.format("%s,%s,%s,%s,%s\n", l.getId(), l.getReaderName(), l.getBookTitle(), l.getBorrowDate(), l.getStatus()));
                    }
                    default -> sb.append("Dữ liệu tổng quan không hỗ trợ xuất CSV chi tiết.\n");
                }
// Ghi dữ liệu vào file
                osw.write(sb.toString());
                new Alert(Alert.AlertType.INFORMATION, "Xuất báo cáo thành công!").show();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Lỗi xuất file: " + e.getMessage()).show();
            }
        }
    }

}