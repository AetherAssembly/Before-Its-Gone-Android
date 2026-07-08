package org.aetherassembly.beforeitsgone.domain.usecase

import org.aetherassembly.beforeitsgone.domain.model.InventoryItem
import org.aetherassembly.beforeitsgone.domain.model.ItemHistory
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Ported 1:1 from desktop's `recordItemHistory` (packages/core/src/inventory.ts).
 * Called once per NEW inventory item (not on edits), keyed by the item's
 * lowercased/trimmed name so re-adding "Milk" accumulates useCount instead of
 * creating a new row per item id.
 */
fun buildItemHistoryEntry(
    item: InventoryItem,
    existing: ItemHistory?,
    now: Instant = Instant.now()
): ItemHistory {
    val historyId = item.name.trim().lowercase()

    val createdAt = Instant.parse(item.createdAt)
    val expiresAt = Instant.parse(item.expiresAt)
    val shelfLifeDays = ChronoUnit.DAYS.between(createdAt, expiresAt).toInt()

    return ItemHistory(
        id = historyId,
        name = item.name,
        barcode = item.barcode,
        location = item.location,
        shelfLifeDays = maxOf(1, shelfLifeDays),
        category = item.category,
        lastUsedAt = now.toString(),
        useCount = (existing?.useCount ?: 0) + 1
    )
}
