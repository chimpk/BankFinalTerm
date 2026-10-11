# Đưa ứng dụng lên Google Play (nhóm tự thực hiện)

Đề yêu cầu ứng dụng được **triển khai trên Google Play** để bất kỳ ai cũng cài được. Bản này đã chuẩn bị cấu hình kỹ thuật, nhưng việc đăng ký và phát hành do nhóm làm.

## Điều kiện kỹ thuật đã đáp ứng trong mã
- Target/compile **API 36** (Google Play yêu cầu ứng dụng mới nhắm API 36 từ 31/08/2026 – hãy xác nhận lại tại trang chính thức vì quy định có thể đổi).
- `applicationId = vn.edu.tdtu.tdtbank` (không dùng `com.example`, Play từ chối).
- `allowBackup=false`; không có quyền thừa (chỉ INTERNET, trạng thái mạng, vị trí).

## Việc cần làm
1. **Tài khoản Google Play Console** (có phí đăng ký một lần; tài khoản cá nhân mới có thể phải chạy *closed test* theo số tester/số ngày mà Google quy định – kiểm tra yêu cầu hiện hành).
2. **Khóa ký:** tạo keystore (`keytool`), dùng *Play App Signing*; **không** commit `.jks/.keystore` (đã trong `.gitignore`).
3. Đổi `versionCode` mỗi lần tải lên; build **App Bundle**: `./gradlew bundleRelease`.
4. Bật `isMinifyEnabled = true` nếu muốn thu nhỏ (đã có `proguard-rules.pro` giữ lớp `model`); **thử lại toàn bộ** sau khi bật vì Firestore ánh xạ POJO bằng reflection.
5. Firebase: thêm SHA-1/SHA-256 của khóa ký (App Signing key) vào ứng dụng Android.
6. **Hồ sơ Store:** mô tả, ảnh chụp, biểu tượng 512×512, **Chính sách quyền riêng tư** (URL công khai – bắt buộc vì app thu thập ảnh khuôn mặt, vị trí, thông tin cá nhân), biểu mẫu **Data safety**, phân loại nội dung.
7. **Chính sách ứng dụng tài chính / thông tin nhạy cảm:** ứng dụng này là sản phẩm học tập, **không phải ngân hàng thật**. Hãy ghi rõ trong mô tả để tránh gây hiểu nhầm, và rà soát chính sách "Financial services" của Google Play; việc dùng tên "Bank" có thể bị xem xét.
8. Xóa hoặc đổi mật khẩu các tài khoản demo trước khi công khai.
