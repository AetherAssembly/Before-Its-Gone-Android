# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog and this project uses semantic versioning.

## [1.0.0-beta.1] - Unreleased

### Added

- **Three-module architecture:** `domain` (pure Kotlin/JVM, zero Android dependencies), `data` (Android library), and `app` (application module). `domain` is testable with plain JUnit 5 on the JVM — no emulator required.
- **Two product flavors:** `play` (ML Kit barcode scanning, targets Google Play) and `foss` (ZXing `MultiFormatReader`, no Google dependencies, targets F-Droid and IzzyOnDroid). Both share the same CameraX `ImageAnalysis` capture pipeline.
- **Inventory CRUD:** add, edit, delete, and log-as-wasted. Items carry name, quantity, location, barcode, expiry date, shelf life, category, low-stock depletion threshold, and tags.
- **Expiry status:** `CalculateExpiryStatusUseCase` classifies each item as Fresh, Expiring Soon (within configurable warning window), or Expired. Status badges shown on every inventory card.
- **Add/Edit item screen:** Material 3 `DatePickerDialog` for expiry date; location `ExposedDropdownMenuBox` (Fridge / Freezer / Pantry); depletion threshold field; barcode field with inline scan button; form pre-filled from barcode profile cache on scan.
- **Barcode scanner screen:** CameraX `PreviewView` + `ImageAnalysis` pipeline; runtime camera permission request via `rememberLauncherForActivityResult`; torch toggle; manual barcode entry fallback. Scanned barcode written to `savedStateHandle` and consumed by the Add/Edit ViewModel.
- **Shopping list:** auto-derives low-stock items from inventory (quantity ≤ depletion threshold). Manual items added via bottom sheet. Check-off persisted in Room; `sourceItemId` distinguishes derived items from manual ones. Clear-checked action.
- **Waste log:** entries created when marking an inventory item as wasted. Canvas bar chart groups waste by category (up to 6). Clear all with confirm dialog.
- **Recipe suggestions:** queries TheMealDB `/filter.php` for each expiring ingredient (capped at 5 API calls). Meal cards show a Coil thumbnail and matched-ingredient chips. Tapping a card opens `https://www.themealdb.com/meal/{id}` directly.
- **Notifications:** three channels — `expiring`, `expired`, `low_stock` — created on app start. Daily `PeriodicWorkRequest` via WorkManager fires at a user-configured time (default 08:00). Each per-type toggle respected at post time. `ExpiryCheckWorker` is a `@HiltWorker` `CoroutineWorker`.
- **Settings screen:** inventory defaults (location, expiry warning days); notification toggles + `TimePicker` dialog; appearance (System / Light / Dark theme override, dynamic color on API 31+); cloud sync (Supabase URL + anon key, last-synced-at, Sync Now); data (Export JSON, Export CSV, Import via SAF); About (version, GitHub link, AGPL-v3).
- **Cloud sync:** Supabase-kt integration with last-write-wins conflict resolution on `updatedAt`, matching the desktop app's algorithm. `SyncEngine` builds the client on-demand from `SettingsDataStore` so URL changes take effect without restart. No-op when sync is disabled or URL is blank.
- **JSON export/import:** full-fidelity round-trip, byte-compatible with Before It's Gone desktop v1.3.0. Photos base64-encoded on export; `data:image/...;base64,` prefix stripped on import from desktop.
- **CSV export/import:** fixed column order on export; case-insensitive headers and flexible date formats on import. `BuildExportCsvUseCase` and `ParseInventoryCsvUseCase` in the `domain` module.
- **Room database v3:** five tables — `inventory_items` (indexed on `expiresAt`), `barcode_profiles`, `waste_log` (append-only), `item_history` (barcode pre-fill cache), `shopping_list`. `MIGRATION_1_2` adds `item_history`; `MIGRATION_2_3` adds `shopping_list`. `StringListConverter` handles `tags` and `allergens` as JSON arrays.
- **Hilt DI:** `DatabaseModule`, `NetworkModule`, `RepositoryModule` in `data/di/`. Flavor-scoped `ScannerModule` in `app/src/play/` and `app/src/foss/` provides the `BarcodeAnalyzerFactory` binding per flavor.
- **WorkManager + Hilt wiring:** `BeforeItsGoneApp` implements `Configuration.Provider` and supplies `HiltWorkerFactory`, so `@HiltWorker` dependencies are injected correctly.
- **Material You dynamic color:** seed color `#E8540A` (carrot orange). Dynamic color enabled by default on API 31+; configurable in Settings.
- **Domain unit tests (JUnit 5):** `CalculateExpiryStatusUseCaseTest` (7 cases including millisecond-boundary), `PredictShelfLifeUseCaseTest`, `ParseInventoryJsonUseCaseTest`, `ParseInventoryCsvUseCaseTest`, `BuildExportJsonUseCaseTest`, `ResolveSyncConflictsUseCaseTest`, `GetShoppingListUseCaseTest`.
- **DAO integration tests (Robolectric + in-memory Room):** `InventoryItemDaoTest` (6 cases), `WasteLogDaoTest` (4 cases), `BarcodeProfileDaoTest` (4 cases). JUnit 4 via `junit-vintage-engine` alongside JUnit 5 in the same test run.
- **CI workflow:** GitHub Actions — PR check runs lint + both-flavor compile + full test suite; release workflow builds signed APKs for both flavors on every version tag, plus a Play Store AAB for stable releases only, and attaches SHA-256 checksums to the GitHub Release. Additional workflows: label sync, PR size labelling, stale issue management.
