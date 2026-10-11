# Ma trận phân quyền (khớp `firestore.rules`)

| Tài nguyên | Khách hàng (ACTIVE) | Nhân viên (ACTIVE) | Người bị LOCKED |
|---|---|---|---|
| Đọc hồ sơ `users` | Của mình | Tất cả | Chỉ của mình (để app báo bị khóa) |
| Tạo `users` | ✗ | Chỉ role CUSTOMER | ✗ |
| Sửa `users` | `phone, contactEmail, address`; nộp eKYC (`kycStatus`→PENDING) | `fullName, phone, contactEmail, address, nationalId, dateOfBirth, status, kycStatus` của khách | ✗ |
| `kyc` | Đọc/ghi của mình (chỉ PENDING) | Đọc tất cả; ghi/duyệt | ✗ |
| Đọc `accounts` | Của mình | Tất cả | ✗ |
| Tạo `accounts` | ✗ | CHECKING/SAVING/MORTGAGE hợp lệ | ✗ |
| Sửa `accounts` | Chỉ `balance`+`lastTxId` kèm giao dịch khớp | Chỉ `interestRate`, `status` | ✗ |
| `accountDirectory` | Đọc | Đọc, tạo | ✗ |
| `transactions` | Tạo giao dịch của mình; đọc giao dịch mình tham gia | Đọc tất cả | ✗ |
| `savingRatePolicies` | Đọc | Đọc, tạo/sửa (0–20%) | ✗ |
| `branches` | Đọc | Đọc/ghi | ✗ |

Phía ứng dụng, `BaseShellActivity` còn đăng xuất ngay nếu hồ sơ real-time cho thấy tài khoản bị khóa hoặc sai vai trò.
Hồ sơ **nhân viên** không tạo được từ ứng dụng; chỉ qua `scripts/seed-demo-data.js` (Admin SDK).
