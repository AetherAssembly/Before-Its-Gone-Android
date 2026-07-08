package org.aetherassembly.beforeitsgone.domain

import org.aetherassembly.beforeitsgone.domain.usecase.predictShelfLife
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PredictShelfLifeUseCaseTest {

    @Test
    fun `dairy in fridge returns correct shelf life`() {
        assertEquals(10, predictShelfLife(listOf("Dairy products"), "fridge"))
    }

    @Test
    fun `dairy in freezer returns freezer shelf life`() {
        assertEquals(90, predictShelfLife(listOf("Dairy products"), "freezer"))
    }

    @Test
    fun `meat in fridge`() {
        assertEquals(4, predictShelfLife(listOf("Meat"), "fridge"))
    }

    @Test
    fun `unknown category falls back to pantry default`() {
        // Default pantry = 30
        assertEquals(30, predictShelfLife(listOf("SomethingUnknownXYZ"), "pantry"))
    }

    @Test
    fun `empty categories returns default for fridge`() {
        // Default fridge = 14
        assertEquals(14, predictShelfLife(emptyList(), "fridge"))
    }

    @Test
    fun `pasta in pantry gets long shelf life`() {
        assertEquals(730, predictShelfLife(listOf("Pasta & Noodles"), "pantry"))
    }

    @Test
    fun `chicken poultry maps correctly`() {
        assertEquals(3, predictShelfLife(listOf("Poultry chicken"), "fridge"))
    }
}
