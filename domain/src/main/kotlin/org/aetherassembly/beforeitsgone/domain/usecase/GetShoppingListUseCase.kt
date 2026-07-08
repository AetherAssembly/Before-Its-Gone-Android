package org.aetherassembly.beforeitsgone.domain.usecase

import org.aetherassembly.beforeitsgone.domain.model.InventoryItem

/**
 * Ported 1:1 from desktop's `getShoppingList` (packages/core/src/inventory.ts).
 * There is deliberately no persisted "shopping list" entity on either platform —
 * it's a live derivation over inventory: items whose quantity has dropped to or
 * below their own [InventoryItem.depletionThreshold].
 */
fun getShoppingList(items: List<InventoryItem>): List<InventoryItem> =
    items
        .filter { it.depletionThreshold != null && it.quantity <= it.depletionThreshold }
        .sortedBy { it.name }
