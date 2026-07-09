package org.aetherassembly.beforeitsgone.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barcode_profiles")
data class BarcodeProfileEntity(
    @PrimaryKey val barcode: String,
    val productName: String,
    val defaultShelfLifeDays: Int?,
    val preferredLocation: String?,
    val updatedAt: String,
    val caloriesPer100g: Double?,
    val allergens: List<String>
)
