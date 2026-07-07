# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Initial Android project scaffold: Kotlin 2.x, Jetpack Compose + Material 3, MVVM architecture
- Three-module layout: `app`, `domain` (pure Kotlin, no Android deps), `data` (Room + DataStore + Ktor)
- Hilt dependency injection throughout
- Room database (SQLite) with `inventory_items`, `barcode_profiles`, and `waste_log` tables
- Jetpack DataStore for app settings
- WorkManager integration for background daily expiry checks and notifications
- CameraX integration for barcode scanning and item photo capture
- ML Kit barcode scanning (`play` product flavor — Google Play)
- ZXing barcode scanning (`foss` product flavor — F-Droid / IzzyOnDroid)
- Ktor HTTP client for Open Food Facts barcode lookup and TheMealDB recipe suggestions
- Supabase-kt integration for optional cloud sync (same tables as the desktop app)
- Notification channels: expiring, expired, low-stock
- JSON and CSV import/export, byte-compatible with the Before It's Gone desktop app
- CI workflow: build + unit tests + lint on PR and push to main
- Release workflow: signed APK/AAB on tag, SHA-256 checksums, GitHub Release
- Fastlane metadata structure for Play Store and F-Droid
