package org.aetherassembly.beforeitsgone.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.aetherassembly.beforeitsgone.domain.model.InventoryItem
import org.aetherassembly.beforeitsgone.domain.usecase.buildExportJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class BuildExportJsonUseCaseTest {

    private val item = InventoryItem(
        id = "id1",
        name = "Butter",
        quantity = 1.0,
        location = "fridge",
        expiresAt = "2026-08-01T23:59:59.000Z",
        createdAt = "2026-07-01T00:00:00Z",
        updatedAt = "2026-07-01T00:00:00Z"
    )

    private fun parseOutput(json: String): JsonObject = Json.parseToJsonElement(json).jsonObject

    @Test
    fun `output has version field equal to 1`() {
        val output = parseOutput(buildExportJson(listOf(item)) { null })
        assertEquals(1, output["version"]?.jsonPrimitive?.content?.toInt())
    }

    @Test
    fun `output has exportedAt field`() {
        val output = parseOutput(buildExportJson(listOf(item)) { null })
        assertNotNull(output["exportedAt"])
    }

    @Test
    fun `item fields match import schema`() {
        val output = parseOutput(buildExportJson(listOf(item)) { null })
        val itemObj = output["items"]!!.jsonArray[0].jsonObject
        assertEquals("id1", itemObj["id"]?.jsonPrimitive?.content)
        assertEquals("Butter", itemObj["name"]?.jsonPrimitive?.content)
        assertEquals("fridge", itemObj["location"]?.jsonPrimitive?.content)
        assertEquals("2026-08-01T23:59:59.000Z", itemObj["expiresAt"]?.jsonPrimitive?.content)
    }

    @Test
    fun `photo is null when photoPath is null`() {
        val output = parseOutput(buildExportJson(listOf(item)) { null })
        val itemObj = output["items"]!!.jsonArray[0].jsonObject
        // photo field should be absent or null when no path
        val photo = itemObj["photo"]
        if (photo != null) {
            assert(photo.jsonPrimitive.isString.not() || photo.jsonPrimitive.content.isEmpty())
        }
    }

    @Test
    fun `photo is base64 when photoPath resolves`() {
        val itemWithPhoto = item.copy(photoPath = "/some/path.jpg")
        val expected = "aGVsbG8="  // base64("hello")
        val output = parseOutput(buildExportJson(listOf(itemWithPhoto)) { expected })
        val itemObj = output["items"]!!.jsonArray[0].jsonObject
        assertEquals(expected, itemObj["photo"]?.jsonPrimitive?.content)
    }

    @Test
    fun `empty list produces empty items array`() {
        val output = parseOutput(buildExportJson(emptyList()) { null })
        assertEquals(0, output["items"]!!.jsonArray.size)
    }
}
