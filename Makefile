# Lối tắt cho Linux/macOS/WSL. Trên Windows PowerShell, chạy trực tiếp các lệnh tương ứng (docs/setup/docker.md).
OUT := .build-outputs

.PHONY: apk apk-emulator firebase seed firebase-down test check

apk:            ## Build APK debug + unit test trong Docker (Firebase thật qua app/google-services.json)
	docker build -f docker/android/Dockerfile --output $(OUT) .

apk-emulator:   ## Build APK debug trỏ tới Firebase Emulator (10.0.2.2 = máy host nhìn từ Android Emulator)
	docker build -f docker/android/Dockerfile --build-arg FIREBASE_EMULATOR_HOST=10.0.2.2 --output $(OUT) .

firebase:       ## Chạy Firebase Emulator (UI: http://localhost:4000)
	docker compose up -d --build firebase

seed: firebase  ## Nạp dữ liệu demo vào Emulator
	docker compose run --rm seed

firebase-down:
	docker compose down

test:           ## Unit test bằng Gradle trên máy (cần JDK 17 + Android SDK)
	./gradlew testDebugUnitTest

check:          ## Kiểm tra tĩnh tài nguyên/import và cú pháp script seed
	python3 tools/check_resources.py
	python3 tools/check_imports.py
	node --check scripts/seed-demo-data.js
