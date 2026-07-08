package org.aetherassembly.beforeitsgone.domain.usecase

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.aetherassembly.beforeitsgone.domain.model.InventoryItem

@Serializable
private data class InventoryExportEnvelope(
    val version: Int = 1,
    val exportedAt: String = "",
    val items: List<InventoryItem> = emptyList()
)

private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

fun parseInventoryJson(input: String): List<InventoryItem> {
    val envelope = json.decodeFromString<InventoryExportEnvelope>(input)
    return envelope.items
}
