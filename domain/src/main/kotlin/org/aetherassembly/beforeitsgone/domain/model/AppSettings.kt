package org.aetherassembly.beforeitsgone.domain.model

data class AppSettings(
    val defaultLocation: String = "fridge",
    val defaultShelfLifeDays: Int = 7,
    val expiryWarningDays: Int = 2,
    val customLocations: List<String> = emptyList(),
    val notificationsExpiring: Boolean = true,
    val notificationsExpired: Boolean = true,
    val notificationsLowStock: Boolean = true
)

data class SyncSettings(
    val enabled: Boolean = false,
    val supabaseUrl: String = "",
    val supabaseAnonKey: String = "",
    val lastSyncedAt: String? = null
)
