package org.aetherassembly.beforeitsgone.data.local.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.aetherassembly.beforeitsgone.data.local.db.AppDatabase
import org.aetherassembly.beforeitsgone.data.local.db.entity.WasteLogEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WasteLogDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WasteLogDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.wasteLogDao()
    }

    @After
    fun teardown() = db.close()

    private fun entry(
        id: String = "e1",
        itemName: String = "Cheese",
        wastedAt: String = "2026-07-05T10:00:00.000Z"
    ) = WasteLogEntity(
        id = id,
        itemName = itemName,
        quantity = 1.0,
        location = "fridge",
        category = "dairy",
        expiresAt = "2026-07-04T23:59:59.000Z",
        wastedAt = wastedAt
    )

    @Test
    fun `insert and observe`() = runTest {
        dao.insert(entry())
        val items = dao.observeAll().first()
        assertEquals(1, items.size)
        assertEquals("e1", items[0].id)
    }

    @Test
    fun `observeAll orders by wastedAt DESC`() = runTest {
        dao.insert(entry("older", wastedAt = "2026-07-03T10:00:00.000Z"))
        dao.insert(entry("newer", wastedAt = "2026-07-05T10:00:00.000Z"))
        val items = dao.observeAll().first()
        assertEquals("newer", items[0].id)
        assertEquals("older", items[1].id)
    }

    @Test
    fun `deleteById removes entry`() = runTest {
        dao.insert(entry("keep"))
        dao.insert(entry("remove"))
        dao.deleteById("remove")
        val items = dao.observeAll().first()
        assertEquals(1, items.size)
        assertEquals("keep", items[0].id)
    }

    @Test
    fun `deleteAll clears all entries`() = runTest {
        dao.insert(entry("a"))
        dao.insert(entry("b"))
        dao.deleteAll()
        assertEquals(0, dao.observeAll().first().size)
    }
}
