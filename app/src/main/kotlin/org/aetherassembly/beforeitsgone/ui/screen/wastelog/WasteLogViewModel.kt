package org.aetherassembly.beforeitsgone.ui.screen.wastelog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.aetherassembly.beforeitsgone.domain.model.WasteLogEntry
import org.aetherassembly.beforeitsgone.domain.repository.WasteLogRepository
import javax.inject.Inject

data class WasteChartData(val byCategory: Map<String, Int>)

data class WasteLogUiState(
    val entries: List<WasteLogEntry> = emptyList(),
    val chartData: WasteChartData = WasteChartData(emptyMap()),
    val isLoading: Boolean = true
)

@HiltViewModel
class WasteLogViewModel @Inject constructor(
    private val wasteLogRepository: WasteLogRepository
) : ViewModel() {

    val uiState: StateFlow<WasteLogUiState> = wasteLogRepository.observeAll()
        .map { entries ->
            val byCategory = entries
                .groupBy { it.category?.ifBlank { null } ?: "Other" }
                .mapValues { it.value.size }
            WasteLogUiState(
                entries = entries,
                chartData = WasteChartData(byCategory),
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WasteLogUiState()
        )

    fun deleteEntry(id: String) {
        viewModelScope.launch { wasteLogRepository.deleteById(id) }
    }

    fun clearAll() {
        viewModelScope.launch { wasteLogRepository.deleteAll() }
    }
}
