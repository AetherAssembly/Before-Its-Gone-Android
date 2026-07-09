package org.aetherassembly.beforeitsgone.ui.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.aetherassembly.beforeitsgone.data.local.preferences.AndroidSettings
import org.aetherassembly.beforeitsgone.data.local.preferences.SettingsDataStore
import org.aetherassembly.beforeitsgone.data.remote.supabase.SyncEngine
import org.aetherassembly.beforeitsgone.domain.model.AppSettings
import org.aetherassembly.beforeitsgone.domain.model.SyncSettings
import org.aetherassembly.beforeitsgone.domain.repository.InventoryRepository
import org.aetherassembly.beforeitsgone.domain.repository.SettingsRepository
import org.aetherassembly.beforeitsgone.domain.usecase.buildExportCsv
import org.aetherassembly.beforeitsgone.domain.usecase.buildExportJson
import org.aetherassembly.beforeitsgone.domain.usecase.parseInventoryCsv
import org.aetherassembly.beforeitsgone.domain.usecase.parseInventoryJson
import java.io.File
import java.util.Base64
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inventoryRepository: InventoryRepository,
    private val settingsRepository: SettingsRepository,
    private val settingsDataStore: SettingsDataStore,
    private val syncEngine: SyncEngine
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val syncSettings: StateFlow<SyncSettings> = settingsRepository.getSyncSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncSettings())

    val androidSettings: StateFlow<AndroidSettings> = settingsDataStore.androidSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AndroidSettings())

    fun updateSettings(updated: AppSettings) {
        viewModelScope.launch { settingsRepository.updateSettings(updated) }
    }

    fun updateSyncSettings(updated: SyncSettings) {
        viewModelScope.launch { settingsRepository.updateSyncSettings(updated) }
    }

    fun updateAndroidSettings(updated: AndroidSettings) {
        viewModelScope.launch { settingsDataStore.updateAndroidSettings(updated) }
    }

    fun syncNow(onDone: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val local = inventoryRepository.getAll().first()
                val merged = syncEngine.sync(local)
                merged.forEach { inventoryRepository.upsert(it) }
                onDone(true)
            } catch (_: Exception) {
                onDone(false)
            }
        }
    }

    fun exportJson(uri: Uri) {
        viewModelScope.launch {
            val items = inventoryRepository.getAll().first()
            val json = buildExportJson(items) { path ->
                runCatching {
                    Base64.getEncoder().encodeToString(File(path).readBytes())
                }.getOrNull()
            }
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
        }
    }

    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            val items = inventoryRepository.getAll().first()
            val csv = buildExportCsv(items)
            context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
        }
    }

    fun importFile(uri: Uri) {
        viewModelScope.launch {
            val text = context.contentResolver.openInputStream(uri)
                ?.use { it.readBytes().toString(Charsets.UTF_8) } ?: return@launch
            val items = if (text.trimStart().startsWith("{") || text.trimStart().startsWith("[")) {
                runCatching { parseInventoryJson(text) }.getOrElse { emptyList() }
            } else {
                parseInventoryCsv(text)
            }
            items.forEach { inventoryRepository.upsert(it) }
        }
    }
}
