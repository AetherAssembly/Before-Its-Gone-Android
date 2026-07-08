package org.aetherassembly.beforeitsgone.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BarcodeProfile(
    val barcode: String,
    val productName: String,
    val defaultShelfLifeDays: Int? = null,
    val preferredLocation: String? = null,
    val updatedAt: String,
    val caloriesPer100g: Double? = null,
    val allergens: List<String> = emptyList()
)
