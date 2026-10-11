# syntax=docker/dockerfile:1.7
# Build APK + chạy unit test trong môi trường cố định (JDK 17 + Android SDK 36), không cần Android Studio.
#   docker build -f docker/android/Dockerfile --output .build-outputs .
#   docker build -f docker/android/Dockerfile --build-arg FIREBASE_EMULATOR_HOST=10.0.2.2 --output .build-outputs .
# Kết quả: .build-outputs/apk/*.apk và .build-outputs/test-results/.

FROM eclipse-temurin:17-jdk-jammy AS sdk
ARG CMDLINE_TOOLS_VERSION=13114758
ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools
RUN apt-get update \
    && apt-get install -y --no-install-recommends unzip curl \
    && rm -rf /var/lib/apt/lists/*
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && curl -fsSL -o /tmp/cmdline-tools.zip \
       https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip \
    && unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools \
    && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm /tmp/cmdline-tools.zip
# Khớp compileSdk 36 trong app/build.gradle.kts và build-tools mặc định của AGP 8.10.
RUN yes | sdkmanager --licenses > /dev/null \
    && sdkmanager --install "platform-tools" "platforms;android-36" "build-tools;35.0.0"

FROM sdk AS build
ARG FIREBASE_EMULATOR_HOST=""
WORKDIR /workspace
COPY . .
# sdk.dir trong local.properties là đường dẫn của máy host (vd. Windows) → bỏ để dùng ANDROID_HOME của image.
RUN if [ -f local.properties ]; then sed -i '/^sdk\.dir/d' local.properties; fi
RUN --mount=type=cache,target=/root/.gradle \
    chmod +x gradlew \
    && ./gradlew --no-daemon --stacktrace \
       -Pfirebase.emulatorHost="${FIREBASE_EMULATOR_HOST}" \
       testDebugUnitTest assembleDebug \
    && mkdir -p /out/apk /out/test-results \
    && cp app/build/outputs/apk/debug/*.apk /out/apk/ \
    && cp -r app/build/test-results/testDebugUnitTest/. /out/test-results/

FROM scratch AS export
COPY --from=build /out/ /
