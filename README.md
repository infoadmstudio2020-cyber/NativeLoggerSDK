# NativeLoggerSDK

Multi-sink diagnostic logging framework for Android with Logcat, Rolling File, Web Console Mirror, and Remote Diagnostics sinks, fully integrated with NativeRuntimeContracts (Layer 0).

[![JitPack](https://jitpack.io/v/infoadmstudio2020-cyber/NativeLoggerSDK.svg)](https://jitpack.io/#infoadmstudio2020-cyber/NativeLoggerSDK)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![JDK](https://img.shields.io/badge/JDK-17-orange.svg)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-Apache-2.0-blue.svg)](LICENSE)

---

## Features

- **Multi-Sink Fan-Out Architecture**: Concurrently routes log events to Logcat, rolling encrypted/structured files, Web Console Mirror (for DevTools), and remote diagnostic buffers.
- **NativeRuntime Universal Contracts**: Implements Layer 0 contracts (`ILogger`, `IDisposable`, `RuntimeState`, `LogEntry`, `LogLevel`, `IApiHandler`).
- **Zero-Allocation Fast Path**: Instantly bypasses disabled or lower priority log levels without memory allocations.
- **Sensitive Data & PII Masking**: Built-in pattern formatter masks emails, phone numbers, credit cards, passwords, and tokens.
- **RPC Command Handler**: Embedded `LoggerCommandHandler` ready for zero-latency NativeRouterSDK RPC calls.
- **Companion Demonstration App**: Interactive UI with real-time log stream, sink flushing, and status diagnostics.

---

## Installation

### Step 1 — Add JitPack repository

In your root `settings.gradle` or root `build.gradle`:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2 — Add the dependency

In your module's `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.infoadmstudio2020-cyber:NativeLoggerSDK:1.0.0'
    // Underlying ecosystem contracts
    implementation 'com.github.infoadmstudio2020-cyber:NativeRuntimeContracts:v1.0.1'
}
```

---

## Quick Start

### 1. SDK Facade Initialization

```java
// Configure NativeLoggerSDK
NativeLoggerSDKConfig config = new NativeLoggerSDKConfig.Builder()
    .debugEnabled(BuildConfig.DEBUG)
    .tag("MyApp")
    .minimumLevel(LogLevel.DEBUG)
    .enableFileLogging(true, new File(context.getFilesDir(), "logs"))
    .enableConsoleMirroring(true)
    .enablePiiMasking(true)
    .build();

// Initialize SDK singleton
NativeLoggerSDK.getInstance().init(context, config);

// Log diagnostic messages
NativeLoggerSDK.i("MyApp", "Application successfully booted");
NativeLoggerSDK.d("MyApp", "User token initialized: secret_token_value"); // Automatically masked if PII masking enabled
```

### 2. Multi-Sink Engine Direct API

```java
NativeLogger logger = NativeLogger.getInstance();
logger.setMinimumLevel(LogLevel.INFO);

// Add custom sink
logger.addSink(new WebConsoleMirrorSink());

// Log structured entries
logger.w("Network", "Server response delayed by 1200ms");
logger.e("Database", "Failed to query local cache", exception);

// Flush buffers to disk/network
logger.flush();
```

---

## Public API Reference

| Component | Responsibility |
|-----------|----------------|
| `NativeLoggerSDK` | Primary public facade and Android lifecycle coordinator |
| `NativeLogger` | Thread-safe multi-sink fan-out engine implementing `ILogger` & `IDisposable` |
| `LoggerConfig` / `NativeLoggerSDKConfig` | Fluent builders for log levels, sinks, formatters, and flags |
| `LogcatSink` | Direct bridge to `android.util.Log` with JVM headless fallback |
| `FileLogSink` | Size-capped rolling file sink with automated rotation |
| `WebConsoleMirrorSink` | Real-time mirror for DevTools and WebBridge streaming |
| `RemoteDiagnosticSink` | Diagnostic memory queue with capacity eviction |
| `LoggerCommandHandler` | `IApiHandler` implementation for RPC command routing |
| `PatternLogFormatter` | High-performance log pattern formatter with regex PII redaction |
| `JsonLogFormatter` | Structured JSON log event serializer |

---

## Requirements

- Android minSdk 21+ / compileSdk 34+
- Java 17
- Gradle 8.7+

---

## Changelog

See [CHANGELOG.md](CHANGELOG.md)

---

## License

```
Copyright 2026 infoadmstudio2020-cyber

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

