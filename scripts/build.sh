#!/bin/bash
# Build script for NativeLoggerSDK
set -e

echo "Building NativeLoggerSDK v1.0.1 (Release AAR, Demo APK, and Demo AAB)..."
chmod +x gradlew
./gradlew clean :native-logger-sdk:test :native-logger-sdk:assembleRelease :app:assembleRelease :app:bundleRelease
echo "Build complete:"
echo " - AAR: native-logger-sdk/build/outputs/aar/"
echo " - APK: app/build/outputs/apk/release/"
echo " - AAB: app/build/outputs/bundle/release/"
