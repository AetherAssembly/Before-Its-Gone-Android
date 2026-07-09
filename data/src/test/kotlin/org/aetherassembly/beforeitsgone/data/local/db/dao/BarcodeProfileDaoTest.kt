package org.aetherassembly.beforeitsgone.data.local.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.aetherassembly.beforeitsgone.data.local.db.AppDatabase
import org.aetherassembly.beforeitsgone.data.local.db.entity.BarcodeProfileEntity
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
class BarcodeProfileDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: BarcodeProfileDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.barcodeProfileDao()
    }

    @After
    fun teardown() = db.close()

    private fun profile(
        barcode: String = "012345678901",
        productName: String = "Whole Milk",
        location: String? = "fridge",
        shelfLifeDays: Int? = 7
    ) = BarcodeProfileEntity(
        barcode = barcode,
        productName = productName,
        defaultShelfLifeDays = shelfLifeDays,
        preferredLocation = location,
        updatedAt = "2026-07-01T00:00:00.000Z",
        caloriesPer100g = null,
        allergens = emptyList()
    )

    @Test
    fun `upsert and get by barcode`() = runTest {
        dao.upsert(profile())
        val result = dao.getByBarcode("012345678901")
        assertEquals("Whole Milk", result?.productName)
    }

    @Test
    fun `getByBarcode returns null for unknown barcode`() = runTest {
        assertNull(dao.getByBarcode("unknown"))
    }

    @Test
    fun `upsert on conflict replaces existing profile`() = runTest {
        dao.upsert(profile(productName = "Old Name"))
        dao.upsert(profile(productName = "New Name"))
        assertEquals("New Name", dao.getByBarcode("012345678901")?.productName)
    }

    @Test
    fun `getAll returns all profiles`() = runTest {
        dao.upsert(profile("111"))
        dao.upsert(profile("222"))
        assertEquals(2, dao.getAll().size)
    }
}
