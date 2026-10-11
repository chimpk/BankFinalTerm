# Báo cáo đồ án cuối kỳ – TDT Bank

> **Lưu ý:** đây là **nội dung** báo cáo theo ba mục đề bài yêu cầu (Analysis and Design, Implementation, User Interface).
> Nhóm cần chuyển sang **mẫu báo cáo của Khoa** (không được cung cấp cho bản soạn này), chèn ảnh chụp màn hình thật,
> điền thông tin nhóm và đổi các mục `[CẦN ĐIỀN]`.

- Môn: Mobile Apps Development – Khoa Công nghệ Thông tin, Đại học Tôn Đức Thắng
- Đề tài: Ứng dụng ngân hàng di động **TDT Bank**
- Nhóm: `[CẦN ĐIỀN – xem docs/report/team-info.md]`
- Liên kết: Mã nguồn `[CẦN ĐIỀN]` · Google Play `[CẦN ĐIỀN]` · Video demo `[CẦN ĐIỀN]`

## 1. Giới thiệu và phạm vi

Ứng dụng cho phép khách hàng thực hiện hoạt động ngân hàng trên điện thoại và cho phép nhân viên quản lý khách hàng, tài khoản, lãi suất, eKYC.
Bảy nhóm yêu cầu của đề (người dùng, bảo mật, tài khoản, giao dịch, tiện ích, bản đồ, chatbot) đều có cài đặt; mức độ "thật" của từng phần
được ghi ở `docs/requirements-traceability.md` và `docs/limitations.md`. **Báo cáo không tuyên bố** OTP/SMS thật, so khớp khuôn mặt tự động,
liên ngân hàng thật hay tích hợp VNPay/Stripe đã được kiểm thử thật.

## 2. Phân tích và thiết kế (Analysis and Design)

### 2.1 Nguyên lý, mẫu và thực hành áp dụng
Xem bảng đầy đủ ở `docs/architecture.md` mục 1 (lý do chọn công nghệ ở mục 0): MVVM, Repository, Observer (LiveData thời gian thực), Template Method (`BaseShellActivity`),
Facade (`TransactionFlow`), tách logic nghiệp vụ thuần Java để kiểm thử, phòng thủ nhiều lớp (UI → domain → Firestore Rules), bản ghi giao dịch bất biến.

### 2.2 Yêu cầu
| Nhóm | Yêu cầu chức năng chính |
|---|---|
| Người dùng | Khách hàng và nhân viên có giao diện/quyền khác nhau; nhân viên tạo khách hàng và sửa dữ liệu theo yêu cầu |
| Bảo mật | Đăng nhập; eKYC bằng ảnh khuôn mặt; mọi giao dịch cần OTP; giao dịch giá trị cao cần xác minh khuôn mặt |
| Tài khoản | Checking (số dư), Saving (số dư, lãi suất, lợi nhuận/tháng), Mortgage (số tiền phải trả mỗi tháng/2 tuần); lịch sử; nạp/rút |
| Giao dịch | Hợp lệ trước khi ghi; nội bộ & liên ngân hàng; cổng thanh toán |
| Tiện ích | Điện, nước, nạp điện thoại, vé máy bay/phim, khách sạn, TMĐT |
| Điều hướng | Vị trí, chi nhánh gần, đường đi bộ ngắn nhất |
| Chatbot | Trả lời kèm liên kết tới chức năng |

### 2.3 Use case, sơ đồ lớp, ER, tuần tự
Các sơ đồ nằm ở **`docs/uml.md`** (Mermaid): use case, class (model/domain và data/viewmodel/ui), sequence chuyển tiền, state eKYC, điều hướng.
Khi chuyển sang mẫu Khoa, xuất các sơ đồ thành ảnh (ví dụ qua mermaid.live) và chèn vào báo cáo.

### 2.4 Thiết kế cơ sở dữ liệu
Cloud Firestore (NoSQL) – lược đồ, sơ đồ ER và quan hệ ở `docs/database.md`: `users, kyc, accounts, accountDirectory, transactions, savingRatePolicies, branches`.
Lý do chọn mô hình: truy vấn chính là theo chủ sở hữu và theo mảng tham gia giao dịch; số dư đổi nguyên tử bằng `WriteBatch` + `FieldValue.increment`.

### 2.5 Thiết kế bảo mật
- Vai trò + khóa tài khoản trong `users`; kiểm tra ở `firestore.rules` (ma trận ở `docs/security.md`).
- Tính toàn vẹn giao dịch: số dư chỉ đổi khi batch tạo kèm giao dịch khớp (số tiền, phí, chủ sở hữu, đủ số dư; phía cộng bắt buộc có phía trừ cùng batch).
- OTP: băm SHA-256 có muối, gắn giao dịch, 2 phút, 3 lần, một lần dùng.
- Giới hạn bảo mật còn lại: `docs/limitations.md` (mục 1–8).

### 2.6 Hạ tầng và giải pháp
Sơ đồ giải pháp: `docs/architecture.md` mục 2. Hạ tầng: Firebase Authentication + Cloud Firestore (gói Spark), OpenStreetMap/OSRM, tùy chọn VNPay sandbox và Gemini.

## 3. Cài đặt (Implementation)

