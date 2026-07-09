package org.aetherassembly.beforeitsgone.ui.screen.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.aetherassembly.beforeitsgone.domain.model.ShoppingListItem
import org.aetherassembly.beforeitsgone.domain.repository.InventoryRepository
import org.aetherassembly.beforeitsgone.domain.repository.ShoppingListRepository
import org.aetherassembly.beforeitsgone.domain.usecase.getShoppingList
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class ShoppingListUiState(
    val lowStockItems: List<ShoppingListItem> = emptyList(),
    val manualItems: List<ShoppingListItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    val uiState: StateFlow<ShoppingListUiState> = combine(
        inventoryRepository.getAll(),
        shoppingListRepository.observeAll()
    ) { inventoryItems, persistedItems ->
        // Derived low-stock items (not in the manual/persisted list)
        val persistedSourceIds = persistedItems.mapNotNull { it.sourceItemId }.toSet()
        val lowStock = getShoppingList(inventoryItems)
            .filter { it.id !in persistedSourceIds }
            .map { inv ->
                ShoppingListItem(
                    id = "derived_${inv.id}",
                    name = inv.name,
                    quantity = inv.depletionThreshold ?: 1.0,
                    checked = false,
                    addedAt = inv.updatedAt,
                    sourceItemId = inv.id
                )
            }
        ShoppingListUiState(
            lowStockItems = lowStock,
            manualItems = persistedItems.filter { it.sourceItemId == null },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShoppingListUiState()
    )

    fun toggleChecked(item: ShoppingListItem) {
        viewModelScope.launch {
            if (item.id.startsWith("derived_")) {
                // Persist to make check-off sticky
                shoppingListRepository.upsert(
                    item.copy(
                        id = UUID.randomUUID().toString(),
                        checked = !item.checked,
                        sourceItemId = item.sourceItemId
                    )
                )
            } else {
                shoppingListRepository.setChecked(item.id, !item.checked)
            }
        }
    }

    fun addManualItem(name: String, quantity: Double = 1.0) {
        if (name.isBlank()) return
        viewModelScope.launch {
            shoppingListRepository.upsert(
                ShoppingListItem(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    quantity = quantity,
                    checked = false,
                    addedAt = Instant.now().toString(),
                    sourceItemId = null
                )
            )
        }
    }

    fun deleteItem(id: String) {
        if (!id.startsWith("derived_")) {
            viewModelScope.launch { shoppingListRepository.deleteById(id) }
        }
    }

    fun clearChecked() {
        viewModelScope.launch { shoppingListRepository.deleteChecked() }
    }
}
