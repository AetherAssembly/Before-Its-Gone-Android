package org.aetherassembly.beforeitsgone.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.aetherassembly.beforeitsgone.domain.model.AppSettings
import org.aetherassembly.beforeitsgone.domain.model.SyncSettings
import javax.inject.Inject

data class AndroidSettings(
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
    val themeOverride: Int = 0,   // 0 = system, 1 = light, 2 = dark
    val dynamicColor: Boolean = true
)

class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val DEFAULT_LOCATION = stringPreferencesKey("default_location")
        val SHELF_LIFE_DAYS = intPreferencesKey("shelf_life_days")
        val EXPIRY_WARNING_DAYS = intPreferencesKey("expiry_warning_days")
        val CUSTOM_LOCATIONS = stringPreferencesKey("custom_locations_json")
        val NOTIF_EXPIRING = booleanPreferencesKey("notifications_expiring")
        val NOTIF_EXPIRED = booleanPreferencesKey("notifications_expired")
        val NOTIF_LOW_STOCK = booleanPreferencesKey("notifications_low_stock")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        val SYNC_URL = stringPreferencesKey("sync_supabase_url")
        val SYNC_KEY = stringPreferencesKey("sync_supabase_anon_key")
        val SYNC_LAST_AT = stringPreferencesKey("sync_last_synced_at")
        // Android-only keys
        val NOTIF_HOUR = intPreferencesKey("notification_hour")
        val NOTIF_MINUTE = intPreferencesKey("notification_minute")
        val THEME_OVERRIDE = intPreferencesKey("theme_override")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    val appSettings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            defaultLocation = prefs[Keys.DEFAULT_LOCATION] ?: "fridge",
            defaultShelfLifeDays = prefs[Keys.SHELF_LIFE_DAYS] ?: 7,
            expiryWarningDays = prefs[Keys.EXPIRY_WARNING_DAYS] ?: 2,
            customLocations = prefs[Keys.CUSTOM_LOCATIONS]?.let { Json.decodeFromString(it) } ?: emptyList(),
            notificationsExpiring = prefs[Keys.NOTIF_EXPIRING] ?: true,
            notificationsExpired = prefs[Keys.NOTIF_EXPIRED] ?: true,
            notificationsLowStock = prefs[Keys.NOTIF_LOW_STOCK] ?: true,
        )
    }

    val syncSettings: Flow<SyncSettings> = dataStore.data.map { prefs ->
        SyncSettings(
            enabled = prefs[Keys.SYNC_ENABLED] ?: false,
            supabaseUrl = prefs[Keys.SYNC_URL] ?: "",
            supabaseAnonKey = prefs[Keys.SYNC_KEY] ?: "",
            lastSyncedAt = prefs[Keys.SYNC_LAST_AT],
        )
    }

    val androidSettings: Flow<AndroidSettings> = dataStore.data.map { prefs ->
        AndroidSettings(
            notificationHour = prefs[Keys.NOTIF_HOUR] ?: 8,
            notificationMinute = prefs[Keys.NOTIF_MINUTE] ?: 0,
            themeOverride = prefs[Keys.THEME_OVERRIDE] ?: 0,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true
        )
    }

    suspend fun updateAppSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_LOCATION] = settings.defaultLocation
            prefs[Keys.SHELF_LIFE_DAYS] = settings.defaultShelfLifeDays
            prefs[Keys.EXPIRY_WARNING_DAYS] = settings.expiryWarningDays
            prefs[Keys.CUSTOM_LOCATIONS] = Json.encodeToString(settings.customLocations)
            prefs[Keys.NOTIF_EXPIRING] = settings.notificationsExpiring
            prefs[Keys.NOTIF_EXPIRED] = settings.notificationsExpired
            prefs[Keys.NOTIF_LOW_STOCK] = settings.notificationsLowStock
        }
    }

    suspend fun updateSyncSettings(settings: SyncSettings) {
        dataStore.edit { prefs ->
            prefs[Keys.SYNC_ENABLED] = settings.enabled
            prefs[Keys.SYNC_URL] = settings.supabaseUrl
            prefs[Keys.SYNC_KEY] = settings.supabaseAnonKey
            settings.lastSyncedAt?.let { prefs[Keys.SYNC_LAST_AT] = it }
        }
    }

    suspend fun updateAndroidSettings(settings: AndroidSettings) {
        dataStore.edit { prefs ->
            prefs[Keys.NOTIF_HOUR] = settings.notificationHour
            prefs[Keys.NOTIF_MINUTE] = settings.notificationMinute
            prefs[Keys.THEME_OVERRIDE] = settings.themeOverride
            prefs[Keys.DYNAMIC_COLOR] = settings.dynamicColor
        }
    }
}
