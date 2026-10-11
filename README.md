# TDT Bank – Ứng dụng ngân hàng di động (Android)

Đồ án cuối kỳ môn **Mobile Apps Development** – Khoa Công nghệ Thông tin, Đại học Tôn Đức Thắng.
Ứng dụng Android (Java, MVVM) với hai giao diện: **khách hàng** và **nhân viên ngân hàng**; dữ liệu trên **Firebase** (Authentication + Cloud Firestore).

> **Trạng thái:** 34 unit test domain/utils đã chạy đạt bằng JUnit; build Android đầy đủ (Gradle + AndroidX/Firebase) **chưa được xác nhận**
> trên máy thật — CI GitHub Actions và `docker/android/Dockerfile` sẽ build và báo lỗi nếu có. Chi tiết: [docs/setup/testing.md](docs/setup/testing.md).

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
osmdroid · ML Kit Face Detection · Material 3 · Docker (Firebase Emulator, build APK) · GitHub Actions CI.
Lý do chọn từng công nghệ: [docs/architecture.md](docs/architecture.md).

## Cấu trúc dự án

```text
.
├── app/                         Module Android duy nhất
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/vn/edu/tdtu/tdtbank/
│       │   ├── model/           POJO Firestore + enum
│       │   ├── domain/          Luật nghiệp vụ thuần Java (TransactionRules, OtpManager, MortgageCalculator, ChatIntentEngine, VnPayUrlBuilder…)
│       │   ├── data/            firebase/ (LiveData thời gian thực), repository/, remote/ (OSRM, Gemini, danh mục mẫu)
│       │   ├── viewmodel/       ViewModel theo màn hình
│       │   ├── adapter/         RecyclerView adapters
│       │   ├── ui/              auth, customer, officer, map, chat, profile, common
│       │   └── utils/
│       ├── main/res/            layout, navigation, menu, values, drawable
│       ├── debug/res/           cấu hình mạng cho Firebase Emulator (chỉ bản debug)
│       └── test/                Unit test JVM
├── gradle/libs.versions.toml    Phiên bản thư viện tập trung
├── firebase.json                Cấu hình Firestore + Emulator
├── firestore.rules              Quy tắc bảo mật (lớp thực thi cuối cùng khi không có server)
├── firestore.indexes.json
├── scripts/                     Nạp dữ liệu demo (Admin SDK, chạy được với Emulator)
├── sample-data/                 Mô tả dữ liệu demo
├── docker/                      android/ (build APK + test), firebase/ (Local Emulator)
├── docker-compose.yml           Firebase Emulator + seed
├── Makefile                     Lối tắt lệnh Docker/test
├── tools/                       Script kiểm tra tĩnh tài nguyên/import
├── .github/workflows/ci.yml     CI: unit test + build APK + kiểm tra tĩnh
└── docs/                        Phân tích thiết kế, UML, ER, bảo mật, báo cáo, hướng dẫn (mục lục: docs/README.md)
```

## Chạy dự án

Yêu cầu: Android Studio (JDK 17), Android SDK 36. Hoặc chỉ cần Docker.

**Cách A – Firebase Emulator bằng Docker (nhanh, không cần Firebase project):**

```bash
docker compose up -d --build firebase    # Emulator UI: http://localhost:4000
docker compose run --rm seed             # nạp dữ liệu demo
```

Thêm `firebase.emulatorHost=10.0.2.2` vào `local.properties` → Sync Gradle → chạy cấu hình `app` trên Android Emulator.
Chi tiết: [docs/setup/docker.md](docs/setup/docker.md).

**Cách B – Firebase thật:** làm theo [docs/setup/firebase.md](docs/setup/firebase.md) (đặt `app/google-services.json`, triển khai rules, chạy seed).

(Tùy chọn) VNPay/Gemini: [docs/setup/external-services.md](docs/setup/external-services.md). Mẫu cấu hình: `local.properties.example`.

Đăng nhập thử: `officer1/officer123`, `customer1/customer123`, `customer2/customer123` (xem [sample-data](sample-data/README.md)).

## Build và test

```bash
./gradlew testDebugUnitTest assembleDebug                        # trên máy có Android SDK
docker build -f docker/android/Dockerfile --output .build-outputs .  # trong Docker → .build-outputs/apk/
```

CI GitHub Actions chạy các bước trên ở mỗi push/PR và đính kèm APK debug.

## Tài liệu

Mục lục và ánh xạ tới bảng chấm điểm: [docs/README.md](docs/README.md).

## Giấy phép dữ liệu

Mọi tên, CCCD, số điện thoại, chi nhánh, danh mục dịch vụ là **dữ liệu hư cấu** phục vụ học tập.
