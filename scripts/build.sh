#!/bin/bash
# Build script for NativeLoggerSDK
set -e

echo "Building NativeLoggerSDK v1.0.0..."
chmod +x gradlew
./gradlew clean :native-logger-sdk:test :native-logger-sdk:assembleRelease :app:assembleDebug
echo "Build complete: native-logger-sdk/build/outputs/aar/"
