# Architecture

Before It's Gone for Android is a native Kotlin application built with Jetpack Compose and a strict three-module architecture designed for testability, long-term maintainability, and eventual Kotlin Multiplatform readiness.

---

## Module layout

```
Before-Its-Gone-Android/
├── domain/         # Pure Kotlin — zero Android dependencies
│   ├── model/      # InventoryItem, WasteLogEntry, BarcodeProfile, ShoppingListItem,
│   │               # ItemHistory, AppSettings, ExpiryStatus
│   ├── usecase/    # CalculateExpiryStatus, PredictShelfLife, ParseInventoryJson/Csv,
│   │               # BuildExportJson/Csv, ResolveSyncConflicts, GetShoppingList
│   └── repository/ # Interfaces only — no implementation, no Room import
│
├── data/           # Android library — implements domain interfaces
│   ├── local/
│   │   ├── db/     # AppDatabase (v3), Room entities, DAOs, TypeConverters, migrations
│   │   └── preferences/  # SettingsDataStore (Jetpack DataStore)
│   ├── remote/
│   │   ├── themealdb/  # TheMealDbClient (Ktor) for recipe suggestions
│   │   └── supabase/   # SyncEngine (push/pull, last-write-wins)
│   ├── repository/ # InventoryRepositoryImpl, SettingsRepositoryImpl,
│   │               # BarcodeProfileRepositoryImpl, WasteLogRepositoryImpl,
│   │               # ShoppingListRepositoryImpl, ItemHistoryRepositoryImpl
│   ├── work/       # ExpiryCheckWorker (WorkManager, Hilt-injected)
│   └── di/         # Hilt modules: DatabaseModule, NetworkModule, RepositoryModule
│
└── app/            # Android application module
    ├── src/main/kotlin/
    │   ├── ui/
    │   │   ├── theme/      # Material 3 dynamic color theme (seed: #E8540A, carrot orange)
    │   │   ├── navigation/ # AppNavHost (8 destinations)
    │   │   └── screen/
    │   │       ├── inventory/   # InventoryScreen + ViewModel (list, waste action)
    │   │       ├── additem/     # AddEditItemScreen + ViewModel (date picker, barcode, threshold)
    │   │       ├── scanner/     # BarcodeScannerScreen + ViewModel (CameraX, runtime permission)
    │   │       ├── shopping/    # ShoppingListScreen + ViewModel (low-stock + manual items)
    │   │       ├── wastelog/    # WasteLogScreen + ViewModel (log + category bar chart)
    │   │       ├── recipes/     # RecipesScreen + ViewModel (TheMealDB suggestions)
    │   │       └── settings/    # SettingsScreen + ViewModel (all preferences + sync + data)
    │   └── BeforeItsGoneApp     # Hilt app, notification channels, WorkManager schedule
    ├── src/play/   # ML Kit BarcodeAnalyzer + flavor Hilt module
    └── src/foss/   # ZXing BarcodeAnalyzer + flavor Hilt module
```

### Dependency graph

```
app → domain ← data
```

`app` and `data` both depend on `domain`. `domain` has no Android imports, making all use cases and models testable with plain JUnit5 on the JVM — no emulator, no instrumented tests.

---

## MVVM + unidirectional data flow

Each screen has a `@HiltViewModel` that exposes `StateFlow<UiState>`. Compose screens collect the flow via `collectAsStateWithLifecycle()` and pass events back to the ViewModel through lambda callbacks. No LiveData.

```
Room DB ──► InventoryRepositoryImpl ──► InventoryViewModel ──► InventoryScreen (Compose)
                                        (StateFlow<UiState>)    collectAsStateWithLifecycle
```

---

## Hilt DI graph

Three Hilt `@Module` objects in `data/di/`:

| Module | Provides |
| --- | --- |
| `DatabaseModule` | `AppDatabase` singleton, all five DAOs |
| `NetworkModule` | `HttpClient` singleton (Ktor + OkHttp engine), `TheMealDbClient` |
| `RepositoryModule` | Binds all six `*RepositoryImpl → *Repository` pairs; provides `DataStore<Preferences>` |