### 3.1 Cấu trúc mã
`model/`, `domain/`, `data/{firebase,repository,remote}`, `viewmodel/`, `adapter/`, `ui/{auth,customer,officer,map,chat,profile,common}`, `utils/` (xem README).

### 3.2 Công nghệ
Java 17; AGP 8.10.1; Gradle 8.11.1; compile/target SDK 36, minSdk 26; Material 3; ViewBinding; Navigation Component; Firebase Auth + Firestore (BoM 33.7.0);
osmdroid 6.1.18; ML Kit Face Detection 16.1.7; Google Play services Location 21.3.0. *(Các phiên bản cần xác nhận khi Gradle sync lần đầu.)*

### 3.3 Các kỹ thuật đáng chú ý
| Chủ đề | Cách làm | Tệp |
|---|---|---|
| Dữ liệu thời gian thực theo vòng đời | `QueryLiveData` / `DocumentLiveData` | `data/firebase/` |
| Giao dịch nguyên tử | `WriteBatch`: tạo `transactions/{txId}` + `increment` số dư + `lastTxId` | `TransactionRepository.commit` |
| Chống trừ tiền hai lần | `txId` sinh sẵn; tạo lại cùng id bị Rules từ chối | `TxDraft`, `firestore.rules` |
| Luồng bảo mật dùng chung | kiểm tra → khuôn mặt → OTP → ghi | `TransactionFlow` |
| eKYC | Camera hệ thống + ML Kit (1 khuôn mặt, mỉm cười), Base64 JPEG ≤ 480 px | `FaceCaptureDialogFragment`, `ImageUtils` |
| Lịch vay | Niên kim; hằng tháng hoặc mỗi 14 ngày (26 kỳ/năm) | `MortgageCalculator` |
| Đường đi bộ | 3 chi nhánh gần nhất (Haversine) → OSRM foot → chọn tuyến ngắn nhất | `MapViewModel`, `RoutingClient` |
| Chatbot | Chuẩn hóa tiếng Việt bỏ dấu + từ khóa có trọng số → liên kết chức năng | `ChatIntentEngine`, `ChatViewModel` |
| Edge-to-edge (API 36) | `fitsSystemWindows` ở khung ứng dụng và màn hình đăng nhập | `activity_shell.xml` |

### 3.4 Kiểm thử
Xem `docs/setup/testing.md`: 38 unit test JVM (CI GitHub Actions chạy bằng Gradle) và danh sách 19 test thủ công **chưa chạy**.
`[CẦN ĐIỀN]` – kết quả test thủ công sau khi nhóm chạy.

## 4. Giao diện người dùng (User Interface)

Chèn ảnh chụp màn hình thật `[CẦN ĐIỀN]` cho từng mục sau:

| # | Màn hình | Mô tả |
|---|---|---|
| 1 | Đăng nhập | Tên đăng nhập + mật khẩu; báo lỗi tiếng Việt |
| 2 | Trang chủ khách hàng | Lời chào, banner eKYC, danh sách tài khoản, nút nạp/rút/lịch sử |
| 3 | Chi tiết tài khoản thanh toán | Số dư, nạp/rút/chuyển, lịch sử tài khoản |
| 4 | Chi tiết tài khoản tiết kiệm | Kỳ hạn, lãi suất, lợi nhuận/tháng, lãi khi đáo hạn |
| 5 | Chi tiết khoản vay | Dư nợ, số tiền mỗi tháng/2 tuần, kỳ tiếp theo, nút trả nợ |
| 6 | Lịch trả nợ | Từng kỳ: gốc, lãi, dư nợ; kỳ đã trả mờ đi |
| 7 | Chuyển tiền | Nội bộ/liên ngân hàng, phí, tra tên người nhận |
| 8 | OTP + SMS mô phỏng | Hộp thoại 2FA |
| 9 | Xác minh khuôn mặt | Chụp, kết quả ML Kit |
| 10 | Biên lai | Sau giao dịch thành công |
| 11 | Nạp/rút tiền | Cổng SANDBOX/VNPay |
| 12 | Tiện ích + thanh toán dịch vụ | Lưới 7 dịch vụ; form theo loại |
| 13 | Lịch sử theo tháng | Tiền vào/ra |
| 14 | Bản đồ | Chi nhánh, vị trí, đường đi bộ, quãng đường/thời gian |
| 15 | Chatbot | Câu trả lời kèm nút mở chức năng |
| 16 | Hồ sơ + eKYC + đổi mật khẩu | |
| 17 | Nhân viên: danh sách/tạo khách hàng | |
| 18 | Nhân viên: chi tiết khách (sửa, khóa, mở tài khoản, eKYC tại quầy) | |
| 19 | Nhân viên: duyệt eKYC | |
| 20 | Nhân viên: chỉnh lãi suất | |

## 5. Kết luận, giới hạn và hướng phát triển
Giới hạn: `docs/limitations.md`. Hướng phát triển: chuyển OTP, xác nhận cổng thanh toán, hạn mức ngày, tạo tài khoản đăng nhập sang Cloud Functions (gói Blaze);
tích hợp so khớp khuôn mặt (mô hình nhúng khuôn mặt chạy trên thiết bị hoặc dịch vụ eKYC); kết nối nhà cung cấp hóa đơn/NAPAS thật; Stripe; kiểm thử giao diện (Espresso).
