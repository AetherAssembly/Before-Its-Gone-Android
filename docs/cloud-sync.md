# Cloud Sync

Before It's Gone for Android supports optional sync to a **Supabase** project you own. The app is fully offline-first; sync is opt-in and disabled by default.

The Android app uses the same Supabase project and table schema as the Before It's Gone desktop app, so a single Supabase project can back both.

---

## How it works

- Your inventory is stored as rows in an `inventory_items` table in your Supabase project.
- When you tap **Sync now** in Settings, the app pushes all local items to Supabase, then pulls any remote items that are newer than the local copy.
- **Conflict resolution is last-write-wins** by the `updatedAt` timestamp on each item. The most-recently-modified version of any item always wins.
- Deleted items are not propagated; deletions are local-only. Items deleted on one device will reappear after the next sync from another device.

---

## Setup

### 1. Create a Supabase project

1. Go to [supabase.com](https://supabase.com) and sign up or sign in.
2. Create a new project. Choose a region close to you.
3. Wait for the project to finish provisioning (~2 minutes).

### 2. Run the SQL migration

In your Supabase dashboard, open the **SQL Editor** and run the following once:

```sql
create table if not exists inventory_items (
  id                  text    not null primary key,
  name                text    not null,
  quantity            real    not null,
  location            text    not null,
  barcode             text,
  expires_at          text    not null,
  shelf_life_days     integer,
  created_at          text    not null,
  updated_at          text    not null,
  category            text,
  depletion_threshold real,
  tags                jsonb   not null default '[]',
  recurring           boolean not null default false,
  restock_quantity    real,
  photo               text
);

alter table inventory_items enable row level security;

-- Allow anonymous (unauthenticated) access for single-user personal projects.
-- Replace with a per-user policy if you share the Supabase project across accounts.
create policy "Anon full access"
  on inventory_items for all
  using (true)
  with check (true);
```

> **Using the same project as the desktop app?** The desktop app uses this exact schema. If you already have the table from a desktop setup, just skip this step and go straight to entering credentials.

### 3. Get your project credentials

In your Supabase dashboard:

1. Go to **Project Settings → API**.
2. Copy the **Project URL** (e.g. `https://xxxx.supabase.co`).
3. Copy the **`anon` / public key** (starts with `eyJ`).

### 4. Enter credentials in the app

Open **Settings → Cloud sync** and paste in:

- **Supabase URL**
- **Supabase anon key**

Then tap **Sync now**.

---

## Syncing between devices

Install the app on a second device, enter the same Supabase URL and anon key, and tap **Sync now**. Both devices will converge to the most-recently-modified version of each item.

To also sync with the **desktop app**, use the same Supabase project. The data formats are byte-compatible.

---

## Credential storage

Your Supabase URL and anon key are stored in `DataStore<Preferences>` in the app's private data directory on-device. They are never transmitted anywhere other than your own Supabase project.

---

## Disabling sync

Toggle **Enable sync** off in **Settings → Cloud sync**. The app stops syncing immediately. Your local inventory is unaffected. Data already in your Supabase project is not deleted; use the Supabase dashboard to remove rows if desired.

---

## Limitations

| | |
| - | - |
| Deletions | Local-only. Items deleted on one device reappear after syncing from another. |
| Waste log | Not synced. Waste log entries are local-only. |
| Barcode profiles | Not synced. Barcode profiles are local-only. |
| Shopping list | Not synced. Shopping list items are local-only. |
| Per-user isolation | The default RLS policy above allows anonymous access (all rows visible to anyone with the anon key). For a shared project with multiple users, replace it with a user_id-based policy matching the desktop app's schema. |
