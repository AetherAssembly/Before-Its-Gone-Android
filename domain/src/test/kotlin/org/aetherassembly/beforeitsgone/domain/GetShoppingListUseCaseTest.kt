package org.aetherassembly.beforeitsgone.domain

import org.aetherassembly.beforeitsgone.domain.model.InventoryItem
import org.aetherassembly.beforeitsgone.domain.usecase.getShoppingList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetShoppingListUseCaseTest {

    private fun item(
        id: String,
        name: String,
        quantity: Double,
        threshold: Double?
    ) = InventoryItem(
        id = id,
        name = name,
        quantity = quantity,
        location = "pantry",
        expiresAt = "2026-12-01T23:59:59.000Z",
        createdAt = "2026-07-01T00:00:00Z",
        updatedAt = "2026-07-01T00:00:00Z",
        depletionThreshold = threshold
    )

    @Test
    fun `item below threshold appears in shopping list`() {
        val items = listOf(item("a", "Oats", 0.5, 1.0))
        val result = getShoppingList(items)
        assertEquals(1, result.size)
        assertEquals("Oats", result.single().name)
    }

    @Test
    fun `item at threshold appears in shopping list`() {
        val items = listOf(item("b", "Rice", 2.0, 2.0))
        val result = getShoppingList(items)
        assertEquals(1, result.size)
    }

    @Test
    fun `item above threshold does not appear`() {
        val items = listOf(item("c", "Sugar", 3.0, 2.0))
        assertTrue(getShoppingList(items).isEmpty())
    }

    @Test
    fun `item with no threshold is excluded`() {
        val items = listOf(item("d", "Flour", 0.0, null))
        assertTrue(getShoppingList(items).isEmpty())
    }

    @Test
    fun `result is sorted by name`() {
        val items = listOf(
            item("e", "Zucchini", 0.0, 1.0),
            item("f", "Apple", 0.0, 1.0),
            item("g", "Milk", 0.0, 1.0)
        )
        val result = getShoppingList(items)
        assertEquals(listOf("Apple", "Milk", "Zucchini"), result.map { it.name })
    }
}
