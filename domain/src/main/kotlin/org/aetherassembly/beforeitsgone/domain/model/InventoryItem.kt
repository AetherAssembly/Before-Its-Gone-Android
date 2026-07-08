package org.aetherassembly.beforeitsgone.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class InventoryItem(
    val id: String,
    val name: String,
    val quantity: Double,
    val location: String,          // "fridge" | "freezer" | "pantry" | custom
    val barcode: String? = null,
    val expiresAt: String,         // ISO 8601: "2026-12-01T23:59:59.000Z"
    val shelfLifeDays: Int? = null,
    val createdAt: String,
    val updatedAt: String,
    val category: String? = null,
    val depletionThreshold: Double? = null,
    val tags: List<String> = emptyList(),
    val recurring: Boolean = false,
    val restockQuantity: Double? = null,
    val photoPath: String? = null  // local file path; base64-encoded on JSON export
)
