# Truy vết yêu cầu đề bài → cài đặt → trạng thái kiểm chứng

Cột "Đã kiểm chứng" chỉ ghi những gì **đã thực sự chạy** ở môi trường soạn thảo. Mọi thứ liên quan Android/Firebase/mạng chưa được chạy.

| Mục đề | Cài đặt chính | Đã kiểm chứng | Chưa kiểm chứng |
|---|---|---|---|
| 1. Hai loại người dùng | `BaseShellActivity`, `nav_customer/officer`, `OfficerViewModel`, `firestore.rules` | Tài nguyên/ViewBinding khớp (script đối chiếu) | Chạy thật, Rules |
| 2. Đăng nhập | `LoginActivity`, `AuthRepository` | – | Đăng nhập với Firebase |
| 2. eKYC | `FaceCaptureDialogFragment`, `KycRepository`, `KycQueueFragment` | – | Camera, ML Kit, duyệt |
| 2. 2FA OTP | `OtpManager`, `OtpDialogFragment` | **Test đơn vị OTP đạt** (đúng/sai/hết hạn/khóa/gắn giao dịch) | Giao diện |
| 3. Checking/Saving/Mortgage | `AccountDetailFragment`, `SavingsCalculator`, `MortgageCalculator` | **Test công thức đạt** (PMT, lịch, 2 tuần, tổng gốc = vay) | Giao diện |
| 3. Nhân viên sửa lãi suất | `RatesFragment`, `RateRepository` | – | Chạy thật, Rules |
| 3. Lịch sử giao dịch | `HistoryFragment` | – | Chạy thật |
| 3. Nạp/rút | `CashFragment` | `TransactionRules` đạt test | Chạy thật |
| 4. Kiểm tra giao dịch | `TransactionRules` + `firestore.rules` | **Test luật đạt** | Rules chưa chạy với Emulator |
| 4. Nội bộ / liên ngân hàng | `TransferFragment` | Luật đạt test | Liên ngân hàng là mô phỏng |
| 4. VNPay | `VnPayUrlBuilder`, `VnPayActivity` | **HMAC-SHA512 đạt vector RFC 4231; vòng ký–xác minh đạt** | Cổng VNPay thật (chưa có tài khoản) |
| 4. Stripe | – | – | **Chưa làm** (cần backend) |
| 5. Tiện ích | `UtilityPayFragment`, `utility_catalog.json` | Parser + asset đạt test | Chạy thật; danh mục là mẫu |
| 6. Bản đồ + đường đi bộ | `BranchMapFragment`, `MapViewModel`, `RoutingClient` | **Parser OSRM + Haversine đạt test** | OSRM thật, osmdroid, định vị |
| 7. Chatbot | `ChatIntentEngine`, `ChatViewModel` | **Nhận diện ý định đạt test** | Giao diện; Gemini (chưa có khóa) |
| KT1. Lưu CSDL | Firestore | – | Chạy thật trên project Firebase của nhóm |
| KT2. Lên Google Play | – | – | **Nhóm tự thực hiện** (ngoài phạm vi repo) |
| KT3. Báo cáo | `docs/report/final-report.md` | – | Cần chuyển sang mẫu Khoa + ảnh chụp |
| KT4. Video demo | `docs/report/demo-script.md` | – | **Nhóm tự quay** |

## Yêu cầu báo cáo (Technical Requirements mục 4) → tài liệu

| Đề yêu cầu | Tài liệu | Trạng thái |
|---|---|---|
| Nguyên lý, mẫu, thực hành phát triển phần mềm | `docs/architecture.md` mục 1 | Có |
| Class diagrams | `docs/uml.md` mục 2–4 (model/domain, data/ViewModel, UI) | Có, khớp mã nguồn |
| Entity-relationship database diagram | `docs/database.md` (ER + bảng quan hệ + mô tả collection) | Có |
| Use cases | `docs/uml.md` mục 1 | Có |
| Solution diagrams cho các thành phần, thiết kế hạ tầng | `docs/architecture.md` mục 2 | Có |
| Implementation: cấu trúc mã và công nghệ | `README.md`, `docs/architecture.md` mục 0, `docs/report/final-report.md` mục 3 | Có |
| User Interface: trình bày mọi chức năng | `docs/report/final-report.md` mục 4 | **Thiếu ảnh chụp màn hình thật** |
| Video demo | `docs/report/demo-script.md` | **Nhóm tự quay** |
| Báo cáo theo mẫu Khoa, mục lục, tài liệu tham khảo | `docs/report/final-report.md` mục 6 | Có nội dung; **cần chuyển sang mẫu Khoa** |
