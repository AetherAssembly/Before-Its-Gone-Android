package org.aetherassembly.beforeitsgone.ui.screen.inventory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onAddItem: () -> Unit,
    onEditItem: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWasteLog: () -> Unit,
    onOpenShoppingList: () -> Unit,
    onOpenRecipes: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Before It's Gone") },
                actions = {
                    IconButton(onClick = onOpenShoppingList) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Shopping list")
                    }
                    IconButton(onClick = onOpenRecipes) {
                        Icon(Icons.Default.Restaurant, contentDescription = "Recipe ideas")
                    }
                    IconButton(onClick = onOpenWasteLog) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Waste log")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItem) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        }
    ) { innerPadding ->
        if (state.items.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No items yet. Tap + to add your first item.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    InventoryItemCard(
                        item = item,
                        expiryStatus = state.expiryStatuses[item.id],
                        onClick = { onEditItem(item.id) },
                        onDelete = { viewModel.deleteItem(item.id) },
                        onWaste = { viewModel.wasteItem(item) }
                    )
                }
            }
        }
    }
}