`@HiltWorker` on `ExpiryCheckWorker` allows Hilt to inject dependencies into WorkManager workers via `HiltWorkerFactory`, which is wired up in `BeforeItsGoneApp`.

---

## Room database

Single `AppDatabase`, **version 3**. Schema exported to `data/schemas/` for migration tracking.

| Entity | Table | Added in | Notes |
| --- | --- | --- | --- |
| `InventoryItemEntity` | `inventory_items` | v1 | Indexed on `expiresAt`. `tags` stored as JSON via `StringListConverter`. |
| `BarcodeProfileEntity` | `barcode_profiles` | v1 | PK is the barcode string. `allergens` stored as JSON. |
| `WasteLogEntity` | `waste_log` | v1 | Append-only log, ordered by `wastedAt DESC`. |
| `ItemHistoryEntity` | `item_history` | v2 | Tracks name/location/shelf-life history for barcode pre-fill. |
| `ShoppingListItemEntity` | `shopping_list` | v3 | `sourceItemId` null = manual item, non-null = derived from inventory. |

Migrations: `MIGRATION_1_2` adds `item_history`; `MIGRATION_2_3` adds `shopping_list`.

---

## WorkManager background job

`ExpiryCheckWorker` is a `CoroutineWorker` scheduled as a daily `PeriodicWorkRequest` with `ExistingPeriodicWorkPolicy.KEEP`. It:

1. Reads all inventory items
2. Calls `calculateExpiryStatus()` for each
3. Posts Android notifications to the appropriate channel (`expiring`, `expired`, `low_stock`)

The notification time-of-day is user-configurable (stored in DataStore). WorkManager uses inexact scheduling by default — `SCHEDULE_EXACT_ALARM` is deliberately not requested.

---

## CameraX and flavor-split barcode scanning

Both flavors use `CameraX ImageAnalysis` as the camera capture layer. The `BarcodeAnalyzer` implementation differs per product flavor:

| Flavor | Analyzer | Dependency |
| --- | --- | --- |
| `play` | ML Kit `BarcodeScanning.getClient()` | `com.google.mlkit:barcode-scanning` |
| `foss` | ZXing `BarcodeReader` | `com.journeyapps:zxing-android-embedded` |

Source sets: `app/src/play/kotlin/...` and `app/src/foss/kotlin/...` each contain a `BarcodeAnalyzer.kt`. The scanner screen is flavor-neutral.

### Barcode → add item flow

```
User taps Scan
  → CameraX PreviewView shown
    → ImageAnalysis feeds frames to flavor-specific BarcodeAnalyzer
      → Barcode string decoded
        → savedStateHandle.set("scanned_barcode", barcode)
          → AddEditItemScreen reads barcode, calls OpenFoodFactsClient.lookupBarcode()
            → OFFProduct returned (name, categories, imageUrl)
              → PredictShelfLifeUseCase(categories, location) → suggested shelf life
                → Form pre-filled (name, category, shelf life, barcode)
```

---

## Supabase cloud sync

`SyncEngine` in `data/remote/supabase/` wraps supabase-kt. Conflict resolution is **last-write-wins on `updatedAt`**, matching the desktop app's algorithm exactly. This means a pull followed by a push will never overwrite a more recently edited item.

The Supabase URL and anon key are stored in `DataStore<Preferences>` (not Room). In a future hardening pass, the anon key can be migrated to `EncryptedSharedPreferences` backed by the Android Keystore.

Sync is user-initiated (Settings → Sync now) by default. The table schema is identical to the desktop (`docs/cloud-sync.md` in the desktop repo).

---

## Import/export

JSON export reads each item's `photoPath`, loads the file from `<filesDir>/photos/<id>.jpg`, and base64-encodes the bytes into the `photo` field of the JSON envelope — making the output byte-compatible with the desktop app's format.

JSON import that contains a `photo` field decodes the base64 (stripping any `data:image/...;base64,` prefix from desktop exports) and writes the file to `<filesDir>/photos/<id>.jpg`, storing the path in Room.

See [import-export-format.md](import-export-format.md) for the full format specification.
