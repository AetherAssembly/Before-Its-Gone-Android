package org.aetherassembly.beforeitsgone.data.local.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.aetherassembly.beforeitsgone.data.local.db.AppDatabase
import org.aetherassembly.beforeitsgone.data.local.db.entity.InventoryItemEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class InventoryItemDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: InventoryItemDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.inventoryItemDao()
    }

    @After
    fun teardown() = db.close()

    private fun entity(
        id: String = "id1",
        name: String = "Milk",
        location: String = "fridge",
        expiresAt: String = "2026-08-01T23:59:59.000Z"
    ) = InventoryItemEntity(
        id = id,
        name = name,
        quantity = 1.0,
        location = location,
        barcode = null,
        expiresAt = expiresAt,
        shelfLifeDays = null,
        createdAt = "2026-07-01T00:00:00.000Z",
        updatedAt = "2026-07-01T00:00:00.000Z",
        category = null,
        depletionThreshold = null,
        tags = emptyList(),
        recurring = false,
        restockQuantity = null,
        photoPath = null
    )

    @Test
    fun `insert and get by id`() = runTest {
        val item = entity()
        dao.upsert(item)
        assertEquals(item, dao.getById("id1"))
    }

    @Test
    fun `getById returns null for missing id`() = runTest {
        assertNull(dao.getById("missing"))
    }

    @Test
    fun `observeAll emits inserted items`() = runTest {
        dao.upsert(entity("a", "Apple"))
        dao.upsert(entity("b", "Banana", expiresAt = "2026-09-01T23:59:59.000Z"))
        val items = dao.observeAll().first()
        assertEquals(2, items.size)
        // ordered by expiresAt ASC
        assertEquals("a", items[0].id)
        assertEquals("b", items[1].id)
    }

    @Test
    fun `upsert replaces existing item`() = runTest {
        dao.upsert(entity(name = "Old name"))
        dao.upsert(entity(name = "New name"))
        assertEquals("New name", dao.getById("id1")?.name)
    }

    @Test
    fun `deleteById removes item`() = runTest {
        dao.upsert(entity())
        dao.deleteById("id1")
        assertNull(dao.getById("id1"))
    }

    @Test
    fun `deleteAll clears all items`() = runTest {
        dao.upsert(entity("a"))
        dao.upsert(entity("b"))
        dao.deleteAll()
        assertEquals(0, dao.observeAll().first().size)
    }
}
