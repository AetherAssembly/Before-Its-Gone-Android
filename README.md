# Before It's Gone — Android

> Track what's in your fridge, freezer, and pantry before it expires.

[![CI](https://img.shields.io/github/actions/workflow/status/AetherAssembly/Before-Its-Gone-Android/ci.yml?label=CI)](https://github.com/AetherAssembly/Before-Its-Gone-Android/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/AetherAssembly/Before-Its-Gone-Android?label=stable)](https://github.com/AetherAssembly/Before-Its-Gone-Android/releases/latest)
[![License: AGPL v3](https://img.shields.io/badge/license-AGPL--3.0-blue)](LICENSE)
[![Play Store](https://img.shields.io/badge/Play_Store-coming_soon-3DDC84?logo=google-play&logoColor=white)](#)
[![F-Droid](https://img.shields.io/badge/F--Droid-coming_soon-1976D2?logo=f-droid&logoColor=white)](#)
[![IzzyOnDroid](https://img.shields.io/badge/IzzyOnDroid-coming_soon-blueviolet)](#)

Offline-first native Android app. No account required; all data stays on your device.

---

## Features

Track expiry dates and get notified before things go off. Includes a shopping list, waste log, recipe suggestions, and opt-in Supabase cloud sync. Dark/light theme with Material You dynamic color. Barcode scanning uses the device camera directly via CameraX — no phone relay or QR code pairing needed.

Everything runs locally; the only outbound requests are Open Food Facts (barcode lookup), TheMealDB (recipe suggestions), and your own Supabase project if you enable sync.

---

## Screenshots

<details>
<summary>Inventory list</summary>

![Inventory list](docs/screenshots/inventory-list.png)

</details>

<details>
<summary>Add item</summary>

![Add item](docs/screenshots/inventory.png)

</details>

<details>
<summary>Waste log</summary>

![Waste log](docs/screenshots/waste-log.png)

</details>

<details>
<summary>Settings</summary>

![Settings](docs/screenshots/settings.png)

</details>

---

## Download

| Distribution | Link |
| --- | --- |
| Google Play | Coming soon |
| F-Droid | Coming soon |
| IzzyOnDroid | Coming soon |
| Direct APK | [Releases page](https://github.com/AetherAssembly/Before-Its-Gone-Android/releases) |

---

## Build from source

**Prerequisites:** Android Studio Meerkat (2024.3.1+) or JDK 17 + Android SDK command-line tools.

```bash
git clone https://github.com/AetherAssembly/Before-Its-Gone-Android.git
cd Before-Its-Gone-Android
chmod +x gradlew
./gradlew assemblePlayDebug   # Play Store flavor (ML Kit barcode)
./gradlew assembleFossDebug   # F-Droid flavor (ZXing barcode)
```

Or open the project root in Android Studio and select a build variant from the Build Variants panel.

### Product flavors

| Flavor | Barcode scanning | Distribution |
| --- | --- | --- |
| `play` | ML Kit (Google) | Google Play, GitHub Releases |
| `foss` | ZXing (fully FOSS) | F-Droid, IzzyOnDroid, GitHub Releases |

Both flavors use CameraX for the camera layer. The `foss` flavor has no Google services dependency, meeting F-Droid's reproducible-build and policy requirements.

---

## Docs

- [Architecture](docs/architecture.md): module layout, MVVM, Hilt DI, Room schema, WorkManager, CameraX flavor split
- [Data Model](docs/data-model.md): `InventoryItem` field reference, Kotlin types, Room columns
- [Import & Export Format](docs/import-export-format.md): JSON/CSV format, compatibility with the desktop app
- [Release Checklist](docs/release-checklist.md): pre-release testing and store submission steps

---

## Privacy

All data is stored locally in Room (SQLite) on your device. The camera is used only for barcode scanning and photo capture; images are processed on-device and never transmitted. Barcode lookups contact Open Food Facts only when you trigger them. Notifications are generated locally. No tracking, no ads, no accounts required.

See [PRIVACY_POLICY.md](PRIVACY_POLICY.md) for full details.

---

## License

[AGPL-3.0](LICENSE)
