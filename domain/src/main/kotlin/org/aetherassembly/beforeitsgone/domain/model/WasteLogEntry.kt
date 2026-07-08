package org.aetherassembly.beforeitsgone.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WasteLogEntry(
    val id: String,
    val itemName: String,
    val quantity: Double,
    val location: String,
    val category: String? = null,
    val expiresAt: String,
    val wastedAt: String
)
