# Contributing to Before It's Gone for Android

Thanks for wanting to help! Here's everything you need to know to get started.

---

## Ways to contribute

- **Bug reports:** open a [bug report](https://github.com/AetherAssembly/Before-Its-Gone-Android/issues/new?template=bug_report.yml)
- **Feature requests:** open a [feature request](https://github.com/AetherAssembly/Before-Its-Gone-Android/issues/new?template=feature_request.yml)
- **Code:** fork the repo, make your changes, and open a pull request against `main`
- **Questions / support:** email [support@aetherassembly.org](mailto:support@aetherassembly.org)

---

## Ground rules

- Be respectful and constructive in all discussion.
- Keep changes focused; one logical change per PR.
- Update `CHANGELOG.md` for any user-visible fix or feature (under the current unreleased version, using **bold inline labels** matching the existing style).
- Don't introduce new dependencies without a clear reason; explain the choice in your PR.
- Don't add comments that describe *what* the code does; only *why*, when the reason isn't obvious from the code itself.

---

## Development setup

**Prerequisites:** JDK 17 and the Android SDK (via Android Studio Meerkat 2024.3.1+ or the command-line tools). The Gradle wrapper is included; no global Gradle install needed.

### Clone and build

```bash
git clone https://github.com/AetherAssembly/Before-Its-Gone-Android.git
cd Before-Its-Gone-Android
chmod +x gradlew
./gradlew assemblePlayDebug   # Play Store flavor (ML Kit barcode)
./gradlew assembleFossDebug   # F-Droid flavor (ZXing barcode)
```

Or open the project root in Android Studio and select a build variant from the **Build Variants** panel (e.g. `playDebug` or `fossDebug`).

### Module structure

The repo is a three-module Gradle project:

| Module | Description |
| ------ | ----------- |
| `:domain` | Pure Kotlin/JVM. Domain models, use cases, repository interfaces. Zero Android imports. Testable with plain JUnit 5 on the JVM. |
| `:data` | Android library. Room database, DataStore, WorkManager worker, Supabase sync engine, TheMealDB client, repository implementations. |
| `:app` | Android application. Compose UI, ViewModels, Hilt DI wiring, navigation. |

The dependency graph is: `app → domain ← data`. Never add Android or Hilt imports to `:domain`.

### Product flavors

| Flavor | Barcode scanning | Dependency | Distribution |
| ------ | ---------------- | ---------- | ------------ |
| `play` | ML Kit `BarcodeScanning.getClient()` | `com.google.mlkit:barcode-scanning` | Google Play, GitHub Releases |
| `foss` | ZXing `MultiFormatReader` | `com.journeyapps:zxing-android-embedded` | F-Droid, IzzyOnDroid, GitHub Releases |

Flavor-specific code lives in `app/src/play/` and `app/src/foss/`. The scanner screen and all other code are flavor-neutral and live in `app/src/main/`.

---

## Common commands

```bash
./gradlew assemblePlayDebug        # Build play-flavor debug APK
./gradlew assembleFossDebug        # Build foss-flavor debug APK
./gradlew installPlayDebug         # Build and install play-flavor debug APK on connected device
./gradlew installFossDebug         # Build and install foss-flavor debug APK on connected device
./gradlew :domain:test             # Domain unit tests (JUnit 5)
./gradlew :data:test               # Data module unit + DAO tests (Robolectric)
./gradlew test                     # All tests across all modules
./gradlew lint                     # Lint all variants
./gradlew assemblePlayRelease      # Release APK (requires signing config)
./gradlew bundlePlayRelease        # Release AAB for Play Store upload
./gradlew clean                    # Wipe build outputs
```

---

## Pull request process

1. Fork the repo and create a branch from `main`:

   ```bash
   git checkout -b fix/describe-your-change
   ```

2. Make your changes. Test on at least one physical or emulated device.
3. Run `./gradlew lint`, `./gradlew :domain:test`, and `./gradlew :data:test`; fix anything that fails.
4. Update `CHANGELOG.md` under the current unreleased version heading.
5. Open a PR against `main` using the pull request template. Fill in every section.

PRs that only update docs or GitHub config files don't need device testing notes.

---

## Where things live

Before adding a feature, identify which layer it belongs to:

| What you're adding | Where it goes |
| ------------------ | ------------- |
| A new field on `InventoryItem` | `domain/src/main/kotlin/.../model/InventoryItem.kt` + `InventoryItemEntity.kt` + a Room migration |
| A new use case or pure business logic | `domain/src/main/kotlin/.../usecase/` |
| A new repository interface | `domain/src/main/kotlin/.../repository/` + implementation in `data/` |
| A new Room table | New entity + DAO in `data/local/db/`, increment `AppDatabase` version, add migration |
| A new DataStore setting | `data/local/preferences/SettingsDataStore.kt` + update `AppSettings` or `AndroidSettings` in `domain/model/` |
| A new screen | New `*Screen.kt` + `*ViewModel.kt` in `app/ui/screen/<name>/` + a destination in `AppNavHost.kt` |
| Something flavor-specific | In `app/src/play/` or `app/src/foss/`; expose via an interface in `app/src/main/` |
| A background job | `data/work/` using `@HiltWorker CoroutineWorker`; schedule from `BeforeItsGoneApp` |

See [`docs/architecture.md`](../docs/architecture.md) for the full module layout and data flow.

---

## Adding a Room migration

Never modify an existing entity without a migration. When you add, rename, or remove a column or table:

1. Update the entity class.
2. Add a `Migration(old, new)` object to `AppDatabase.Companion`.
3. Increment `@Database(version = N)`.
4. Add the migration to the `addMigrations(...)` call in `DatabaseModule`.
5. Run `./gradlew :data:test` — Room will fail if the migration is missing or the schema doesn't match.

Schema files are exported to `data/schemas/` and tracked in git. Always commit the new schema JSON alongside your migration.

---

## Testing

### Domain tests

`domain/src/test/kotlin/` uses JUnit 5 and runs on the JVM with no Android setup needed:

```bash
./gradlew :domain:test
```

When adding or changing use cases or pure domain functions, add corresponding tests. See the existing test classes for style.

### DAO tests

`data/src/test/kotlin/` uses Robolectric + in-memory Room:

```bash
./gradlew :data:test
```

Use `@RunWith(RobolectricTestRunner::class)` and `Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)`. Tests are JUnit 4 (Robolectric doesn't yet support JUnit 5) and run via `junit-vintage-engine`.

### Manual testing

- Install a debug APK directly on a connected device: `./gradlew installFossDebug`
- Test the `play` and `foss` flavors separately for any change that touches the scanner or DI wiring.
- For notifications, trigger the `ExpiryCheckWorker` without waiting for the daily schedule:

  ```bash
  adb shell am broadcast \
    -a org.aetherassembly.beforeitsgone.ACTION_CHECK_EXPIRY \
    -p org.aetherassembly.beforeitsgone
  ```

- For cloud sync, create a Supabase project, run the SQL migration from [`docs/cloud-sync.md`](../docs/cloud-sync.md), and test **Sync now** in Settings.
- View logs from the running app:

  ```bash
  adb logcat | grep -i "beforeitsgone\|WorkManager\|RoomDB"
  ```

---

## Gotchas and non-obvious behaviour

Things that have caused bugs in the past and will trip you up if you don't know about them.

### Date storage

Expiry dates are stored as UTC ISO strings in Room (`expiresAt TEXT NOT NULL`). When displaying a date back to the user, always convert to local time first. When saving a date from a `DatePickerDialog` (which returns a UTC epoch millis), convert carefully:

```kotlin
// Correct — extract local date from epoch, format as ISO
val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
val iso = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))

// Wrong — toISOString()-style UTC slice, off by one for UTC- users
val iso = Instant.ofEpochMilli(epochMillis).toString().substring(0, 10)
```

### Smart cast across module boundary

If a value is declared `val` in another module, Kotlin cannot smart-cast it after a null check because another thread could theoretically change it. Copy to a local `val` first:

```kotlin
// Fails to compile — smart cast impossible across module boundary
if (repository.item != null) doSomething(repository.item.name)

// Correct
val item = repository.item
if (item != null) doSomething(item.name)
```

### `Modifier.clickable` on cards

In Compose 1.7+, `Modifier.clickable` is annotated `@Composable` and cannot be called inside a `remember` or non-composable lambda. When adding a click handler to a `Card` that needs to launch an `Intent`, use a `Box` overlay or pass the `onClick` directly to `Card`:

```kotlin
// Correct — onClick is a composable parameter
Card(onClick = { context.startActivity(intent) }) { ... }

// Wrong — Modifier.clickable inside a non-composable context
Box(modifier = Modifier.clickable { ... }) { ... }  // will fail in some contexts
```

### Hilt `@Binds` vs `@Provides`

Use `@Binds` when wiring an interface to its implementation (the function body is empty and generated by Hilt). Use `@Provides` when construction requires logic (e.g. building a Ktor client). Mixing them in the same `@Module` class requires the module to be `abstract`; if you add a `@Provides` function to an abstract module, make it `companion object` or move it to a separate concrete module.

### WorkManager one-time enqueue on app start

`BeforeItsGoneApp` uses `ExistingPeriodicWorkPolicy.KEEP` when scheduling `ExpiryCheckWorker`. This means re-installs and app upgrades do not reset the schedule. If you change the worker's constraints or period and need the new schedule to take effect, change the policy to `UPDATE` temporarily — but remember to revert it, otherwise every app start will reschedule.

### `savedStateHandle` barcode back-propagation

The barcode scanner writes its result to the *previous* back-stack entry's `savedStateHandle["scanned_barcode"]` before calling `popBackStack()`. `AddEditItemViewModel` reads this key in `init {}`. If you add a new caller of the scanner screen, make sure your ViewModel also reads and clears this key — leaving it set will cause the value to be re-consumed on the next ViewModel recreation.

---

## Issue labels

| Label | Meaning |
| ----- | ------- |
| `bug` | Something broken in an existing release |
| `feature` | New capability or user-visible behaviour |
| `enhancement` | Improvement to an existing feature |
| `docs` | Documentation only |
| `good first issue` | Small, well-scoped, low risk; good for first contributions |
| `area: domain` | Affects `:domain` models or use cases |
| `area: data` | Affects `:data` Room, DataStore, sync, or workers |
| `area: ui` | Affects `:app` Compose screens or navigation |
| `area: scanner` | Affects the barcode scanner or CameraX pipeline |
| `area: notifications` | Affects WorkManager or notification channels |
| `area: sync` | Affects Supabase sync or import/export |
| `flavor: play` | Play Store flavor only |
| `flavor: foss` | F-Droid / foss flavor only |

---

## Code style

- Kotlin everywhere; no Java source files.
- Follow the existing `@Composable` patterns: state hoisted to ViewModel, events as lambdas, no business logic in Compose functions.
- Hilt `@HiltViewModel` for all ViewModels; `@Singleton` for repositories; `@Binds` over `@Provides` where possible.
- No `LiveData`; use `StateFlow` + `collectAsStateWithLifecycle`.
- No comments that describe *what* the code does; only *why*, when the reason isn't obvious from the code itself.

---

## Credit

If you use Before It's Gone for Android (or any of its code) as a base for your own project, please include a credit to [AetherAssembly](https://aetherassembly.org/about).
