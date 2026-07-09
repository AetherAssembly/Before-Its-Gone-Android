package org.aetherassembly.beforeitsgone.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_history")
data class ItemHistoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val barcode: String?,
    val location: String,
    val shelfLifeDays: Int,
    val category: String?,
    val lastUsedAt: String,
    val useCount: Int
)
