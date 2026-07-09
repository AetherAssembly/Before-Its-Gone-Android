package org.aetherassembly.beforeitsgone.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waste_log")
data class WasteLogEntity(
    @PrimaryKey val id: String,
    val itemName: String,
    val quantity: Double,
    val location: String,
    val category: String?,
    val expiresAt: String,
    val wastedAt: String
)
