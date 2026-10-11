# Tạo repository GitHub cho đồ án cuối kỳ

Giải nén ZIP, rồi trong thư mục dự án:

```bash
git init -b main
git add .
git status            # KIỂM TRA: không có google-services.json, local.properties, serviceAccountKey.json, *.jks
git commit -m "chore: khởi tạo đồ án cuối kỳ TDT Bank"
git remote add origin https://github.com/<tài-khoản>/tdtbank-final.git
git push -u origin main
```

Gợi ý quy trình nhóm (giống kho giữa kỳ): nhánh `main` ổn định; mỗi thành viên làm trên `dev/<tên>`; gộp bằng Pull Request.
Sau khi tạo project Firebase, **không** đẩy `app/google-services.json` lên kho công khai.

Nếu lỡ commit nhầm khóa/bí mật: đổi (rotate) khóa ngay trong Firebase/Google Cloud, rồi mới xóa khỏi lịch sử Git.

Chưa có tệp `LICENSE`; nhóm tự chọn giấy phép (hoặc để kho riêng tư) theo quy định của môn học.
