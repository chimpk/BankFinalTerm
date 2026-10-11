# Giới hạn đã biết (đọc trước khi demo hoặc phát hành)

Danh sách này nhằm tránh hiểu nhầm về mức độ "thật" của từng tính năng.

## Bảo mật và 2FA
1. **OTP là SMS mô phỏng.** Mã được sinh trên máy, hiển thị trong hộp thoại và không đi qua SMS/email. Nó chứng minh luồng 2FA nhưng **không tăng bảo mật thật** vì kẻ tấn công có APK vẫn thấy mã. OTP thật cần máy chủ (Cloud Functions + nhà cung cấp SMS/email).
2. **Rules không xác minh được OTP/khuôn mặt**; chỉ bắt buộc cờ `otpVerified`/`faceVerified` = true. Một client bị sửa đổi có thể đặt cờ giả. Giao dịch vẫn phải đúng số dư, đúng chủ sở hữu, khớp số tiền.
3. **Nạp tiền (DEPOSIT) có thể bị "đúc tiền"** bởi client độc hại, vì rules không xác minh được cổng thanh toán đã thu tiền. Khắc phục thật: máy chủ nhận IPN của VNPay rồi mới cộng tiền (cần gói Blaze).
4. **Hạn mức ngày (500 triệu)** chỉ kiểm tra phía ứng dụng.
5. **eKYC không so khớp khuôn mặt.** ML Kit chỉ xác nhận có đúng một khuôn mặt và (khi yêu cầu) nụ cười – một thử thách "sống" rất cơ bản, có thể bị qua mặt bằng ảnh/video. Việc gắn ảnh với danh tính thật do **nhân viên đối chiếu giấy tờ**. Khi giao dịch giá trị cao, app yêu cầu hồ sơ đã được nhân viên duyệt **và** chụp lại một ảnh sống nhưng **không so sánh với ảnh đã đăng ký**.
6. Ảnh khuôn mặt lưu Base64 trong Firestore (không có Storage); nhân viên đọc được ảnh mọi khách hàng. Dữ liệu sinh trắc là dữ liệu nhạy cảm — phát hành thật cần chính sách quyền riêng tư và căn cứ pháp lý.
7. `VNPAY_HASH_SECRET` và `GEMINI_API_KEY` nằm trong APK.
8. Quên mật khẩu: chưa có luồng tự đặt lại (email bí danh không nhận thư); nhân viên cần hỗ trợ.

## Nghiệp vụ mô phỏng
9. **Chuyển liên ngân hàng** chỉ trừ tiền và ghi nhận; không có NAPAS/ngân hàng thật.
10. **Tiện ích** (điện, nước, nạp ĐT, vé, khách sạn, TMĐT): không tra cứu hóa đơn thật; người dùng nhập mã/số tiền hoặc chọn danh mục mẫu.
11. **Giải ngân khoản vay** không cộng tiền vào tài khoản thanh toán; khoản vay chỉ ghi dư nợ để minh họa lịch trả nợ. Lãi suất trả nợ tính theo niên kim; nếu trả sớm/trễ không tính phạt.
12. Lợi nhuận tiết kiệm là **lãi đơn** hiển thị; không có tác vụ tự cộng lãi vào số dư hằng tháng.
13. Đổi họ tên khách hàng **không** cập nhật `ownerName` trong `accounts`/`accountDirectory` đã tạo.
14. Giao dịch thất bại không được lưu (chỉ giao dịch COMPLETED).

## Hạ tầng
15. Tạo khách hàng gồm 2 bước (Auth rồi Firestore) không nguyên tử → có thể còn Auth user mồ côi nếu bước 2 lỗi.
16. OSRM công cộng, OSM tile: không có SLA; có thể chậm hoặc chặn nếu lạm dụng.
17. Chưa có giao diện tiếng Anh, chưa tối ưu dark mode, chưa có test giao diện (instrumented). CI (GitHub Actions) chỉ chạy unit test + build APK.
18. Số lượng chi nhánh, lãi suất mẫu, tên người là dữ liệu hư cấu.

## Việc nhóm phải tự làm
- Tạo Firebase project, cấu hình, chạy seed.
- Build, sửa lỗi biên dịch nếu có, chụp ảnh màn hình, quay video demo.
- Đưa ứng dụng lên Google Play (ngoài phạm vi repo).
- Chuyển báo cáo sang **mẫu của Khoa** (không có trong dữ liệu được cung cấp).
