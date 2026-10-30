# Changelog

All notable changes to NativeLoggerSDK are documented here.
Format based on [Keep a Changelog](https://keepachangelog.com).

## [1.0.1] — 2026-10-29

### Added
- Complete Layer 1 Core Infrastructure implementation for NativeRuntime ecosystem.
- Integrated `com.github.infoadmstudio2020-cyber:NativeRuntimeContracts:v1.0.1` universal interfaces.
- Implemented `NativeLogger` thread-safe singleton state machine with `AtomicReference<RuntimeState>`.
- Implemented multi-sink fan-out architecture:
  - `LogcatSink` with Android runtime detection and headless JVM fallback.
  - `FileLogSink` with rolling byte-size caps, file rotation, and automated cleanup.
  - `WebConsoleMirrorSink` with thread-safe live event listeners for DevTools streaming.
  - `RemoteDiagnosticSink` with memory queue buffer and eviction handling.
- Implemented formatters:
  - `PatternLogFormatter` with regex-based PII masking for emails, phone numbers, credit cards, and passwords.
  - `JsonLogFormatter` with full JSON escaping for structured logging.
- Implemented `LoggerCommandHandler` (`IApiHandler`) for NativeRouterSDK RPC dispatch.
- Implemented `NativeLoggerSDK` lifecycle facade and `NativeLoggerSDKConfig.Builder`.
- Implemented comprehensive automated JVM unit test suite with 100% pass rate.
- Interactive companion demo application in `:app` module with live log viewer and controls.
- Enhanced GitHub Release workflow (`release.yml`) with:
  - Explicit `permissions: contents: write` to prevent `Resource not accessible by integration` error.
  - Upgrade to `softprops/action-gh-release@v2` running on Node 20 to eliminate deprecation warnings.
  - Automated release artifact bundling: Release AAR, Demo Release APK, and Demo Release AAB.
- JitPack cloud build and GitHub Actions CI/CD workflows validated.

## [Unreleased]

### Planned
- Additional features
- Performance improvements
