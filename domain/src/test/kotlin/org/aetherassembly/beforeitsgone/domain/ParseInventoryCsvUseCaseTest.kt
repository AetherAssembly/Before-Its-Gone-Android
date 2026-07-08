package org.aetherassembly.beforeitsgone.domain

import org.aetherassembly.beforeitsgone.domain.usecase.parseInventoryCsv
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParseInventoryCsvUseCaseTest {

    @Test
    fun `valid header and data row parses correctly`() {
        val csv = """
            id,name,quantity,location,barcode,expiresAt,shelfLifeDays,createdAt,updatedAt,category,depletionThreshold,tags,recurring,restockQuantity
            abc,"Cheddar cheese",2.0,fridge,,2026-08-01T23:59:59Z,,2026-07-01T00:00:00Z,2026-07-01T00:00:00Z,dairy,,,false,
        """.trimIndent()
        val items = parseInventoryCsv(csv)
        assertEquals(1, items.size)
        assertEquals("Cheddar cheese", items[0].name)
        assertEquals(2.0, items[0].quantity)
        assertEquals("fridge", items[0].location)
    }

    @Test
    fun `header-only CSV returns empty list`() {
        val csv = "id,name,quantity,location,barcode,expiresAt,shelfLifeDays,createdAt,updatedAt"
        assertTrue(parseInventoryCsv(csv).isEmpty())
    }

    @Test
    fun `blank file returns empty list`() {
        assertTrue(parseInventoryCsv("").isEmpty())
    }

    @Test
    fun `commas inside quoted fields are handled`() {
        val csv = """
            id,name,quantity,location,barcode,expiresAt,shelfLifeDays,createdAt,updatedAt
            xyz,"Salt, pepper & spice",1.0,pantry,,2027-01-01T23:59:59Z,,2026-07-01T00:00:00Z,2026-07-01T00:00:00Z
        """.trimIndent()
        val items = parseInventoryCsv(csv)
        assertEquals(1, items.size)
        assertEquals("Salt, pepper & spice", items[0].name)
    }

    @Test
    fun `date-only expiry is normalized to ISO-8601`() {
        val csv = """
            id,name,quantity,location,barcode,expiresAt,shelfLifeDays,createdAt,updatedAt
            d1,Apple,3.0,fridge,,2026-09-15,,,
        """.trimIndent()
        val items = parseInventoryCsv(csv)
        assertEquals(1, items.size)
        assertTrue(items[0].expiresAt.startsWith("2026-09-15"))
    }

    @Test
    fun `UTF-8 accented names are preserved`() {
        val csv = "id,name,quantity,location,barcode,expiresAt,shelfLifeDays,createdAt,updatedAt\n" +
            "e1,Crème brûlée,1.0,fridge,,2026-08-01T23:59:59Z,,,\n"
        val items = parseInventoryCsv(csv)
        assertEquals("Crème brûlée", items[0].name)
    }
}
