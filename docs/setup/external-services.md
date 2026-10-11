# Dịch vụ ngoài (tùy chọn)

Tất cả khóa đặt trong `local.properties` (bị `.gitignore`); mẫu ở `local.properties.example`. Sau khi sửa, **Sync Gradle** để `BuildConfig` cập nhật.

## VNPay sandbox
1. Đăng ký merchant sandbox: https://sandbox.vnpayment.vn/devreg/ → nhận `TmnCode` và `HashSecret`.
2. Điền `vnpay.tmnCode`, `vnpay.hashSecret`.
3. Màn **Nạp tiền** sẽ có thêm lựa chọn "VNPay (sandbox)". Dùng thẻ thử nghiệm do VNPay công bố trong tài liệu sandbox của họ.
4. Lưu ý: `ReturnUrl` là `https://tdtbank.app/vnpay-return` (app tự chặn URL này trong WebView, không cần sở hữu tên miền); địa chỉ IP gửi lên là `127.0.0.1`.
5. **Bảo mật:** `HashSecret` nằm trong APK → chỉ dùng demo. Thực tế phải ký/xác nhận (IPN) ở máy chủ.
6. **Chưa kiểm thử với VNPay thật.**

## Stripe
Chưa cài đặt. Stripe yêu cầu máy chủ tạo PaymentIntent (cần Cloud Functions → gói Blaze). Đề cho phép "VNPay *hoặc* Stripe" nên bản này chỉ làm VNPay + cổng SANDBOX mô phỏng.

## Gemini (chatbot AI tùy chọn)
1. Lấy khóa tại https://aistudio.google.com → điền `gemini.apiKey`; `gemini.model` mặc định `gemini-2.0-flash` (kiểm tra tên mô hình còn được hỗ trợ; có thể đã đổi).
2. Chỉ dùng khi bộ luật nội bộ không nhận diện được câu hỏi. Khóa nằm trong APK → chỉ demo.
3. **Chưa kiểm thử với API thật.**

## Bản đồ / đường đi bộ
- Ô gạch bản đồ: máy chủ OpenStreetMap (cần `User-Agent` – app đặt bằng package name). Chính sách: https://operations.osmfoundation.org/policies/tiles/
- Định tuyến: `https://routing.openstreetmap.de/routed-foot/` (OSRM, profile đi bộ). Đây là máy chủ công cộng, **không có SLA**; khi lỗi app hiển thị đường chim bay và ghi rõ.
- Nếu phát hành rộng, nên tự dựng máy chủ tile/routing hoặc dùng nhà cung cấp trả phí.
