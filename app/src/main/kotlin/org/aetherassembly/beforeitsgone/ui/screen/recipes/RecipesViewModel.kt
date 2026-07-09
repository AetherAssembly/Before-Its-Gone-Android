package org.aetherassembly.beforeitsgone.ui.screen.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.aetherassembly.beforeitsgone.data.remote.themealdb.MealSummary
import org.aetherassembly.beforeitsgone.data.remote.themealdb.TheMealDbClient
import org.aetherassembly.beforeitsgone.domain.model.ExpiryStatus
import org.aetherassembly.beforeitsgone.domain.repository.InventoryRepository
import org.aetherassembly.beforeitsgone.domain.repository.SettingsRepository
import org.aetherassembly.beforeitsgone.domain.usecase.calculateExpiryStatus
import javax.inject.Inject

data class RecipeMeal(
    val summary: MealSummary,
    val matchedIngredients: List<String>
)

data class RecipesUiState(
    val isLoading: Boolean = false,
    val meals: List<RecipeMeal> = emptyList(),
    val noExpiringItems: Boolean = false
)

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val settingsRepository: SettingsRepository,
    private val mealDbClient: TheMealDbClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipesUiState(isLoading = true))
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, meals = emptyList()) }

            val settings = settingsRepository.getSettings().first()
            val items = inventoryRepository.getAll().first()

            val expiringItems = items.filter { item ->
                calculateExpiryStatus(item.expiresAt, settings.expiryWarningDays) == ExpiryStatus.EXPIRING_SOON
            }.take(5) // cap API calls

            if (expiringItems.isEmpty()) {
                _uiState.update { RecipesUiState(isLoading = false, noExpiringItems = true) }
                return@launch
            }

            val mealToIngredients = mutableMapOf<String, Pair<MealSummary, MutableList<String>>>()
            for (item in expiringItems) {
                for (meal in mealDbClient.searchByIngredient(item.name)) {
                    mealToIngredients.getOrPut(meal.id) { meal to mutableListOf() }
                        .second.add(item.name)
                }
            }

            val sorted = mealToIngredients.values
                .sortedByDescending { it.second.size }
                .take(20)
                .map { RecipeMeal(it.first, it.second) }

            _uiState.update { RecipesUiState(isLoading = false, meals = sorted) }
        }
    }
}
