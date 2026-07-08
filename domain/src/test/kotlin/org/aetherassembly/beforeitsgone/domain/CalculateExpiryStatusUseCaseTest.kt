package org.aetherassembly.beforeitsgone.domain

import org.aetherassembly.beforeitsgone.domain.model.ExpiryStatus
import org.aetherassembly.beforeitsgone.domain.usecase.calculateExpiryStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class CalculateExpiryStatusUseCaseTest {

    private val now = Instant.parse("2026-07-01T12:00:00Z")

    @Test
    fun `fresh item returns FRESH`() {
        val expiresAt = now.plus(10, ChronoUnit.DAYS).toString()
        assertEquals(ExpiryStatus.FRESH, calculateExpiryStatus(expiresAt, 3, now))
    }

    @Test
    fun `item expiring exactly on boundary returns EXPIRING_SOON`() {
        val expiresAt = now.plus(2, ChronoUnit.DAYS).toString()
        assertEquals(ExpiryStatus.EXPIRING_SOON, calculateExpiryStatus(expiresAt, 3, now))
    }

    @Test
    fun `item expiring within warning window returns EXPIRING_SOON`() {
        val expiresAt = now.plus(1, ChronoUnit.DAYS).toString()
        assertEquals(ExpiryStatus.EXPIRING_SOON, calculateExpiryStatus(expiresAt, 3, now))
    }

    @Test
    fun `item expiring exactly at now is EXPIRING_SOON not EXPIRED`() {
        // isBefore is strict: expiry == now is not before now, so it's still EXPIRING_SOON
        val expiresAt = now.toString()
        assertEquals(ExpiryStatus.EXPIRING_SOON, calculateExpiryStatus(expiresAt, 3, now))
    }

    @Test
    fun `item expired one millisecond ago is EXPIRED`() {
        val expiresAt = now.minusMillis(1).toString()
        assertEquals(ExpiryStatus.EXPIRED, calculateExpiryStatus(expiresAt, 3, now))
    }

    @Test
    fun `item expired in the past returns EXPIRED`() {
        val expiresAt = now.minus(1, ChronoUnit.DAYS).toString()
        assertEquals(ExpiryStatus.EXPIRED, calculateExpiryStatus(expiresAt, 3, now))
    }

    @Test
    fun `zero warning days — item in future is FRESH`() {
        val expiresAt = now.plus(1, ChronoUnit.DAYS).toString()
        assertEquals(ExpiryStatus.FRESH, calculateExpiryStatus(expiresAt, 0, now))
    }
}
