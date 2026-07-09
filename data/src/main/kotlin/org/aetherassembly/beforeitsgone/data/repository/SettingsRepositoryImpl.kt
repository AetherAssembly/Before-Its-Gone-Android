package org.aetherassembly.beforeitsgone.data.repository

import kotlinx.coroutines.flow.Flow
import org.aetherassembly.beforeitsgone.data.local.preferences.SettingsDataStore
import org.aetherassembly.beforeitsgone.domain.model.AppSettings
import org.aetherassembly.beforeitsgone.domain.model.SyncSettings
import org.aetherassembly.beforeitsgone.domain.repository.SettingsRepository
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : SettingsRepository {
    override fun getSettings(): Flow<AppSettings> = dataStore.appSettings
    override suspend fun updateSettings(settings: AppSettings) = dataStore.updateAppSettings(settings)
    override fun getSyncSettings(): Flow<SyncSettings> = dataStore.syncSettings
    override suspend fun updateSyncSettings(settings: SyncSettings) = dataStore.updateSyncSettings(settings)
}
