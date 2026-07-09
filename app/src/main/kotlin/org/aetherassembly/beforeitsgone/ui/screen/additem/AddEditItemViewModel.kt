package org.aetherassembly.beforeitsgone.ui.screen.additem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.aetherassembly.beforeitsgone.domain.model.InventoryItem
import org.aetherassembly.beforeitsgone.domain.repository.BarcodeProfileRepository
import org.aetherassembly.beforeitsgone.domain.repository.InventoryRepository
import org.aetherassembly.beforeitsgone.domain.repository.ItemHistoryRepository
import org.aetherassembly.beforeitsgone.domain.usecase.buildItemHistoryEntry
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class AddEditUiState(
    val name: String = "",
    val barcode: String = "",
    val expiresAt: String = "",
    val quantity: String = "1",
    val location: String = "fridge",
    val shelfLifeDays: String = "",
    val category: String = "",
    val depletionThreshold: String = ""
)

@HiltViewModel
class AddEditItemViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val itemHistoryRepository: ItemHistoryRepository,
    private val barcodeProfileRepository: BarcodeProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: String? = savedStateHandle["itemId"]

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        itemId?.let { id ->
            viewModelScope.launch {
                inventoryRepository.getById(id)?.let { item ->
                    _uiState.update {
                        AddEditUiState(
                            name = item.name,
                            barcode = item.barcode ?: "",
                            expiresAt = item.expiresAt.take(10),
                            quantity = item.quantity.toString(),
                            location = item.location,
                            shelfLifeDays = item.shelfLifeDays?.toString() ?: "",
                            category = item.category ?: "",
                            depletionThreshold = item.depletionThreshold?.toString() ?: ""
                        )
                    }
                }
            }
        }

        // Observe barcode scanned by BarcodeScannerScreen placed on this back-stack entry
        viewModelScope.launch {
            savedStateHandle.getStateFlow("scanned_barcode", "").collect { barcode ->
                if (barcode.isNotBlank()) {
                    savedStateHandle["scanned_barcode"] = ""   // consume
                    _uiState.update { it.copy(barcode = barcode) }
                    // Prefill from profile cache
                    barcodeProfileRepository.getByBarcode(barcode)?.let { profile ->
                        _uiState.update { current ->
                            current.copy(
                                name = if (current.name.isBlank()) profile.productName else current.name,
                                location = profile.preferredLocation ?: current.location,
                                shelfLifeDays = profile.defaultShelfLifeDays?.toString()
                                    ?: current.shelfLifeDays
                            )
                        }
                    }
                }
            }
        }
    }

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onBarcodeChange(v: String) = _uiState.update { it.copy(barcode = v) }
    fun onExpiresAtChange(v: String) = _uiState.update { it.copy(expiresAt = v) }
    fun onQuantityChange(v: String) = _uiState.update { it.copy(quantity = v) }
    fun onLocationChange(v: String) = _uiState.update { it.copy(location = v) }
    fun onShelfLifeDaysChange(v: String) = _uiState.update { it.copy(shelfLifeDays = v) }
    fun onCategoryChange(v: String) = _uiState.update { it.copy(category = v) }
    fun onDepletionThresholdChange(v: String) = _uiState.update { it.copy(depletionThreshold = v) }

    fun save(onDone: () -> Unit) {
        val state = _uiState.value
        val now = Instant.now().toString()
        val isNew = itemId == null
        viewModelScope.launch {
            val item = InventoryItem(
                id = itemId ?: UUID.randomUUID().toString(),
                name = state.name.trim(),
                quantity = state.quantity.toDoubleOrNull() ?: 1.0,
                location = state.location.trim().ifBlank { "fridge" },
                barcode = state.barcode.trim().ifBlank { null },
                expiresAt = "${state.expiresAt.trim()}T23:59:59.000Z",
                shelfLifeDays = state.shelfLifeDays.toIntOrNull(),
                category = state.category.trim().ifBlank { null },
                depletionThreshold = state.depletionThreshold.toDoubleOrNull(),
                createdAt = now,
                updatedAt = now
            )
            inventoryRepository.upsert(item)
            if (isNew) {
                val existing = itemHistoryRepository.getByName(item.name)
                itemHistoryRepository.upsert(buildItemHistoryEntry(item, existing))
            }
            onDone()
        }
    }
}
