# Data Model

Field names are identical to the desktop app's `InventoryItem` TypeScript interface to maintain import/export compatibility. A JSON export from the desktop can be imported on Android and vice versa.

---

## InventoryItem

| Field | Kotlin Type | Room Column | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `String` | `TEXT PRIMARY KEY` | Yes | UUID string |
| `name` | `String` | `TEXT NOT NULL` | Yes | |
| `quantity` | `Double` | `REAL NOT NULL` | Yes | Fractional values supported (e.g. 0.5 kg) |
| `location` | `String` | `TEXT NOT NULL` | Yes | `"fridge"` / `"freezer"` / `"pantry"` / custom |
| `barcode` | `String?` | `TEXT` | No | |
| `expiresAt` | `String` | `TEXT NOT NULL` | Yes | ISO 8601 (`"2026-12-01T23:59:59.000Z"`). Indexed. |
| `shelfLifeDays` | `Int?` | `INTEGER` | No | |
| `createdAt` | `String` | `TEXT NOT NULL` | Yes | ISO 8601 |
| `updatedAt` | `String` | `TEXT NOT NULL` | Yes | ISO 8601. Used for sync conflict resolution. |
| `category` | `String?` | `TEXT` | No | |
| `depletionThreshold` | `Double?` | `REAL` | No | Low-stock trigger quantity |
| `tags` | `List<String>` | `TEXT` | No | Stored as JSON array string via `StringListConverter` |
| `recurring` | `Boolean` | `INTEGER` | No | Auto-restock on depletion; default `false` |
| `restockQuantity` | `Double?` | `REAL` | No | |
| `photoPath` | `String?` | `TEXT` | No | Local file path (see Photo field section below) |

---

## Photo field

The desktop stores the `photo` field as a data URI directly in IndexedDB (e.g. `data:image/jpeg;base64,...`).

On Android, photos are stored as JPEG files in the app's private storage at `<filesDir>/photos/<itemId>.jpg`. The Room column `photoPath` holds the absolute file path. Photos are never transmitted off the device.

**On JSON export:** the photo is loaded from disk, base64-encoded, and written into the `photo` field of the JSON envelope — making the export byte-compatible with the desktop format.

**On JSON import:** if the imported item contains a `photo` field, the Android importer:
1. Strips any `data:image/...;base64,` prefix (present in desktop exports)
2. Decodes the base64 bytes
3. Writes the file to `<filesDir>/photos/<itemId>.jpg`
4. Stores the path in `photoPath`

---

## WasteLogEntry

| Field | Kotlin Type | Room Column | Notes |
| --- | --- | --- | --- |
| `id` | `String` | `TEXT PRIMARY KEY` | UUID |
| `itemName` | `String` | `TEXT NOT NULL` | |
| `quantity` | `Double` | `REAL NOT NULL` | |
| `location` | `String` | `TEXT NOT NULL` | |
| `category` | `String?` | `TEXT` | |
| `expiresAt` | `String` | `TEXT NOT NULL` | ISO 8601 |
| `wastedAt` | `String` | `TEXT NOT NULL` | ISO 8601 |

---

## BarcodeProfile

| Field | Kotlin Type | Room Column | Notes |
| --- | --- | --- | --- |
| `barcode` | `String` | `TEXT PRIMARY KEY` | |
| `productName` | `String` | `TEXT NOT NULL` | |
| `defaultShelfLifeDays` | `Int?` | `INTEGER` | |
| `preferredLocation` | `String?` | `TEXT` | |
| `updatedAt` | `String` | `TEXT NOT NULL` | ISO 8601 |
| `caloriesPer100g` | `Double?` | `REAL` | |
| `allergens` | `List<String>` | `TEXT` | JSON array string |

---

## ItemHistory

Used to pre-fill the Add/Edit form when a known item name is re-entered. `useCount` increments on each add.

| Field | Kotlin Type | Room Column | Notes |
| --- | --- | --- | --- |
| `id` | `String` | `TEXT PRIMARY KEY` | UUID |
| `name` | `String` | `TEXT NOT NULL` | Matched against new item names |
| `barcode` | `String?` | `TEXT` | |
| `location` | `String` | `TEXT NOT NULL` | Last used location |
| `shelfLifeDays` | `Int` | `INTEGER NOT NULL` | Last used shelf life |
| `category` | `String?` | `TEXT` | |
| `lastUsedAt` | `String` | `TEXT NOT NULL` | ISO 8601 |
| `useCount` | `Int` | `INTEGER NOT NULL` | |

---

## ShoppingListItem

Persisted shopping list entries. `sourceItemId` distinguishes auto-derived items from manually added ones.

| Field | Kotlin Type | Room Column | Notes |
| --- | --- | --- | --- |
| `id` | `String` | `TEXT PRIMARY KEY` | UUID |
| `name` | `String` | `TEXT NOT NULL` | |
| `quantity` | `Double` | `REAL NOT NULL` | |
| `checked` | `Boolean` | `INTEGER NOT NULL` | |
| `addedAt` | `String` | `TEXT NOT NULL` | ISO 8601 |
| `sourceItemId` | `String?` | `TEXT` | `null` = manual; non-null = derived from inventory item |

---

## AppSettings (DataStore)

Stored in `DataStore<Preferences>` rather than Room. Keys are string-keyed preference entries.

| Setting | Type | Default | Notes |
| --- | --- | --- | --- |
| `default_location` | `String` | `"fridge"` | |
| `shelf_life_days` | `Int` | `7` | |
| `expiry_warning_days` | `Int` | `2` | Days before expiry to show warning |
| `custom_locations_json` | `String` | `"[]"` | JSON array of custom location names |
| `notifications_expiring` | `Boolean` | `true` | |
| `notifications_expired` | `Boolean` | `true` | |
| `notifications_low_stock` | `Boolean` | `true` | |
| `sync_enabled` | `Boolean` | `false` | |
| `sync_supabase_url` | `String` | `""` | |
| `sync_supabase_anon_key` | `String` | `""` | |
| `sync_last_synced_at` | `String` | (absent) | ISO 8601 |

---

## AndroidSettings (DataStore)

Android-only preferences stored in a separate `DataStore<Preferences>` instance from `AppSettings`.

| Setting | Type | Default | Notes |
| --- | --- | --- | --- |
| `theme_override` | `Int` | `0` | `0` = System, `1` = Light, `2` = Dark |
| `dynamic_color` | `Boolean` | `true` | Material You dynamic color; ignored below API 31 |
| `notification_hour` | `Int` | `8` | Hour of day for the daily expiry check (0–23) |
| `notification_minute` | `Int` | `0` | Minute of the daily expiry check |
