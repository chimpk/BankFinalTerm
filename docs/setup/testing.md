# Kiểm thử

## 1. Cách chạy

| Mục | Lệnh |
|---|---|
| Unit test JVM (38 test: `TransactionRulesTest` 10, `OtpManagerTest` 5, `CalculatorsTest` 8, `ChatIntentEngineTest` 2, `VnPayUrlBuilderTest` 4, `ValidationUtilsTest` 5, `RemoteParsersTest` 4) | `./gradlew testDebugUnitTest` |
| Build APK debug | `./gradlew assembleDebug` → `app/build/outputs/apk/debug/` |
| Cả hai trong Docker (không cần Android Studio) | `docker build -f docker/android/Dockerfile --output .build-outputs .` |
| Kiểm tra tĩnh tài nguyên/ViewBinding/import + cú pháp seed | `python3 tools/check_resources.py`, `python3 tools/check_imports.py`, `node --check scripts/seed-demo-data.js` |
| CI | GitHub Actions (`.github/workflows/ci.yml`) chạy tất cả mục trên ở mỗi push/PR và đính kèm APK + kết quả test |

## 2. Trạng thái kiểm chứng

| Kiểm tra | Kết quả |
|---|---|
| 34 unit test `domain/` + `utils/` biên dịch bằng `javac` + JUnit 4.13.2 (không có Android SDK) | **34/34 đạt** |
| 4 test `RemoteParsersTest` (cần `org.json`) | Đạt ở bản trước; chạy lại bằng Gradle/CI |
| `tools/check_resources.py`, `tools/check_imports.py` | **0 lỗi** |
| `node --check scripts/seed-demo-data.js` | **Đạt** |
| `./gradlew assembleDebug` với AndroidX/Firebase/Material thật | **Chưa xác nhận** – chạy CI hoặc Docker để xác nhận |
| Chạy trên emulator/thiết bị; camera, ML Kit, định vị, osmdroid, OSRM | Chưa chạy |
| `firestore.rules` với Firebase Emulator | Có sẵn môi trường (`docker compose up firebase`), chưa chạy kịch bản T19 |
| VNPay sandbox, Gemini | Chưa có khóa |

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

## 4. Công cụ kiểm tra (không nằm trong app)

`tools/check_resources.py` đối chiếu mọi `R.*`/`@…` và trường ViewBinding với tài nguyên; `tools/check_imports.py` phát hiện tên lớp dùng mà chưa import. Cả hai trả mã lỗi ≠ 0 khi có vấn đề (dùng trong CI).

## 5. Thiết lập môi trường

- Android Studio bản hiện hành, **JDK 17**, Android SDK Platform 36.
- Emulator: đặt **vị trí giả lập ở TP.HCM** (Extended controls → Location, ví dụ 10.7326, 106.6996) vì chi nhánh mẫu ở TP.HCM. Emulator có camera ảo để thử eKYC.
- Thiết bị thật cần Google Play services (định vị, ML Kit).
