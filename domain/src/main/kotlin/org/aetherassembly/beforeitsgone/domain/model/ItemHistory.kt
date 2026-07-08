package org.aetherassembly.beforeitsgone.domain.model

import kotlinx.serialization.Serializable

/**
 * Tracks, per distinct item name, how the user has previously stocked that item.
 * Mirrors desktop's `ItemHistory` (packages/core/src/models.ts) exactly. [id] is
 * the lowercased/trimmed item name, not a UUID — one row per distinct name.
 */
@Serializable
data class ItemHistory(
    val id: String,
    val name: String,
    val barcode: String? = null,
    val location: String,
    val shelfLifeDays: Int,
    val category: String? = null,
    val lastUsedAt: String,
    val useCount: Int
)
