# TDT Bank – Ứng dụng ngân hàng di động (Android)

Đồ án cuối kỳ môn **Mobile Apps Development** – Khoa Công nghệ Thông tin, Đại học Tôn Đức Thắng.
Ứng dụng Android (Java, MVVM) với hai giao diện: **khách hàng** và **nhân viên ngân hàng**; dữ liệu trên **Firebase** (Authentication + Cloud Firestore).

> **Trạng thái bản giao này (trung thực):** mã nguồn, tài liệu và quy tắc bảo mật đã được viết đầy đủ nhưng
> **chưa được build bằng Gradle/Android Studio và chưa chạy trên thiết bị hoặc emulator** (môi trường soạn thảo không có Android SDK).
> Những gì đã kiểm chứng thật và những gì chưa nằm ở [docs/test-and-setup.md](docs/test-and-setup.md). Hãy **Sync Gradle → Build → chạy**
> rồi gửi lại lỗi (nếu có) để sửa.

## Tính năng theo đề bài

| # | Yêu cầu đề | Cài đặt | Ghi chú trung thực |
|---|---|---|---|
| 1 | Hai loại người dùng, giao diện/quyền khác nhau | `CustomerMainActivity` / `OfficerMainActivity`, `firestore.rules` | Nhân viên tạo khách hàng, sửa dữ liệu theo yêu cầu, khóa/mở khóa |
| 2 | Đăng nhập, eKYC, 2FA | Firebase Auth; ML Kit + chụp ảnh; OTP 6 số | **OTP là SMS mô phỏng**; eKYC **không so khớp khuôn mặt tự động** – xem [limitations](docs/limitations.md) |
| 3 | Tài khoản checking/saving/mortgage | Mỗi loại một cách hiển thị; lãi suất, lợi nhuận/tháng, lịch trả nợ tháng/2 tuần; nhân viên sửa lãi suất | |
| 4 | Giao dịch, nội bộ/liên ngân hàng, VNPay/Stripe | Batch nguyên tử + Firestore Rules; cổng SANDBOX mô phỏng; VNPay sandbox khi có khóa | **Chưa có tài khoản VNPay/Stripe** nên chưa kiểm thử cổng thật; liên ngân hàng là mô phỏng; Stripe chưa làm |
| 5 | Tiện ích | Điện, nước, nạp điện thoại, vé máy bay, vé phim, khách sạn, đơn hàng TMĐT | Danh mục **mẫu**, không nối nhà cung cấp thật |
| 6 | Bản đồ, chi nhánh, đường đi bộ ngắn nhất | osmdroid (OpenStreetMap) + OSRM foot | Cần Internet; chi nhánh là dữ liệu hư cấu |
| 7 | Chatbot AI | Bộ luật ý định tiếng Việt + liên kết chức năng; Gemini tùy chọn | Gemini chưa kiểm thử (chưa có khóa) |

## Công nghệ

Java 17 · Android `minSdk 26`, `targetSdk/compileSdk 36` (Google Play yêu cầu API 36 cho ứng dụng mới từ 31/08/2026) ·
AGP 8.10.1 · Gradle 8.11.1 · MVVM + ViewBinding + Navigation Component · Firebase Auth + Firestore (gói **Spark**, không Cloud Functions/Storage) ·
osmdroid · ML Kit Face Detection · Material 3.

## Cấu trúc

```text
app/src/main/java/vn/edu/tdtu/tdtbank/
├── model/        POJO Firestore + enum
├── domain/       Luật nghiệp vụ thuần Java (TransactionRules, OtpManager, MortgageCalculator, ChatIntentEngine, VnPayUrlBuilder…)
├── data/         firebase/ (LiveData thời gian thực), repository/, remote/ (OSRM, Gemini, danh mục mẫu)
├── viewmodel/    ViewModel theo màn hình
├── adapter/      RecyclerView adapters
├── ui/           auth, customer, officer, map, chat, profile, common
└── utils/
firestore.rules   Quy tắc bảo mật (lớp thực thi cuối cùng khi không có server)
scripts/          Nạp dữ liệu demo bằng Admin SDK
docs/             Phân tích thiết kế, UML, ER, hướng dẫn, báo cáo
```

## Chạy dự án

1. Làm theo [docs/firebase-setup.md](docs/firebase-setup.md): tạo Firebase project, bật Email/Password, tạo Firestore, đặt `app/google-services.json`, triển khai `firestore.rules`, chạy script seed.
2. (Tùy chọn) VNPay/Gemini: [docs/services-setup.md](docs/services-setup.md).
3. Mở bằng Android Studio (JDK 17), **Sync**, chạy cấu hình `app`.
4. Đăng nhập thử: `officer1/officer123`, `customer1/customer123` (xem [sample-data](sample-data/README.md)).

## Chạy test

```bash
./gradlew test            # unit test JVM (domain, validation, parser)
```
Lưu ý: kết quả `gradlew test` chưa được chạy ở bản giao này (xem docs/test-and-setup.md để biết cách các test đã được thực thi thay thế).

## Khác biệt so với repo giữa kỳ

Repo giữa kỳ (`student-management-midterm`) là bộ khung (các package chỉ có `.gitkeep`). Bản này giữ cách tổ chức (Java, MVVM, ViewBinding, Navigation, Firebase,
docs, quy tắc đặt tên: code tiếng Anh – giao diện tiếng Việt) nhưng: nâng AGP/Gradle/SDK lên API 36; đổi package khỏi `com.example`;
bỏ Firebase Storage/Analytics/Glide (gói Spark, không dùng); thêm `gradlew` Unix còn thiếu.

## Giấy phép dữ liệu

Mọi tên, CCCD, số điện thoại, chi nhánh, danh mục dịch vụ là **dữ liệu hư cấu** phục vụ học tập.
