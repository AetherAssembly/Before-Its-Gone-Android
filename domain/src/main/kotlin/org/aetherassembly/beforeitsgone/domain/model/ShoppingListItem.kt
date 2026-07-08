package org.aetherassembly.beforeitsgone.domain.model

data class ShoppingListItem(
    val id: String,
    val name: String,
    val quantity: Double,
    val checked: Boolean,
    val addedAt: String,
    val sourceItemId: String? = null
)
