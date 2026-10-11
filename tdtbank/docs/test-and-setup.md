# Kiểm thử và thiết lập

## 1. Những gì đã được kiểm chứng thật (môi trường soạn thảo, JDK 21, không có Android SDK)

| Kiểm tra | Cách chạy | Kết quả |
|---|---|---|
| Unit test thuần Java: 38 test | `javac` + runner JUnit tối giản tự viết (**không phải Gradle**), `org.json` biên dịch từ mã nguồn JSON-java | **38/38 đạt** |
| Tài nguyên: mọi `R.*`, `@string/…`, `@drawable/…`, id, menu, nav, array được dùng đều có định nghĩa | script `tools/check_resources.py` (xem mục 4) | **0 thiếu** |
| ViewBinding: mọi trường `binding.xxx` trong Java có `android:id` tương ứng trong layout | cùng script | **0 sai** |
| XML (layout, menu, nav, values, drawable, manifest) đúng cú pháp | `xml.dom.minidom` | **Đạt** |
| Cú pháp `scripts/seed-demo-data.js` | `node --check` | **Đạt** |
| Cân bằng ngoặc `firestore.rules` | script đếm ngoặc | **Cân bằng** (không phải kiểm tra ngữ nghĩa) |

Chi tiết 38 test: `TransactionRulesTest` (10), `OtpManagerTest` (5), `CalculatorsTest` (8), `ChatIntentEngineTest` (2), `VnPayUrlBuilderTest` (4), `ValidationUtilsTest` (5), `RemoteParsersTest` (4).

## 2. Những gì CHƯA được kiểm chứng (cần làm trên máy của nhóm)

- `./gradlew assembleDebug` / Gradle sync (phiên bản thư viện chưa tải được để xác minh; AGP 8.10.1, Gradle 8.11.1, BoM 33.7.0, osmdroid 6.1.18, ML Kit 16.1.7 là các phiên bản đã chọn từ hiểu biết, **cần sync để xác nhận**).
- Biên dịch Java với thư viện AndroidX/Firebase/Material thật (có thể còn lỗi cú pháp API hoặc import).
- Chạy trên emulator/thiết bị; camera, ML Kit, định vị, osmdroid, OSRM.
- `firestore.rules` với Firebase Emulator hoặc project thật.
- `scripts/seed-demo-data.js` với project thật.
- VNPay sandbox, Gemini.

> Nếu Gradle sync/build báo lỗi, hãy gửi nguyên văn thông báo lỗi; các lỗi dạng "cannot find symbol"/phiên bản thư viện sẽ được sửa nhanh.

## 3. Danh sách test thủ công cần chạy (kết quả: **CHƯA CHẠY**)

| # | Kịch bản | Kỳ vọng |
|---|---|---|
| T1 | Đăng nhập `customer1/customer123` | Vào giao diện khách hàng, thấy 3 tài khoản |
| T2 | Đăng nhập `officer1/officer123` | Vào giao diện nhân viên, thấy danh sách khách |
| T3 | Đăng nhập sai mật khẩu | Thông báo lỗi tiếng Việt, không vào |
| T4 | Nhân viên khóa `customer2`, đăng nhập lại | Bị từ chối "Tài khoản đã bị khóa" |
| T5 | Chuyển 1.000.000 ₫ `1000000001`→`1000000011` + OTP đúng | Số dư nguồn −1.000.000, đích +1.000.000, có 2 tài khoản thấy giao dịch |
| T6 | Như T5 nhưng OTP sai 3 lần | Bị khóa lượt, phải gửi lại mã |
| T7 | Chuyển vượt số dư | Báo "Số dư không đủ", không ghi gì |
| T8 | Chuyển liên ngân hàng 500.000 ₫ | Trừ thêm phí 7.700 ₫ |
| T9 | Chuyển 15.000.000 ₫ khi chưa VERIFIED | Báo cần eKYC |
| T10 | Khách nộp eKYC → nhân viên duyệt → chuyển 15.000.000 ₫ | Hỏi mỉm cười + OTP, thành công |
| T11 | Nạp 500.000 ₫ qua cổng SANDBOX | Số dư tăng, giao dịch gateway=SANDBOX |
| T12 | Rút tiền vượt số dư | Từ chối |
| T13 | Trả kỳ nợ tiếp theo của khoản vay | Checking −kỳ, dư nợ −phần gốc, kỳ kế tiếp tăng |
| T14 | Nhân viên sửa lãi suất 12 tháng = 6% + áp dụng cho sổ hiện có | Lợi nhuận/tháng của `1000000002` đổi |
| T15 | Thanh toán tiền điện / nạp điện thoại / vé máy bay | Trừ tiền, xuất hiện trong lịch sử |
| T16 | Bản đồ → định vị → đường đi bộ tới chi nhánh gần nhất | Hiện tuyến + km + phút (đặt vị trí emulator ở TP.HCM) |
| T17 | Chatbot: "xem giao dịch tháng này", "thanh toán tiền điện" | Tóm tắt tháng / nút mở màn hình thanh toán |
| T18 | Tắt mạng rồi mở app | Thông báo lỗi mạng, không crash |
| T19 | Thử gọi Firestore trực tiếp để tự cộng tiền (Emulator) | Rules từ chối |

## 4. Công cụ kiểm tra dùng trong quá trình soạn (không nằm trong app)

`tools/check_resources.py` (đối chiếu tài nguyên: `python3 tools/check_resources.py`) `tools/check_imports.py` (phát hiện tên lớp dùng mà chưa import) và `tools/gen_layouts.py` (đã dùng để sinh phần lớn layout; sửa layout trực tiếp là được).
Việc chạy 38 unit test bằng runner tối giản không đóng gói kèm; nhóm hãy dùng `./gradlew test`.

## 5. Thiết lập môi trường

- Android Studio bản hiện hành, **JDK 17**, Android SDK Platform 36.
- Emulator: đặt **vị trí giả lập ở TP.HCM** (Extended controls → Location, ví dụ 10.7326, 106.6996) vì chi nhánh mẫu ở TP.HCM. Emulator có camera ảo để thử eKYC.
- Thiết bị thật cần Google Play services (định vị, ML Kit).
