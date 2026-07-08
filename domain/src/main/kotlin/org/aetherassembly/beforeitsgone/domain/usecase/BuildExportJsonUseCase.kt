package org.aetherassembly.beforeitsgone.domain.usecase

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.aetherassembly.beforeitsgone.domain.model.InventoryItem
import java.time.Instant

@Serializable
private data class ExportItem(
    val id: String,
    val name: String,
    val quantity: Double,
    val location: String,
    val barcode: String? = null,
    val expiresAt: String,
    val shelfLifeDays: Int? = null,
    val createdAt: String,
    val updatedAt: String,
    val category: String? = null,
    val depletionThreshold: Double? = null,
    val tags: List<String> = emptyList(),
    val recurring: Boolean = false,
    val restockQuantity: Double? = null,
    val photo: String? = null   // base64-encoded image data (desktop-compatible)
)

@Serializable
private data class ExportEnvelope(
    val version: Int,
    val exportedAt: String,
    val items: List<ExportItem>
)

private val json = Json { prettyPrint = true }

fun buildExportJson(
    items: List<InventoryItem>,
    photoResolver: (path: String) -> String?  // returns base64 string or null
): String {
    val exportItems = items.map { item ->
        ExportItem(
            id = item.id,
            name = item.name,
            quantity = item.quantity,
            location = item.location,
            barcode = item.barcode,
            expiresAt = item.expiresAt,
            shelfLifeDays = item.shelfLifeDays,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt,
            category = item.category,
            depletionThreshold = item.depletionThreshold,
            tags = item.tags,
            recurring = item.recurring,
            restockQuantity = item.restockQuantity,
            photo = item.photoPath?.let { photoResolver(it) }
        )
    }
    return json.encodeToString(ExportEnvelope(version = 1, exportedAt = Instant.now().toString(), items = exportItems))
}
