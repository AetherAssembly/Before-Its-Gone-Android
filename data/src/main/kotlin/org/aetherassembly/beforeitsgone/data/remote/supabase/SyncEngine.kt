package org.aetherassembly.beforeitsgone.data.remote.supabase

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import org.aetherassembly.beforeitsgone.data.local.preferences.SettingsDataStore
import org.aetherassembly.beforeitsgone.domain.model.InventoryItem
import org.aetherassembly.beforeitsgone.domain.usecase.resolveSyncConflicts
import java.time.Instant
import javax.inject.Inject

/**
 * Talks to the same `inventory_sync` table the desktop app uses.
 * Builds its Supabase client on-demand from DataStore so URL/key changes
 * take effect on the next manual sync without needing an app restart.
 * Sync is a no-op when disabled or when the URL is blank.
 */
class SyncEngine @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    suspend fun sync(local: List<InventoryItem>): List<InventoryItem> {
        val settings = settingsDataStore.syncSettings.first()
        if (!settings.enabled ||
            settings.supabaseUrl.isBlank() ||
            settings.supabaseAnonKey.isBlank()
        ) return local

        val client = createSupabaseClient(settings.supabaseUrl, settings.supabaseAnonKey) {
            install(Postgrest)
            install(Auth)
        }
        return try {
            val userId = client.auth.currentUserOrNull()?.id ?: return local
            val table = client.postgrest["inventory_sync"]

            if (local.isNotEmpty()) {
                table.upsert(local.map { it.toRow(userId) })
            }

            val remote = table.select { filter { eq("user_id", userId) } }
                .decodeList<SyncRow>()
                .map { it.data }

            val merged = resolveSyncConflicts(local, remote)
            settingsDataStore.updateSyncSettings(settings.copy(lastSyncedAt = Instant.now().toString()))
            merged
        } catch (_: Exception) {
            local
        } finally {
            client.close()
        }
    }
}

@Serializable
private data class SyncRow(
    val id: String,
    val user_id: String,
    val updated_at: String,
    val data: InventoryItem
)

private fun InventoryItem.toRow(userId: String) = SyncRow(
    id = id,
    user_id = userId,
    updated_at = updatedAt,
    data = this
)
