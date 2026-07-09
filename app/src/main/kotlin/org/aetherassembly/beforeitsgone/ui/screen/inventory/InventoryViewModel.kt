package org.aetherassembly.beforeitsgone.ui.screen.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.aetherassembly.beforeitsgone.domain.model.ExpiryStatus
import org.aetherassembly.beforeitsgone.domain.model.InventoryItem
import org.aetherassembly.beforeitsgone.domain.model.WasteLogEntry
import org.aetherassembly.beforeitsgone.domain.repository.InventoryRepository
import org.aetherassembly.beforeitsgone.domain.repository.SettingsRepository
import org.aetherassembly.beforeitsgone.domain.repository.WasteLogRepository
import org.aetherassembly.beforeitsgone.domain.usecase.calculateExpiryStatus
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class InventoryUiState(
    val items: List<InventoryItem> = emptyList(),
    val expiryStatuses: Map<String, ExpiryStatus> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val settingsRepository: SettingsRepository,
    private val wasteLogRepository: WasteLogRepository
) : ViewModel() {

    val uiState: StateFlow<InventoryUiState> = combine(
        inventoryRepository.getAll(),
        settingsRepository.getSettings()
    ) { items, settings ->
        val statuses = items.associate { item ->
            item.id to calculateExpiryStatus(item.expiresAt, settings.expiryWarningDays)
        }
        InventoryUiState(items = items, expiryStatuses = statuses, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InventoryUiState()
    )

    fun deleteItem(id: String) {
        viewModelScope.launch { inventoryRepository.delete(id) }
    }

    fun wasteItem(item: InventoryItem) {
        viewModelScope.launch {
            wasteLogRepository.insert(
                WasteLogEntry(
                    id = UUID.randomUUID().toString(),
                    itemName = item.name,
                    quantity = item.quantity,
                    location = item.location,
                    category = item.category,
                    expiresAt = item.expiresAt,
                    wastedAt = Instant.now().toString()
                )
            )
            inventoryRepository.delete(item.id)
        }
    }
}
