# Chạy bằng Docker

Docker giúp cả nhóm có cùng môi trường mà không phụ thuộc máy cá nhân. Có hai phần độc lập:

| Phần | Tệp | Dùng để |
|---|---|---|
| Firebase Local Emulator | `docker-compose.yml`, `docker/firebase/Dockerfile` | Chạy Auth + Firestore + **`firestore.rules` thật của dự án** trên máy, không cần Firebase project |
| Build Android | `docker/android/Dockerfile` | Chạy unit test + build APK debug với JDK 17 + Android SDK 36 |

Yêu cầu: Docker Desktop (Windows/macOS) hoặc Docker Engine + Compose v2 (Linux). Các lệnh dưới chạy được trên PowerShell lẫn bash; trên Linux/macOS/WSL có thể dùng `make` (xem `Makefile`).

## 1. Firebase Emulator + dữ liệu demo

```bash
docker compose up -d --build firebase   # lần đầu mất vài phút để tải emulator
docker compose run --rm seed            # nạp officer1 / customer1 / customer2, tài khoản, lãi suất, chi nhánh
```

- Emulator UI: <http://localhost:4000> (xem/sửa dữ liệu Firestore, người dùng Auth).
- Cổng: Firestore `8080`, Auth `9099`, Hub `4400`.
- Dữ liệu nằm trong RAM: `docker compose down` là mất, chạy lại `seed` khi khởi động lại.
- Sửa `firestore.rules` → `docker compose restart firebase` để nạp lại rules.

## 2. Cho app kết nối Emulator

Thêm vào `local.properties`:

```properties
firebase.emulatorHost=10.0.2.2
```

rồi **Sync Gradle** và chạy bản *debug* từ Android Studio.

- `10.0.2.2` là máy host nhìn từ Android Emulator.
- Thiết bị thật qua USB: đặt `firebase.emulatorHost=127.0.0.1` và chạy `adb reverse tcp:8080 tcp:8080` và `adb reverse tcp:9099 tcp:9099`.
- Không cần `app/google-services.json` khi dùng Emulator: app tự khởi tạo Firebase với project `demo-tdtbank` (`FirebaseClient.init`).
- Bản *release* luôn dùng Firebase thật; HTTP tới emulator chỉ được phép trong bản debug (`app/src/debug/res/xml/network_security_config.xml`).
- Bỏ dòng `firebase.emulatorHost` (hoặc để trống) để quay lại Firebase thật.

## 3. Build APK + unit test trong Docker

```bash
# Firebase thật (dùng app/google-services.json nếu có)
docker build -f docker/android/Dockerfile --output .build-outputs .

# Bản debug trỏ tới Firebase Emulator
docker build -f docker/android/Dockerfile --build-arg FIREBASE_EMULATOR_HOST=10.0.2.2 --output .build-outputs .
```

Kết quả: `.build-outputs/apk/app-debug.apk` và `.build-outputs/test-results/` (XML JUnit). Build thất bại nếu có test lỗi.
Cài APK lên emulator: `adb install -r .build-outputs/apk/app-debug.apk`.

Ghi chú:
- Lần build đầu tải Android SDK (~1 GB) và thư viện Gradle; các lần sau dùng cache của BuildKit.
- `sdk.dir` trong `local.properties` (đường dẫn của máy host) được bỏ qua trong container; các khóa VNPay/Gemini vẫn được đọc.
