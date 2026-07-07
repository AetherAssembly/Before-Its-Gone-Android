# Privacy Policy

Last updated: 2026-07-07

## Overview

Before It's Gone is a local-first Android application for tracking food items, barcodes, and expiry dates. This application is designed to keep your data on your own device.

## Who We Are

Before It's Gone is developed and maintained by [AetherAssembly](https://aetherassembly.org/about). For privacy-related questions, contact us at [support@aetherassembly.org](mailto:support@aetherassembly.org) or via the [contact form](https://forms.gle/T4i7GGzaT3HUrffm9).

## Data We Process

The app may store the following information locally on your device:

- Inventory item names
- Barcode values you enter
- Quantity, location, shelf-life, and expiry information
- Saved barcode profiles used for auto-fill behavior
- Local notification state used to avoid duplicate alerts
- Optional item photos, stored as files in the app's private storage on your device

This data is stored locally in an Android Room database (SQLite) and Jetpack DataStore on your device. Photos are never transmitted off your device.

## Data We Do Not Intentionally Collect

This project does not include:

- Mandatory user accounts
- Built-in analytics or tracking
- Advertising identifiers
- Remote telemetry sent by the application itself

## Network Requests

The application makes outbound network requests only in the following circumstances:

### Always off by default / user-triggered

| Request | Destination | When |
| ------- | ----------- | ---- |
| Barcode product lookup | [Open Food Facts](https://world.openfoodfacts.org/) | When you scan or look up a barcode |
| Batch barcode enrichment | Open Food Facts | When you import a barcode list file |

### Automatic when 3+ items are expiring (dismissible)

| Request | Destination | Data sent |
| ------- | ----------- | --------- |
| Recipe suggestions | [TheMealDB](https://www.themealdb.com/) | The name of one expiring item as an ingredient query |

### Opt-in only

| Feature | Destination | Data sent |
| ------- | ----------- | --------- |
| Cloud sync | Your Supabase project | Your full inventory (all fields) |

All third-party services may receive standard HTTP request metadata (IP address, user agent, request timestamp) according to their own privacy policies.

## Notifications

The app may request permission to display Android system notifications (via the `POST_NOTIFICATIONS` permission) for items that are expiring soon, already expired, or running low on stock. Notification content is generated entirely on-device; no item data is sent to any notification service.

## Cloud Sync

If you enable optional Supabase cloud sync, your inventory data is transmitted to and stored in the Supabase project you configure. AetherAssembly does not operate or have access to your Supabase project. Your Supabase URL and anon key are stored in your device's DataStore. Sync is disabled by default and requires explicit opt-in.

## Android Data Safety

This section corresponds to the Data Safety section shown on the Google Play Store listing.

**Data collected:** None. Before It's Gone does not collect any data.

**Data shared:** None. The app does not share data with third parties.

**Optional user-initiated network requests** (not collection):

| Destination | Data sent | When |
| ----------- | --------- | ---- |
| Open Food Facts (world.openfoodfacts.org) | Barcode string | When user scans or looks up a barcode |
| TheMealDB (themealdb.com) | One item name as ingredient query | When 3+ items are expiring (dismissible) |
| Your Supabase project | Full inventory | When user enables and triggers cloud sync |

Camera access is used exclusively for barcode scanning and item photo capture. Barcode frames are analyzed on-device by ML Kit or ZXing and are never stored or transmitted. Photos are stored only in the app's private storage on your device.

## Data Sharing

The project does not intentionally sell, rent, or share your local inventory data with third parties.

## Data Retention and Deletion

Your data remains on your device until you remove it in the application or uninstall the app. Uninstalling the app removes all data stored by the app on most Android versions. You can also clear all data from Android's App Info screen at any time.

## Open Source Nature of the Project

Because this project is open source, anyone can inspect the code to verify how data is handled. If you build modified versions yourself or install builds from third parties, their behavior may differ from official AetherAssembly releases.

## Contact

For privacy-related questions, contact AetherAssembly at [support@aetherassembly.org](mailto:support@aetherassembly.org) or via the [contact form](https://forms.gle/T4i7GGzaT3HUrffm9).
