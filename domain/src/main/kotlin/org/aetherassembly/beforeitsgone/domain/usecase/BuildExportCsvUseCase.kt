package org.aetherassembly.beforeitsgone.domain.usecase

import org.aetherassembly.beforeitsgone.domain.model.InventoryItem

fun buildExportCsv(items: List<InventoryItem>): String {
    val header = "id,name,quantity,location,barcode,expiresAt,shelfLifeDays," +
        "createdAt,updatedAt,category,depletionThreshold,tags,recurring,restockQuantity"
    val rows = items.map { item ->
        listOf(
            item.id.csvCell(),
            item.name.csvCell(),
            item.quantity.toString(),
            item.location.csvCell(),
            (item.barcode ?: "").csvCell(),
            item.expiresAt.csvCell(),
            item.shelfLifeDays?.toString() ?: "",
            item.createdAt.csvCell(),
            item.updatedAt.csvCell(),
            (item.category ?: "").csvCell(),
            item.depletionThreshold?.toString() ?: "",
            item.tags.joinToString(";").csvCell(),
            item.recurring.toString(),
            item.restockQuantity?.toString() ?: ""
        ).joinToString(",")
    }
    return (listOf(header) + rows).joinToString("\n")
}

private fun String.csvCell(): String =
    if (contains(',') || contains('"') || contains('\n'))
        "\"${replace("\"", "\"\"")}\""
    else this
