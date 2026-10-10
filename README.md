# 🎓 HỆ THỐNG QUẢN LÝ SINH VIÊN & ĐÀO TẠO (QLSV PORTAL)

Hệ thống quản lý đào tạo, điểm học phần, đánh giá điểm rèn luyện và kết xuất báo cáo thống kê chuyên nghiệp dành cho trường đại học. Dự án được xây dựng theo kiến trúc hiện đại: **Spring Boot 3** (Backend) kết hợp **Angular 19** (Frontend), sử dụng **Microsoft SQL Server**, **RabbitMQ** để xử lý hàng đợi bất đồng bộ và **JasperReports** để kết xuất báo cáo chuẩn học thuật.

---

## 📌 MỤC LỤC
1. [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
2. [Các tính năng nổi bật](#-các-tính-năng-nổi-bật)
3. [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
4. [Hướng dẫn cài đặt & Khởi chạy](#-hướng-dẫn-cài-đặt--khởi-chạy)
5. [Tài khoản kiểm thử mặc định](#-tài-khoản-kiểm-thử-mặc-định)
6. [Danh sách API chính](#-danh-sách-api-chính)
7. [Cấu trúc thư mục dự án](#-cấu-trúc-thư-mục-dự-án)

---

## 🏗 KIẾN TRÚC HỆ THỐNG

```
       [ Angular 19 Client ]
           │          ▲
      HTTP │          │ Server-Sent / Polling
           ▼          │
   ┌──────────────────────────────────────────────┐
   │         Spring Boot 3 REST API               │
   │  (Spring Security + JWT + Validation)        │
   └───────────────┬──────────────────────┬───────┘
                   │                      │
        JDBC / JPA │                      │ AMQP (Produce/Consume)
                   ▼                      ▼
    ┌──────────────────────┐    ┌──────────────────────┐
    │ Microsoft SQL Server │    │  RabbitMQ Message    │
    │      (Database)      │    │        Broker        │
    └──────────────────────┘    └──────────┬───────────┘
                                           │
                                           ▼
                                ┌──────────────────────┐
                                │ JasperReports Engine │
                                │ (Async PDF Export)   │
                                └──────────────────────┘
```

---

## ✨ CÁC TÍNH NĂNG NỔI BẬT

### 1. 🔐 Phân quyền & Bảo mật (Spring Security + JWT)
- Hệ thống hỗ trợ 3 nhóm vai trò người dùng (Roles):
  - **Quản trị viên (ADMIN)**: Quản lý toàn bộ danh mục đào tạo, duyệt điểm rèn luyện cấp trường, cấu hình đợt đánh giá, xem báo cáo toàn diện.
  - **Giảng viên (LECTURER / GIANG_VIEN)**: Nhập điểm học phần, đánh giá điểm rèn luyện cho lớp chủ nhiệm/cố vấn, chuyển duyệt lên Admin.
  - **Sinh viên (STUDENT / SINH_VIEN)**: Xem lịch học, bảng điểm cá nhân, thực hiện phiếu tự đánh giá rèn luyện học kỳ.

### 2. 📚 Quản lý Đào tạo Toàn diện
- Quản lý Khoa/Viện đào tạo.
- Quản lý Lớp sinh hoạt / Lớp hành chính.
- Quản lý Danh mục Môn học & Số tín chỉ.
- Quản lý Giảng viên, Sinh viên, hồ sơ cá nhân.
- Quản lý Đăng ký môn học & Điểm quá trình, thi kết thúc học phần.

### 3. ⭐ Quy trình Đánh giá Điểm Rèn Luyện (ĐRL) 3 Cấp
Chuẩn hóa theo đúng quy chế rèn luyện của Bộ Giáo dục & Đào tạo:
1. **Admin**: Tạo và mở đợt rèn luyện (theo học kỳ & năm học), cấu hình thời hạn nộp.
2. **Sinh viên**: Tự chấm điểm theo 5 khung tiêu chí (Ý thức học tập, Ý thức chấp hành nội quy, Hoạt động phong trào, Quan hệ cộng đồng, Khen thưởng/Kỷ luật - Thang điểm 100), ghi chú minh chứng và nộp phiếu.
3. **Giảng viên**: Xem danh sách lớp chủ nhiệm, chấm điểm phản biện từng sinh viên, chỉnh sửa và bấm *"🚀 Gửi toàn bộ lớp lên Admin duyệt"*.
4. **Admin**: Phê duyệt chính thức toàn trường hoặc từng lớp, tự động tính điểm tổng kết và xếp loại (*Xuất sắc, Tốt, Khá, Trung bình, Yếu, Kém*).

### 4. 📊 Thống kê GPA & Phân loại Học lực (Native SQL)
- Tối ưu hiệu năng bằng **Native SQL** kết hợp `SqlResultSetMapping` theo cấu trúc Repository Impl.
- Thống kê trực tiếp điểm trung bình tích lũy (GPA), tổng số tín chỉ tích lũy, số môn đã hoàn thành và xếp loại học lực của sinh viên theo từng lớp hoặc toàn trường.

### 5. 🚀 Xuất Báo cáo PDF Chuyên nghiệp (JasperReports + RabbitMQ)
- **Giải quyết bài toán tải trang lâu (Non-blocking Asynchronous Export)**: Khi xuất báo cáo với khối lượng dữ liệu lớn, hệ thống không bắt người dùng chờ đợi trực tiếp trên HTTP Request.
- **Quy trình xử lý ngầm**:
  1. Frontend gửi yêu cầu xuất báo cáo ➔ Backend trả về ngay `HTTP 202 Accepted` kèm `jobId`.
  2. Backend đẩy thông điệp vào hàng đợi RabbitMQ (`qlsv.report.queue`).
  3. Consumer xử lý ngầm trong nền: biên dịch mẫu JasperReports (`.jrxml`), nạp dữ liệu và xuất file PDF chuẩn A4 lưu trữ tại `reports_storage/`.
  4. Frontend tự động tải file PDF về máy người dùng ngay khi hoàn tất mà không làm đơ trang.
- **Hỗ trợ 2 mẫu báo cáo chuẩn đại học chính quy**:
  - **Báo cáo Thống kê Điểm TB (GPA) & Học lực**: Khổ ngang (Landscape A4), tiêu ngữ, thông tin bộ lọc, bảng điểm chi tiết, 3 chữ ký (Người lập, Trưởng phòng đào tạo, Ban Giám hiệu).
  - **Báo cáo Tổng hợp Điểm Rèn Luyện**: Khổ dọc (Portrait A4), điểm SV, điểm GV, điểm kết luận, xếp loại và chữ ký 3 bên.

### 6. 🛡️ Validation & Exception Handling Chuẩn mực
- **Validation chặt chẽ**:
  - Email đúng định dạng RFC 5322.
  - Số điện thoại chuẩn nhà mạng Việt Nam 10 chữ số (đầu số 03, 05, 07, 08, 09).
  - Ngày sinh phải là ngày trong quá khứ.
  - Ràng buộc thời gian: Ngày bắt đầu phải nhỏ hơn ngày kết thúc đợt rèn luyện.
- **Bắt lỗi tập trung (GlobalExceptionHandler)**: Trả về cấu trúc JSON thông báo lỗi rõ ràng, dễ hiểu cho người dùng cuối.

---

## 💻 CÔNG NGHỆ SỬ DỤNG

### Backend
- **Ngôn ngữ**: Java 17
- **Framework**: Spring Boot 3.5.x
- **Bảo mật**: Spring Security 6, JJWT (0.12.6)
- **Dữ liệu**: Spring Data JPA, Hibernate, Microsoft SQL Server JDBC Driver
- **Message Broker**: Spring AMQP (RabbitMQ)
- **Báo cáo**: JasperReports (6.21.3) + OpenPDF (1.3.40)
- **Công cụ build**: Maven

### Frontend
- **Framework**: Angular 19 (Standalone Components)
- **Ngôn ngữ**: TypeScript 5, HTML5, SCSS
- **HTTP & State**: Angular HttpClient, RxJS
- **Giao diện**: Thiết kế Responsive, Bảng điều khiển KPI, Modal xác nhận, Toast thông báo

### Cơ sở hạ tầng
- **Database**: Microsoft SQL Server
- **Message Broker**: RabbitMQ 4 (Management UI)

---

## 🚀 HƯỚNG DẪN CÀI ĐẶT & KHỞI CHẠY

### 1. Yêu cầu môi trường
- **JDK**: Phiên bản 17 trở lên
- **Node.js**: Phiên bản 18.x hoặc 20.x
- **Docker** (khuyến nghị để chạy RabbitMQ) hoặc RabbitMQ cài trực tiếp trên máy
- **Microsoft SQL Server**: Cổng 1433

---

### 2. Khởi động RabbitMQ
Chạy container RabbitMQ bằng Docker:
```bash
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:4-management
```
> Giao diện quản trị RabbitMQ Management: [http://localhost:15672](http://localhost:15672) (Tài khoản: `guest` / `guest`).

---

### 3. Cấu hình & Chạy Backend (Spring Boot)

1. Mở file cấu hình [BE/src/main/resources/application.properties](file:///d:/LT-W/java/QLSV/BE/src/main/resources/application.properties) và điều chỉnh thông tin database:
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=QLDD;encrypt=true;trustServerCertificate=true;sendStringParametersAsUnicode=true
   spring.datasource.username=sa
   spring.datasource.password=123456
   
   spring.rabbitmq.host=localhost
   spring.rabbitmq.port=5672
   spring.rabbitmq.username=guest
   spring.rabbitmq.password=guest
   ```

2. Tạo database `QLDD` trong SQL Server:
   ```sql
   CREATE DATABASE QLDD;
   ```

3. Biên dịch và khởi chạy Backend:
   ```bash
   cd BE
   mvn clean spring-boot:run
   ```
   > Backend sẽ khởi chạy tại: **http://localhost:8080**  
   > Dữ liệu khởi tạo mặc định sẽ được nạp tự động qua `DataInitializer`.

---

### 4. Cài đặt & Chạy Frontend (Angular)

1. Di chuyển vào thư mục Frontend:
   ```bash
   cd FE
   ```

2. Cài đặt các thư viện phụ thuộc:
   ```bash
   npm install
   ```

3. Khởi chạy ứng dụng:
   ```bash
   npm start
   ```
   > Truy cập hệ thống tại: **http://localhost:4200**

---

## 🔑 TÀI KHOẢN KIỂM THỬ MẶC ĐỊNH

Khi hệ thống khởi động lần đầu, `DataInitializer` sẽ tự động tạo sẵn các tài khoản mẫu:

| Vai trò | Tên đăng nhập | Mật khẩu mặc định | Quyền hạn |
| :--- | :--- | :--- | :--- |
| **Quản trị viên** | `admin` | `admin123` | Toàn quyền quản trị hệ thống, duyệt ĐRL, xem thống kê |
| **Giảng viên** | Mã GV (Ví dụ: `GV001`) | `gv123456` | Nhập điểm học phần, đánh giá ĐRL cho lớp phụ trách |
| **Sinh viên** | MSSV (Ví dụ: `SV2026001`) | Ngày sinh `ddMMyyyy` (hoặc `student123`) | Tự đánh giá ĐRL, xem bảng điểm cá nhân |

---

## 📡 DANH SÁCH API CHÍNH

### 1. Xác thực & Tài khoản (`/api/auth`)
- `POST /api/auth/login`: Đăng nhập, nhận JWT Token và thông tin User.
- `GET /api/auth/me`: Lấy thông tin tài khoản hiện tại.

### 2. Quản lý Đào tạo
- `/api/khoa`: Quản lý danh sách Khoa.
- `/api/lop`: Quản lý Lớp hành chính.
- `/api/mon-hoc`: Quản lý Môn học.
- `/api/giang-vien`: Quản lý thông tin Giảng viên.
- `/api/sinh-vien`: Quản lý hồ sơ Sinh viên.
- `/api/dang-ky-mon-hoc` & `/api/diem`: Đăng ký môn và bảng điểm học phần.

### 3. Điểm Rèn Luyện (`/api/diem-ren-luyen`)
- `GET /api/diem-ren-luyen/dots`: Danh sách các đợt đánh giá rèn luyện.
- `POST /api/diem-ren-luyen/dots`: (Admin) Tạo mới hoặc cập nhật đợt rèn luyện.
- `GET /api/diem-ren-luyen/my-sheet`: (Sinh viên) Lấy phiếu tự đánh giá của mình.
- `POST /api/diem-ren-luyen/student-submit`: (Sinh viên) Nộp phiếu tự đánh giá.
- `GET /api/diem-ren-luyen/class/{lopId}`: (Giảng viên/Admin) Lấy phiếu điểm của cả lớp.
- `POST /api/diem-ren-luyen/lecturer-grade`: (Giảng viên) Chấm điểm cho từng sinh viên.
- `POST /api/diem-ren-luyen/lecturer-submit-class`: (Giảng viên) Chuyển toàn bộ lớp lên Admin duyệt.
- `POST /api/diem-ren-luyen/admin-approve`: (Admin) Phê duyệt phiếu điểm.
- `POST /api/diem-ren-luyen/admin-approve-class`: (Admin) Phê duyệt toàn bộ lớp.

### 4. Thống kê & Báo cáo (`/api/thong-ke` & `/api/reports`)
- `GET /api/thong-ke/sinh-vien-gpa?lopId={id}`: Thống kê GPA sinh viên (Native Query).
- `POST /api/reports/export-drl`: Gửi yêu cầu xuất PDF Điểm rèn luyện qua RabbitMQ (HTTP 202).
- `POST /api/reports/export-gpa`: Gửi yêu cầu xuất PDF Thống kê GPA qua RabbitMQ (HTTP 202).
- `GET /api/reports/status/{jobId}`: Kiểm tra tiến độ xử lý tác vụ báo cáo.
- `GET /api/reports/download/{jobId}`: Tải file PDF kết xuất về máy.
- `GET /api/reports/my-history`: Xem lịch sử các file báo cáo đã xuất.

---

## 📂 CẤU TRÚC THƯ MỤC DỰ ÁN

```
QLSV/
├── BE/                                    # Mã nguồn Backend (Spring Boot)
│   ├── src/main/java/com/demo/be/
│   │   ├── config/                        # Cấu hình Security, RabbitMQ, DataInitializer
│   │   ├── consumer/                      # RabbitMQ Consumers (Xử lý báo cáo ngầm)
│   │   ├── controller/                    # REST API Controllers
│   │   ├── dto/                           # Data Transfer Objects
│   │   ├── exception/                     # Global Exception Handler
│   │   ├── model/                         # JPA Entities (SinhVien, Lop, ReportJob, ...)
│   │   ├── producer/                      # RabbitMQ Message Producers
│   │   ├── repository/                    # Spring Data JPA Repositories
│   │   ├── security/                      # JWT Token Provider & Auth Filter
│   │   └── service/                       # Business Logic & JasperReports Service
│   ├── src/main/resources/
│   │   ├── reports/                       # Mẫu JasperReports (.jrxml)
│   │   │   ├── diem_ren_luyen_report.jrxml
│   │   │   └── thong_ke_gpa_report.jrxml
│   │   └── application.properties         # Cấu hình kết nối DB & RabbitMQ
│   ├── reports_storage/                   # Thư mục lưu trữ file PDF kết xuất
│   └── pom.xml                            # Quản lý dependencies Maven
│
├── FE/                                    # Mã nguồn Frontend (Angular 19)
│   ├── src/app/
│   │   ├── components/                    # Confirm Dialog, Toast, Pagination
│   │   ├── layouts/                       # Shell, Header, Sidebar
│   │   ├── pages/                         # Các trang chức năng
│   │   │   ├── admin/                     # Quản trị (Sinh viên, Giảng viên, ĐRL, Thống kê GPA)
│   │   │   ├── lecturer/                  # Giảng viên (Chấm điểm môn, ĐRL lớp chủ nhiệm)
│   │   │   ├── student-portal/            # Cổng sinh viên (Tự đánh giá ĐRL, Xem điểm)
│   │   │   └── auth/                      # Đăng nhập
│   │   ├── services/                      # API Services (Auth, Drl, Report, Toast)
│   │   └── styles.scss                    # Design system chuẩn màu đại học
│   ├── angular.json
│   └── package.json
│
└── README.md                              # Tài liệu hướng dẫn dự án
```

---

## 👨‍💻 BẢN QUYỀN & PHÁT TRIỂN
Dự án được xây dựng phục vụ học tập, nghiên cứu và quản lý giáo dục đào tạo theo tiêu chuẩn ứng dụng doanh nghiệp.
