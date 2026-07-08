package org.aetherassembly.beforeitsgone.domain

import org.aetherassembly.beforeitsgone.domain.model.InventoryItem
import org.aetherassembly.beforeitsgone.domain.usecase.resolveSyncConflicts
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResolveSyncConflictsUseCaseTest {

    private fun item(id: String, updatedAt: String) = InventoryItem(
        id = id,
        name = "Item $id",
        quantity = 1.0,
        location = "fridge",
        expiresAt = "2026-12-01T23:59:59.000Z",
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = updatedAt
    )

    @Test
    fun `local newer than remote wins`() {
        val local = listOf(item("a", "2026-07-02T00:00:00Z"))
        val remote = listOf(item("a", "2026-07-01T00:00:00Z").copy(name = "Remote"))
        val result = resolveSyncConflicts(local, remote)
        assertEquals("Item a", result.single().name)
    }

    @Test
    fun `remote newer than local wins`() {
        val local = listOf(item("b", "2026-07-01T00:00:00Z"))
        val remote = listOf(item("b", "2026-07-02T00:00:00Z").copy(name = "Remote"))
        val result = resolveSyncConflicts(local, remote)
        assertEquals("Remote", result.single().name)
    }

    @Test
    fun `equal updatedAt keeps local`() {
        val local = listOf(item("c", "2026-07-01T00:00:00Z").copy(name = "Local"))
        val remote = listOf(item("c", "2026-07-01T00:00:00Z").copy(name = "Remote"))
        val result = resolveSyncConflicts(local, remote)
        assertEquals("Local", result.single().name)
    }

    @Test
    fun `remote-only items are added to merged result`() {
        val local = listOf(item("x", "2026-07-01T00:00:00Z"))
        val remote = listOf(item("y", "2026-07-01T00:00:00Z"))
        val result = resolveSyncConflicts(local, remote)
        assertEquals(2, result.size)
    }

    @Test
    fun `local-only items are preserved`() {
        val local = listOf(item("p", "2026-07-01T00:00:00Z"))
        val remote = emptyList<InventoryItem>()
        val result = resolveSyncConflicts(local, remote)
        assertEquals(1, result.size)
        assertEquals("p", result.single().id)
    }
}
