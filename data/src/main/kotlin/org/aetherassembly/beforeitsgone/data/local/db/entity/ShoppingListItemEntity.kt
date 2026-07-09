package org.aetherassembly.beforeitsgone.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_list")
data class ShoppingListItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: Double,
    val checked: Boolean,
    val addedAt: String,
    val sourceItemId: String?
)
