# External API Setup

Before It's Gone for Android integrates with two external services. Neither requires an account or an API key.

---

## Overview

| Service | Purpose | Account required | API key required |
| ------- | ------- | --------------- | --------------- |
| [TheMealDB](#themealdb) | Recipe suggestions based on expiring items | No | No |
| [Supabase](#supabase) | Optional cloud sync | Yes (free tier available) | No — anon key only |

---

## TheMealDB

**What it does:** Suggests recipes when items in your inventory are expiring soon. For each expiring item, the app queries TheMealDB for meals that use that ingredient as a main component. Results are deduplicated and sorted by how many of your expiring items each meal uses.

**Account required:** No  
**API key required:** No (the free public API uses the path prefix `/api/json/v1/1/`, which the app handles internally)  
**Cost:** Free

**Endpoint used:**

```
GET https://www.themealdb.com/api/json/v1/1/filter.php?i={ingredient}
```

**Data sent:** The name of each expiring item (up to 5 items per refresh). No personal data, no inventory quantities, no dates.

**Trigger:** The Recipes screen loads suggestions automatically when opened and can be refreshed manually via the refresh button. Only items currently in an expiring-soon state are used as query ingredients.

**No configuration needed.** The integration works out of the box.

---

## Supabase

**What it does:** Provides optional cloud sync so your inventory can be shared across devices or backed up to your own Supabase project.

**Account required:** Yes (at [supabase.com](https://supabase.com))  
**API key required:** No — the app uses the project's public **anon key**, which is safe to store on-device  
**Cost:** Free tier includes 500 MB database storage

### Setup

See [docs/cloud-sync.md](cloud-sync.md) for the complete walkthrough including the required SQL migration.

Quick reference:

1. Create a project at [supabase.com](https://supabase.com).
2. Run the SQL migration from the cloud sync guide.
3. Go to **Project Settings → API** and copy the **Project URL** and **anon / public** key.
4. Paste both into **Settings → Cloud sync** in the app.
5. Tap **Sync now**.

**No account sign-in is required in the app.** The anon key alone is sufficient. Access is controlled by the Row Level Security policy on your Supabase table.

---

## Troubleshooting

### Recipe suggestions don't appear

- Open the Recipes screen — it loads on demand, not automatically in the background.
- At least one item must be in expiring-soon or expired status. Check the inventory list for items with orange or red badges.
- TheMealDB returns results only when the ingredient name matches its database. Generic or unusual item names (e.g. "leftovers", "misc") may return no results.
- Check your network connection; TheMealDB requires an internet connection.

### Supabase sync fails

- Ensure the SQL migration has been run; the `inventory_items` table must exist in your Supabase project.
- Verify the RLS policy was created. Check **Authentication → Policies** in your Supabase dashboard.
- Confirm the URL and anon key are correct — the URL should end in `.supabase.co` with no trailing slash, and the key should start with `eyJ`.
- Free-tier Supabase projects pause after 1 week of inactivity. Resume the project from the Supabase dashboard if paused.
