# Thiết lập Firebase (gói Spark – miễn phí)

Làm lần lượt:

1. **Tạo project**: https://console.firebase.google.com → *Add project*. Không cần bật Google Analytics.
2. **Thêm ứng dụng Android**: package name **`vn.edu.tdtu.tdtbank`** (đúng như `applicationId`). Tải `google-services.json` và đặt vào **`app/google-services.json`** (file bị `.gitignore`, **không commit**).
   - Khi phát hành lên Google Play, thêm **SHA-1/SHA-256 của khóa ký** vào app Android trong Firebase.
3. **Authentication** → *Sign-in method* → bật **Email/Password**.
4. **Firestore Database** → *Create database* → chọn vùng gần (ví dụ `asia-southeast1`) → chế độ **production**.
5. **Triển khai Rules**: dán nội dung `firestore.rules` vào *Firestore → Rules → Publish* (hoặc `firebase deploy --only firestore:rules` nếu đã cài Firebase CLI).
6. **Nạp dữ liệu demo**:
   - Console → *Project settings* → *Service accounts* → *Generate new private key* → lưu thành `scripts/serviceAccountKey.json` (bị `.gitignore`).
   - `cd scripts && npm install && npm run seed`.
   - Script tạo: 1 nhân viên, 2 khách hàng, 4 tài khoản, bảng lãi suất, 5 chi nhánh hư cấu (xem `sample-data/README.md`).
7. Build và đăng nhập thử.

## Lưu ý

- Hồ sơ **nhân viên** chỉ tạo được bằng script seed (rules không cho app tạo nhân viên) — đây là chủ ý để không ai tự phong quyền.
- Gói Spark có hạn mức đọc/ghi hằng ngày; đủ cho demo.
- Nếu thiếu `google-services.json`, app vẫn build nhưng màn hình đăng nhập báo "chưa cấu hình Firebase" thay vì crash.
- Quên đổi mật khẩu demo trước khi công khai app là rủi ro bảo mật — hãy đổi hoặc xóa tài khoản demo.
