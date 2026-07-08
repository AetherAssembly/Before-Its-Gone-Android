package org.aetherassembly.beforeitsgone.domain

import org.aetherassembly.beforeitsgone.domain.usecase.buildExportJson
import org.aetherassembly.beforeitsgone.domain.usecase.parseInventoryJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParseInventoryJsonUseCaseTest {

    private val sampleJson = """
        {
          "version": 1,
          "exportedAt": "2026-07-01T00:00:00Z",
          "items": [
            {
              "id": "abc",
              "name": "Milk",
              "quantity": 1.5,
              "location": "fridge",
              "expiresAt": "2026-07-10T23:59:59.000Z",
              "createdAt": "2026-07-01T00:00:00Z",
              "updatedAt": "2026-07-01T00:00:00Z"
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `valid envelope parses to items`() {
        val items = parseInventoryJson(sampleJson)
        assertEquals(1, items.size)
        assertEquals("Milk", items[0].name)
        assertEquals(1.5, items[0].quantity)
    }

    @Test
    fun `extra unknown fields are ignored`() {
        val json = sampleJson.replace("\"quantity\": 1.5,", "\"quantity\": 1.5, \"unknownField\": true,")
        val items = parseInventoryJson(json)
        assertEquals(1, items.size)
    }

    @Test
    fun `round-trip through export and parse preserves name and id`() {
        val original = parseInventoryJson(sampleJson)
        val exported = buildExportJson(original) { null }
        val reimported = parseInventoryJson(exported)
        assertEquals(original[0].id, reimported[0].id)
        assertEquals(original[0].name, reimported[0].name)
    }

    @Test
    fun `empty items list returns empty`() {
        val json = """{"version":1,"exportedAt":"2026-07-01T00:00:00Z","items":[]}"""
        assertTrue(parseInventoryJson(json).isEmpty())
    }
}
