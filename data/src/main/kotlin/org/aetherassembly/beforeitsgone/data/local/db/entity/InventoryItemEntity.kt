package org.aetherassembly.beforeitsgone.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_items",
    indices = [Index("expiresAt")]
)
data class InventoryItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: Double,
    val location: String,
    val barcode: String?,
    val expiresAt: String,
    val shelfLifeDays: Int?,
    val createdAt: String,
    val updatedAt: String,
    val category: String?,
    val depletionThreshold: Double?,
    val tags: List<String>,     // stored as JSON via StringListConverter
    val recurring: Boolean,
    val restockQuantity: Double?,
    val photoPath: String?
)
