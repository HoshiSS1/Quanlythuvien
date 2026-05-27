<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/JavaFX-21.0.6-007396?style=for-the-badge&logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/SQL%20Server-2019+-CC2927?style=for-the-badge&logo=microsoft-sql-server&logoColor=white" />
  <img src="https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Status-Production-brightgreen?style=for-the-badge" />
</p>

<h1 align="center">📖 LMS Prestige</h1>
<h3 align="center">Hệ Thống Quản Lý Thư Viện Chuyên Nghiệp</h3>

<p align="center">
  <em>Desktop application xây dựng trên JavaFX 21 với kiến trúc Layered MVC, tích hợp SQL Server, HikariCP connection pooling và hệ thống gửi email thông báo tự động.</em>
</p>

---

## 📑 Mục Lục

- [Giới Thiệu](#-giới-thiệu)
- [Features](#-features)
- [System Architecture](#️-system-architecture)
- [Tech Stack](#️-tech-stack)
- [Folder Structure](#-folder-structure)
- [Installation](#️-installation)
- [Environment Variables](#-environment-variables)
- [Running Project](#️-running-project)
- [Database Schema](#-database-schema)
- [Testing](#-testing)
- [Performance & Scalability](#-performance--scalability)
- [Security](#-security)
- [Deployment](#-deployment)
- [Monitoring & Logging](#-monitoring--logging)
- [Contributing](#-contributing)
- [License](#-license)
- [Author](#-author)

---

## 📌 Giới Thiệu

**LMS Prestige** là hệ thống quản lý thư viện dạng desktop application, được phát triển với mục tiêu:

- **Số hóa** toàn bộ quy trình quản lý mượn trả sách tại thư viện
- **Tự động hóa** thông báo quá hạn qua email (Gmail SMTP)
- **Trực quan hóa** dữ liệu thống kê qua dashboard với biểu đồ real-time
- **Bảo mật** dữ liệu người dùng với mã hóa PBKDF2-HMAC-SHA256

### Đối tượng sử dụng

| Vai trò | Mô tả |
|---------|-------|
| **Thủ thư / Admin** | Quản lý sách, bạn đọc, phiếu mượn trả, gửi thông báo email |
| **Quản lý thư viện** | Xem thống kê tổng quan, xuất báo cáo CSV |

---

## 🚀 Features

### Core Features
| Tính năng | Mô tả | Trạng thái |
|-----------|--------|:----------:|
| 🔐 Authentication | Đăng nhập / Đăng ký với mã hóa PBKDF2 | ✅ |
| 📚 Quản lý kho sách | CRUD sách (mã, tên, tác giả, thể loại, số lượng) | ✅ |
| 👥 Quản lý bạn đọc | CRUD bạn đọc (mã SV, họ tên, email, SĐT) | ✅ |
| 📑 Quản lý mượn trả | Mượn / Trả / Gia hạn / Xóa phiếu với transaction | ✅ |
| 📊 Dashboard | Thống kê tổng quan với PieChart + BarChart | ✅ |
| ⚠️ Phát hiện quá hạn | Tự động tính toán và badge trạng thái | ✅ |
| ✉️ Email thông báo | Gửi email hàng loạt cho bạn đọc quá hạn (Gmail SMTP) | ✅ |
| 📄 Xuất báo cáo CSV | Export sách / bạn đọc / phiếu mượn ra file CSV (UTF-8 + BOM) | ✅ |
| 🔍 Tìm kiếm real-time | Lọc dữ liệu tức thì trên tất cả bảng | ✅ |
| 🎨 Modern UI | Giao diện Soft Tech Professional với CSS tùy chỉnh 546 dòng | ✅ |

### Chi tiết nổi bật

- **Overdue Prevention**: Hệ thống tự động chặn mượn sách mới nếu bạn đọc đang có phiếu quá hạn
- **Duplicate Prevention**: Mỗi bạn đọc chỉ được mượn 1 cuốn cho mỗi đầu sách
- **Stock Management**: Tự động trừ/cộng số lượng kho khi mượn/trả, sử dụng database transaction
- **Grouped Email**: Nhóm nhiều phiếu quá hạn của cùng bạn đọc thành 1 email duy nhất
- **BCC Admin**: Tự động gửi bản sao ẩn về hộp thư admin để kiểm tra
- **Tracking Pixel**: Nhúng pixel tracking trong email HTML
- **Async Loading**: Dashboard tải dữ liệu bất đồng bộ, không đóng băng UI

---

## 🏗️ System Architecture

### Kiến trúc tổng thể: Layered MVC

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐  │
│  │LoginView │ │ BookView │ │ReaderView│ │  BorrowView   │  │
│  └──────────┘ └──────────┘ └──────────┘ └───────────────┘  │
│  ┌────────────────┐  ┌────────────────────────────────────┐ │
│  │BaseEntityView  │  │     MainDashboardView (Charts)     │ │
│  │  (Abstract)    │  │     PieChart + BarChart            │ │
│  └────────────────┘  └────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                      SERVICE LAYER                           │
│  ┌────────────┐ ┌────────────┐ ┌──────────────┐            │
│  │AuthService │ │BookService │ │ReaderService │            │
│  │ (PBKDF2)   │ │  (CRUD)    │ │   (CRUD)     │            │
│  └────────────┘ └────────────┘ └──────────────┘            │
│  ┌────────────────┐  ┌──────────────────────────┐          │
│  │LibraryService  │  │    EmailService           │          │
│  │(Borrow/Return) │  │  (Jakarta Mail + SMTP)    │          │
│  │(Transactions)  │  │  (Batch + Grouped Send)   │          │
│  └────────────────┘  └──────────────────────────┘          │
├─────────────────────────────────────────────────────────────┤
│                      CONFIG LAYER                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │          DatabaseManager (HikariCP Pool)              │  │
│  │          MaxPool=10 | MinIdle=2 | Async Init          │  │
│  └──────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                      DATA LAYER                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │     Microsoft SQL Server (LibraryDB)                  │  │
│  │     Tables: Users, Books, Readers, Loans              │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Flow chính

```
User → LoginView → AuthService (PBKDF2 verify)
         ↓ success
     MainDashboardView
         ├── Dashboard (Stats + Charts) ← BookService, LibraryService, ReaderService
         ├── BookView     ← BookService     ← DatabaseManager ← SQL Server
         ├── ReaderView   ← ReaderService   ← DatabaseManager ← SQL Server
         └── BorrowView   ← LibraryService  ← DatabaseManager ← SQL Server
                           └── EmailService  ← Jakarta Mail    ← Gmail SMTP
```

### Design Patterns áp dụng

| Pattern | Áp dụng |
|---------|---------|
| **MVC** | View (JavaFX) → Service → Model |
| **Template Method** | `BaseEntityView<T>` abstract class cho BookView, ReaderView, BorrowView |
| **Singleton** | `DatabaseManager` (static HikariDataSource) |
| **Observer** | JavaFX `ObservableList` + `FilteredList` + `SortedList` cho reactive UI |
| **Strategy** | `filterData()` abstract method cho từng View |
| **Factory** | `createMenuButton()`, `createStatCard()` trong Dashboard |

---

## 🛠️ Tech Stack

| Layer | Technology | Version | Mô tả |
|-------|-----------|---------|--------|
| **Language** | Java | 21 (LTS) | OpenJDK 21 |
| **UI Framework** | JavaFX | 21.0.6 | Controls + FXML modules |
| **Build Tool** | Maven | 3.9+ | Dependency management + build lifecycle |
| **Database** | Microsoft SQL Server | 2019+ | Relational database chính |
| **Connection Pool** | HikariCP | 5.1.0 | High-performance JDBC connection pooling |
| **JDBC Driver** | mssql-jdbc | 12.4.2 (jre11) | Microsoft SQL Server JDBC driver |
| **Logging** | SLF4J + Simple | 2.0.12 | Structured logging |
| **Email** | Jakarta Mail + Angus Mail | 2.1.3 / 2.0.3 | Gmail SMTP integration |
| **Forms** | FormsFX | 11.6.0 | JavaFX form utilities |
| **Testing** | JUnit Jupiter | 5.12.1 | Unit testing framework |
| **UI Styling** | CSS | Custom (546 lines) | Soft Tech Professional theme |

---

## 📂 Folder Structure

```bash
demo5/
├── pom.xml                          # Maven configuration & dependencies
├── mvnw / mvnw.cmd                  # Maven wrapper (cross-platform)
├── library.db                       # SQLite fallback (optional)
├── .gitignore                       # Git ignore rules
│
└── src/
    └── main/
        ├── java/com/example/demo5/
        │   │
        │   ├── App/                 # 🚀 Application Entry Point
        │   │   ├── Launcher.java    #    Main class (JVM settings + launch)
        │   │   └── LibraryApp.java  #    JavaFX Application (scene management)
        │   │
        │   ├── Config/              # ⚙️ Infrastructure Configuration
        │   │   └── DatabaseManager.java  # HikariCP connection pool (singleton)
        │   │
        │   ├── Model/               # 📦 Domain Models (JavaFX Properties)
        │   │   ├── Book.java        #    Sách (id, title, author, category, qty, status↻)
        │   │   ├── Reader.java      #    Bạn đọc (id, name, email, phone)
        │   │   ├── Loan.java        #    Phiếu mượn (overdue calc, status badge, actions)
        │   │   └── User.java        #    Tài khoản đăng nhập
        │   │
        │   ├── Service/             # 🔧 Business Logic Layer
        │   │   ├── AuthService.java      # Đăng nhập/Đăng ký (PBKDF2 hashing)
        │   │   ├── BookService.java      # CRUD sách + duplicate check
        │   │   ├── ReaderService.java    # CRUD bạn đọc + duplicate check
        │   │   ├── LibraryService.java   # Mượn/Trả sách (transactions, overdue logic)
        │   │   └── EmailService.java     # Gmail SMTP (batch, grouped, HTML template)
        │   │
        │   ├── View/                # 🎨 Presentation Layer (JavaFX UI)
        │   │   ├── BaseEntityView.java   # Abstract base (toolbar, table, search, CRUD)
        │   │   ├── LoginView.java        # Login/Register form (show/hide password)
        │   │   ├── MainDashboardView.java # Dashboard (stats cards + charts + CSV export)
        │   │   ├── BookView.java         # Quản lý sách
        │   │   ├── ReaderView.java       # Quản lý bạn đọc
        │   │   └── BorrowView.java       # Quản lý mượn trả + email dialog
        │   │
        │   └── Util/                # 🔨 Utilities
        │       ├── ViewUtils.java   #    Alert/Confirm helpers
        │       └── ExcelExporter.java #  CSV export (UTF-8 + BOM)
        │
        └── resources/
            ├── dbconfig.properties  # Database connection settings
            ├── email.properties     # SMTP email configuration
            ├── style.css            # 546-line custom CSS theme
            └── com/example/demo5/   # FXML resources (if any)
```

### Giải thích kiến trúc thư mục

| Package | Trách nhiệm |
|---------|-------------|
| `App/` | Khởi tạo JVM, JavaFX toolkit, quản lý scene transitions |
| `Config/` | Cấu hình infrastructure (database connection pool) |
| `Model/` | Domain entities sử dụng JavaFX Properties cho data binding |
| `Service/` | Business logic thuần túy, tương tác database qua JDBC |
| `View/` | UI components kế thừa từ `BaseEntityView<T>` (Template Method) |
| `Util/` | Utility functions dùng chung (alerts, file export) |

---

## ⚙️ Installation

### Prerequisites

- **Java JDK 21** trở lên ([Download](https://adoptium.net/))
- **Microsoft SQL Server 2019+** ([Download](https://www.microsoft.com/en-us/sql-server/sql-server-downloads))
- **Maven 3.9+** (hoặc sử dụng Maven Wrapper đi kèm)
- **Git**

### Bước 1: Clone repository

```bash
git clone https://github.com/HoshiSS1/QuanLyThuVien_LMS.git
cd QuanLyThuVien_LMS
```

### Bước 2: Tạo Database trên SQL Server

```sql
CREATE DATABASE LibraryDB;
GO

USE LibraryDB;
GO

-- Bảng Users (đăng nhập)
CREATE TABLE Users (
    Username NVARCHAR(50) PRIMARY KEY,
    Password NVARCHAR(500) NOT NULL
);

-- Bảng Books (kho sách)
CREATE TABLE Books (
    Id NVARCHAR(20) PRIMARY KEY,
    Title NVARCHAR(200) NOT NULL,
    Author NVARCHAR(100) NOT NULL,
    Category NVARCHAR(50) DEFAULT N'Khác',
    Quantity INT DEFAULT 0
);

-- Bảng Readers (bạn đọc)
CREATE TABLE Readers (
    Id NVARCHAR(20) PRIMARY KEY,
    Name NVARCHAR(100) NOT NULL,
    Email NVARCHAR(100),
    Phone NVARCHAR(15)
);

-- Bảng Loans (phiếu mượn)
CREATE TABLE Loans (
    id NVARCHAR(20) PRIMARY KEY,
    readerName NVARCHAR(100),
    bookTitle NVARCHAR(200),
    borrowDate DATE,
    returnDate DATE,
    status NVARCHAR(20) DEFAULT N'ĐANG MƯỢN',
    quantity INT DEFAULT 1
);
```

### Bước 3: Cấu hình kết nối Database

Chỉnh sửa file `src/main/resources/dbconfig.properties`:

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=LibraryDB;encrypt=true;trustServerCertificate=true;
db.user=sa
db.password=YOUR_PASSWORD
```

### Bước 4: Build & Run

```bash
# Sử dụng Maven Wrapper
./mvnw clean javafx:run

# Hoặc sử dụng Maven hệ thống
mvn clean javafx:run
```

---

## 🔐 Environment Variables

### Database Configuration (`dbconfig.properties`)

| Variable | Description | Default | Required |
|----------|-------------|---------|:--------:|
| `db.url` | JDBC URL kết nối SQL Server | `jdbc:sqlserver://localhost:1433;databaseName=LibraryDB;...` | ✅ |
| `db.user` | Username database | `sa` | ✅ |
| `db.password` | Password database | — | ✅ |

### Email Configuration (`email.properties`)

| Variable | Description | Default | Required |
|----------|-------------|---------|:--------:|
| `mail.smtp.host` | SMTP server hostname | `smtp.gmail.com` | ✅ |
| `mail.smtp.port` | SMTP port (587 cho TLS, 465 cho SSL) | `587` | ✅ |
| `mail.smtp.auth` | Bật xác thực SMTP | `true` | ✅ |
| `mail.smtp.starttls.enable` | Bật STARTTLS encryption | `true` | ✅ |
| `mail.from` | Email gửi đi (Gmail) | — | ✅ |
| `mail.password` | App Password (16 ký tự) | — | ✅ |

> **⚠️ Lưu ý**: Sử dụng [App Password](https://myaccount.google.com/apppasswords) của Google, KHÔNG phải mật khẩu Gmail thường. Yêu cầu bật xác minh 2 bước.

### Runtime Config (tự động lưu tại `~/.lms_email.properties`)

Email config cũng có thể được cấu hình trực tiếp trong giao diện ứng dụng (BorrowView → Gửi Email Quá Hạn → Cấu Hình SMTP). Cấu hình sẽ được lưu tại `%USERPROFILE%\.lms_email.properties`.

---

## ▶️ Running Project

### Development

```bash
# Build và chạy trực tiếp
mvn clean javafx:run

# Chỉ compile (không chạy)
mvn clean compile
```

### Production Build

```bash
# Build JAR
mvn clean package

# Chạy JAR (cần JavaFX trên module path)
java --module-path /path/to/javafx/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/demo5-1.0-SNAPSHOT.jar
```

### Tham số JVM (đã cấu hình trong Launcher.java)

```
-Dprism.text=t2k                    # Fix font rendering trên Windows
-Dprism.lcdtext=false               # Tắt LCD text antialiasing  
-Dprism.subpixeltext=false          # Tắt subpixel rendering
-Djavafx.font=Segoe UI              # Font mặc định
```

---

## 🗄️ Database Schema

### Entity Relationship

```
┌──────────────┐         ┌──────────────┐
│    Users     │         │    Books     │
├──────────────┤         ├──────────────┤
│ Username PK  │         │ Id       PK  │
│ Password     │         │ Title        │
└──────────────┘         │ Author       │
                         │ Category     │
┌──────────────┐         │ Quantity     │
│   Readers    │         └──────┬───────┘
├──────────────┤                │
│ Id       PK  │      ┌────────┴────────┐
│ Name         │◄─────┤     Loans       │
│ Email        │      ├─────────────────┤
│ Phone        │      │ id          PK  │
└──────────────┘      │ readerName  FK* │
                      │ bookTitle   FK* │
                      │ borrowDate      │
                      │ returnDate      │
                      │ status          │
                      │ quantity        │
                      └─────────────────┘
                      
* FK logic (không phải physical FK constraint)
```

### Connection Pool (HikariCP)

| Parameter | Value | Mô tả |
|-----------|-------|--------|
| `maxPoolSize` | 10 | Số kết nối tối đa |
| `minIdle` | 2 | Số kết nối idle tối thiểu |
| `idleTimeout` | 300,000ms (5 phút) | Thời gian idle trước khi đóng |
| `connectionTimeout` | 30,000ms (30s) | Timeout khi lấy kết nối |
| `maxLifetime` | 1,800,000ms (30 phút) | Tuổi thọ tối đa của kết nối |
| `initializationFailTimeout` | -1 | Không block UI nếu DB chậm (async init) |

---

## 🧪 Testing

### Unit Tests

```bash
# Chạy toàn bộ test suite
mvn test

# Chạy test cụ thể
mvn test -Dtest=AuthServiceTest
```

### Manual Testing Checklist

| Test Case | Steps | Expected |
|-----------|-------|----------|
| Đăng ký | Nhập user/pass → Đăng ký | Tạo tài khoản thành công |
| Đăng nhập | Nhập user/pass → Đăng nhập | Vào Dashboard |
| Thêm sách | Dashboard → Sách → Thêm | Sách xuất hiện trong bảng |
| Mượn sách | Mượn Trả → Thêm → Chọn bạn đọc & sách | Phiếu mượn tạo thành công, kho trừ |
| Trả sách | Chọn phiếu → Trả | Trạng thái = ĐÃ TRẢ, kho cộng lại |
| Quá hạn | Mượn sách → Sửa ngày < hôm nay | Badge đỏ "QUÁ HẠN" |
| Gửi email | Mượn Trả → Gửi Email Quá Hạn | Email HTML gửi thành công |
| Export CSV | Sidebar → Xuất Báo Cáo CSV | File CSV với BOM (mở Excel đúng tiếng Việt) |

---

## 📈 Performance & Scalability

### Chiến lược tối ưu hiện tại

| Aspect | Implementation |
|--------|---------------|
| **Connection Pooling** | HikariCP (10 max connections) — loại bỏ overhead tạo/đóng kết nối |
| **Async UI Loading** | Dashboard tải dữ liệu trên background thread (`javafx.concurrent.Task`) |
| **Lazy Init** | Database pool khởi tạo bất đồng bộ (`initializationFailTimeout = -1`) |
| **Observable Binding** | JavaFX Properties + Bindings — UI tự cập nhật khi data thay đổi |
| **Filtered/Sorted List** | `FilteredList` + `SortedList` — tìm kiếm O(n) không reload từ DB |
| **Batch Email** | Gửi email nhóm theo bạn đọc, giảm số lượng SMTP sessions |
| **SMTP Timeout** | Connection timeout 4s, send timeout 5s — tránh block UI khi cấu hình sai |
| **Transaction Safety** | `setAutoCommit(false)` + `commit()`/`rollback()` cho mượn/trả sách |

### Khả năng mở rộng

```
Hiện tại (Desktop)          →  Tương lai (Client-Server)
┌────────────────────┐      ┌──────────────────────────┐
│  JavaFX Client     │      │  JavaFX / Web Client     │
│  ↕ JDBC Direct     │  →   │  ↕ REST API (Spring Boot)│
│  SQL Server        │      │  ↕ Service Layer          │
└────────────────────┘      │  SQL Server + Redis Cache │
                            └──────────────────────────┘
```

---

## 🔒 Security

### Authentication & Authorization

| Aspect | Implementation |
|--------|---------------|
| **Password Hashing** | PBKDF2 with HMAC-SHA256 (65,536 iterations, 256-bit key) |
| **Salt** | 16-byte random salt per user (`SecureRandom`) |
| **Storage Format** | `base64(salt):base64(hash)` |
| **Comparison** | Constant-time `MessageDigest.isEqual()` — chống timing attack |
| **Case Insensitive** | Username lowercase trước khi lưu/so sánh |

### SQL Injection Prevention

```java
// ✅ PreparedStatement toàn bộ hệ thống
PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Users WHERE LOWER(Username) = ?");
pstmt.setString(1, username.toLowerCase());
```

- **100% PreparedStatement** — không có string concatenation trong SQL
- Validation input tại UI layer (empty check, format check)

### Email Security

| Aspect | Implementation |
|--------|---------------|
| **STARTTLS** | Bắt buộc TLS 1.2 cho port 587 |
| **SSL** | Hỗ trợ SSL trực tiếp cho port 465 |
| **App Password** | Yêu cầu Google App Password, không dùng password thường |
| **Config Storage** | Lưu tại `~/.lms_email.properties` (user home, không commit vào git) |

### Data Integrity

- **Database Transactions**: Mượn/Trả sách sử dụng `setAutoCommit(false)` + `rollback()` khi lỗi
- **Duplicate Prevention**: Kiểm tra trùng ID trước khi INSERT (Books, Readers)
- **Business Rule**: Chặn mượn nếu có phiếu quá hạn, chặn mượn trùng đầu sách

---

## 🚢 Deployment

### Local Development

```bash
# 1. Đảm bảo SQL Server đang chạy
# 2. Tạo database LibraryDB (xem mục Installation)
# 3. Chạy ứng dụng
mvn clean javafx:run
```

### Build Distributable

```bash
# Package với Maven
mvn clean package

# Tạo native image (optional, cần GraalVM)
mvn clean javafx:jlink
```

### Checklist trước khi deploy

- [ ] Cấu hình `dbconfig.properties` đúng credentials
- [ ] SQL Server đang chạy và database đã được tạo
- [ ] Java 21+ đã cài đặt
- [ ] JavaFX SDK đã có trên classpath/module path
- [ ] (Optional) Cấu hình email SMTP nếu cần gửi thông báo

---

## 📊 Monitoring & Logging

### Logging System

Hệ thống sử dụng **SLF4J 2.0.12** + **SLF4J Simple** backend.

```
Logging levels được sử dụng:
├── INFO  → Khởi tạo thành công, gửi email thành công
├── WARN  → Không tìm thấy config, user có nợ quá hạn
└── ERROR → SQL exceptions, email failures, hashing errors
```

### Ví dụ log output

```
[main] INFO  c.e.d.Config.DatabaseManager - Database connection pool (HikariCP) initialized successfully.
[Thread-3] INFO  c.e.d.Service.EmailService - ══════════════════════════════════════════════
[Thread-3] INFO  c.e.d.Service.EmailService - ✅ EMAIL GỬI THÀNH CÔNG
[Thread-3] INFO  c.e.d.Service.EmailService -    Đến: Nguyễn Văn A (nguyenvana@gmail.com)
[Thread-3] INFO  c.e.d.Service.EmailService -    📖 Clean Code | Quá hạn 5 ngày
[Thread-3] INFO  c.e.d.Service.EmailService - ══════════════════════════════════════════════
```

### Health Check

- **Database**: HikariCP tự động kiểm tra connection liveness (keepalive)
- **Email SMTP**: Timeout cấu hình 4s/5s để phát hiện lỗi nhanh
- **UI Thread**: `javafx.concurrent.Task` với handler `failed()` để hiển thị lỗi trên UI

---

## 🤝 Contributing

### Branch Naming

```
feature/add-book-import       # Tính năng mới
bugfix/fix-login-crash        # Sửa lỗi
hotfix/patch-sql-injection    # Vá bảo mật khẩn cấp
refactor/optimize-hikari      # Refactor code
docs/update-readme            # Cập nhật tài liệu
```

### Commit Convention

```
feat: thêm chức năng import sách từ Excel
fix: sửa lỗi crash khi đăng nhập username rỗng
refactor: tối ưu HikariCP pool size
docs: cập nhật README hướng dẫn cài đặt
style: format lại CSS dashboard
test: thêm unit test cho AuthService
```

### Pull Request Flow

1. Fork repository
2. Tạo branch mới từ `main`
3. Commit changes với convention trên
4. Push và tạo Pull Request
5. Code review bởi ít nhất 1 member
6. Merge sau khi approved

### Code Style

- **Java**: Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- **CSS**: Sử dụng class naming convention có ý nghĩa (e.g., `btn-gradient`, `stat-card`)
- **Comments**: Tiếng Việt cho business logic, tiếng Anh cho technical comments

---

## 📄 License

```
MIT License

Copyright (c) 2026 HoshiSS1

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 👨‍💻 Author

| | Thông tin |
|--|----------|
| **Team** | HoshiSS1 |
| **Project** | LMS Prestige — Hệ Thống Quản Lý Thư Viện |
| **GitHub** | [github.com/HoshiSS1/QuanLyThuVien_LMS](https://github.com/HoshiSS1/QuanLyThuVien_LMS) |
| **Built with** | ☕ Java 21 + 💜 JavaFX + 🎨 Custom CSS |

---

<p align="center">
  <strong>⭐ Nếu project hữu ích, hãy cho một Star trên GitHub! ⭐</strong>
</p>
