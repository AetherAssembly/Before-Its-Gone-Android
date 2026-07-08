package org.aetherassembly.beforeitsgone.domain.usecase

import org.aetherassembly.beforeitsgone.domain.model.InventoryItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

fun parseInventoryCsv(input: String): List<InventoryItem> {
    val lines = input.lines().filter { it.isNotBlank() }
    if (lines.size < 2) return emptyList()

    val headers = lines[0].split(",").map { it.trim().lowercase() }
    val now = Instant.now().toString()

    return lines.drop(1).mapNotNull { line ->
        runCatching {
            val values = splitCsvLine(line)
            fun col(name: String) = headers.indexOf(name).takeIf { it >= 0 }?.let { values.getOrNull(it)?.trim() }

            val expiresAt = col("expiresat") ?: col("expires_at") ?: col("expiry") ?: return@mapNotNull null
            InventoryItem(
                id = col("id") ?: UUID.randomUUID().toString(),
                name = col("name") ?: return@mapNotNull null,
                quantity = col("quantity")?.toDoubleOrNull() ?: 1.0,
                location = col("location") ?: "pantry",
                barcode = col("barcode")?.takeIf { it.isNotEmpty() },
                expiresAt = normalizeDate(expiresAt) ?: return@mapNotNull null,
                shelfLifeDays = col("shelflifedays")?.toIntOrNull(),
                createdAt = col("createdat") ?: now,
                updatedAt = col("updatedat") ?: now,
                category = col("category")?.takeIf { it.isNotEmpty() },
                depletionThreshold = col("depletionthreshold")?.toDoubleOrNull(),
                tags = col("tags")?.split(";")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
                recurring = col("recurring")?.lowercase() == "true",
                restockQuantity = col("restockquantity")?.toDoubleOrNull(),
            )
        }.getOrNull()
    }
}

private fun normalizeDate(raw: String): String? {
    val cleaned = raw.trim().removeSurrounding("\"")
    // Already ISO 8601
    if (cleaned.contains("T")) return runCatching { Instant.parse(cleaned).toString() }.getOrNull()
        ?: runCatching { Instant.parse("${cleaned}Z").toString() }.getOrNull()
    // Date-only: YYYY-MM-DD
    return runCatching {
        LocalDate.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE)
            .atTime(23, 59, 59)
            .toInstant(ZoneOffset.UTC)
            .toString()
    }.getOrNull()
}

private fun splitCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    for (ch in line) {
        when {
            ch == '"' -> inQuotes = !inQuotes
            ch == ',' && !inQuotes -> { result.add(current.toString()); current.clear() }
            else -> current.append(ch)
        }
    }
    result.add(current.toString())
    return result
}
