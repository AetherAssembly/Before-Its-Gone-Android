package org.aetherassembly.beforeitsgone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.aetherassembly.beforeitsgone.domain.model.AppSettings
import org.aetherassembly.beforeitsgone.domain.model.SyncSettings

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateSettings(settings: AppSettings)
    fun getSyncSettings(): Flow<SyncSettings>
    suspend fun updateSyncSettings(settings: SyncSettings)
}
