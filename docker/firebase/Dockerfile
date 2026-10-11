# Firebase Local Emulator Suite (Auth + Firestore + UI) cho phát triển/demo không cần Firebase project thật.
FROM node:22-bookworm-slim

# firebase-tools mới yêu cầu Java 21+ để chạy emulator.
COPY --from=eclipse-temurin:21-jre /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk \
    PATH=/opt/java/openjdk/bin:${PATH}

ARG FIREBASE_TOOLS_VERSION=14
RUN npm install -g --no-audit --no-fund firebase-tools@${FIREBASE_TOOLS_VERSION} \
    && firebase setup:emulators:firestore \
    && firebase setup:emulators:ui \
    && npm cache clean --force

WORKDIR /workspace
EXPOSE 4000 4400 8080 9099

HEALTHCHECK --interval=5s --timeout=3s --start-period=30s --retries=20 \
    CMD node -e "fetch('http://127.0.0.1:4400/emulators').then(r=>process.exit(r.ok?0:1)).catch(()=>process.exit(1))"

CMD ["firebase", "emulators:start", "--project", "demo-tdtbank", "--only", "auth,firestore,ui"]
