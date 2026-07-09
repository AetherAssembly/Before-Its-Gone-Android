# Import & Export Format Reference

The JSON and CSV formats are byte-compatible with the Before It's Gone desktop app. A JSON export from the desktop can be imported on Android and vice versa.

---

## JSON

### JSON-Exporting

The app exports a single JSON object with three top-level keys:

```json
{
  "version": 1,
  "exportedAt": "2026-05-18T14:30:00.000Z",
  "items": [ ... ]
}
```

| Key | Type | Description |
| --- | ---- | ----------- |
| `version` | `number` | Schema version. Currently always `1`. |
| `exportedAt` | `string` | ISO 8601 timestamp of when the export was created. |
| `items` | `array` | Array of inventory item objects (see below). |

#### Item object

| Field | Type | Notes |
| ----- | ---- | ----- |
| `id` | `string` | UUID. Preserved on re-import. |
| `name` | `string` | Display name of the item. |
| `quantity` | `number` | Positive number. Fractional values (e.g. 0.5) are supported. |
| `location` | `string` | One of `"fridge"`, `"freezer"`, `"pantry"`, or a custom location name. |
| `barcode` | `string \| null` | Raw barcode string, or `null` if none was scanned. |
| `expiresAt` | `string` | ISO 8601 timestamp (e.g. `"2026-06-01T23:59:59.000Z"`). |
| `shelfLifeDays` | `number` | Shelf life in days. |
| `createdAt` | `string` | ISO 8601 timestamp. |
| `updatedAt` | `string` | ISO 8601 timestamp. Used for sync conflict resolution. |
| `category` | `string \| null` | Free-text category label, or `null`. |
| `depletionThreshold` | `number \| null` | Low-stock alert threshold (quantity), or `null` if unset. |
| `tags` | `string[]` | Array of tag strings. Empty array if none. |
| `recurring` | `boolean` | Whether the item auto-restocks when depleted. Optional; defaults to `false`. |
| `restockQuantity` | `number \| undefined` | Quantity to restock to when `recurring` is true. Optional. |
| `photo` | `string \| undefined` | Base64-encoded image data. See **Photo field** section below. |

#### Full example

```json
{
  "version": 1,
  "exportedAt": "2026-05-18T14:30:00.000Z",
  "items": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "name": "Whole Milk",
      "quantity": 2,
      "location": "fridge",
      "barcode": "5000112637922",
      "expiresAt": "2026-05-25T23:59:59.000Z",
      "shelfLifeDays": 10,
      "createdAt": "2026-05-15T08:00:00.000Z",
      "updatedAt": "2026-05-15T08:00:00.000Z",
      "category": "dairy",
      "depletionThreshold": 1,
      "tags": ["organic", "fridge-door"]
    }
  ]
}
```

### Photo field on Android

On Android, photos are stored as files on-device. The `photo` field in the JSON is the base64-encoded JPEG content (without a data URI prefix).

**On export from Android:** the photo file is read from `<filesDir>/photos/<id>.jpg` and base64-encoded into the `photo` field. The desktop app can decode this directly.

**On import from a desktop export:** the desktop stores photos as data URIs (`data:image/jpeg;base64,...`). The Android importer strips the `data:...;base64,` prefix, decodes the base64 bytes, and writes the JPEG to `<filesDir>/photos/<id>.jpg`.

### JSON-Importing

The importer reads the `items` array from inside the `{ version, exportedAt, items }` envelope. Items missing an `id` or `name` are silently skipped. Re-importing a previously exported file overwrites existing items with matching `id` values in place rather than duplicating them.

> **Tip:** JSON import is best for full backups and restores. Use CSV when adding items from a spreadsheet.

---

## CSV

### CSV-Exporting

The exported CSV uses the following fixed column order:

```csv
name,quantity,location,barcode,expiresAt,category,tags,shelf_life_days,depletion_threshold,createdAt
```

- Values that contain commas are double-quoted.
- `barcode` and `category` are empty strings when not set.
- `expiresAt` is a date-only string (`YYYY-MM-DD`); `createdAt` is a full ISO 8601 timestamp.
- `tags` is a semicolon-separated list (e.g. `organic;bulk`); empty string if none.
- `shelf_life_days` and `depletion_threshold` are empty strings when not set.

> **Note:** CSV export omits `id`, `recurring`, `restockQuantity`, and per-item photo data. Use JSON export for a complete, lossless backup.

### CSV-Importing

The CSV importer accepts any column order. Column headers are **case-insensitive**.

#### Column reference

| Column name | Required | Type | Notes |
| ----------- | -------- | ---- | ----- |
| `name` | **Yes** | `string` | Rows with an empty name are skipped. |
| `expires_at` or `expiresat` | **Yes** | `string` | See date formats below. |
| `location` | **Yes** | `string` | `fridge`, `freezer`, `pantry`, or a custom location. |
| `quantity` | No | `number` | Defaults to `1` if missing or non-numeric. |
| `barcode` | No | `string` | Leave empty if none. |
| `category` | No | `string` | |
| `shelf_life_days` | No | `number` | |
| `tags` | No | `string` | Semicolon-separated (e.g. `organic;bulk`). |
| `depletion_threshold` | No | `number` | |

#### Date formats

| Format | Example | Notes |
| ------ | ------- | ----- |
| `YYYY-MM-DD` | `2026-06-01` | Recommended. Stored as `2026-06-01T23:59:59.000Z`. |
| Full ISO timestamp | `2026-06-01T23:59:59.000Z` | Accepted as-is. |

---

## Format comparison

| | JSON export | CSV export | JSON import | CSV import |
| - | :-----------: | :----------: | :-----------: | :----------: |
| `id` | ✅ | ❌ | ✅ preserved | ❌ new UUIDs assigned |
| `name` | ✅ | ✅ | ✅ | ✅ |
| `quantity` | ✅ | ✅ | ✅ | ✅ |
| `location` | ✅ | ✅ | ✅ | ✅ |
| `barcode` | ✅ | ✅ | ✅ | ✅ |
| `expiresAt` | ✅ full ISO | ✅ date only | ✅ | ✅ |
| `shelfLifeDays` | ✅ | ✅ | ✅ | ✅ |
| `category` | ✅ | ✅ | ✅ | ✅ |
| `tags` | ✅ | ✅ semicolons | ✅ | ✅ semicolons |
| `depletionThreshold` | ✅ | ✅ | ✅ | ✅ |
| `recurring` / `restockQuantity` | ✅ | ❌ | ✅ | ❌ |
| `photo` | ✅ base64 | ❌ | ✅ | ❌ |
| `createdAt` / `updatedAt` | ✅ | `createdAt` only | ✅ preserved | ❌ set to import time |

**For a lossless round-trip, always use JSON.**

---

## Barcode import (.txt)

> **Not available in v1.0.** Plain barcode list import (one barcode per line) is planned for a future release. Individual barcode scanning via the device camera covers the equivalent use case for v1.

When this feature ships, it will accept a plain text file with one barcode per line and behave identically to the desktop's barcode import: Open Food Facts lookup, default shelf life and location from Settings, nutritional profile saved to the barcode profile store.
