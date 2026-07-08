package org.aetherassembly.beforeitsgone.domain.usecase

import org.aetherassembly.beforeitsgone.domain.model.ExpiryStatus
import java.time.Instant
import java.time.temporal.ChronoUnit

fun calculateExpiryStatus(
    expiresAt: String,
    expiryWarningDays: Int,
    now: Instant = Instant.now()
): ExpiryStatus {
    val expiry = Instant.parse(expiresAt)
    return when {
        expiry.isBefore(now) -> ExpiryStatus.EXPIRED
        expiry.isBefore(now.plus(expiryWarningDays.toLong(), ChronoUnit.DAYS)) -> ExpiryStatus.EXPIRING_SOON
        else -> ExpiryStatus.FRESH
    }
}
