package org.aetherassembly.beforeitsgone.ui.screen.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.aetherassembly.beforeitsgone.domain.model.ShoppingListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onBack: () -> Unit,
    onOpenItem: (String) -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showAddSheet by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false; newItemName = "" },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Add to list", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    label = { Text("Item name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showAddSheet = false; newItemName = "" }) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            viewModel.addManualItem(newItemName)
                            newItemName = ""
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showAddSheet = false
                            }
                        },
                        enabled = newItemName.isNotBlank()
                    ) { Text("Add") }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping list") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.clearChecked() }) {
                        Text("Clear checked")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        }
    ) { innerPadding ->
        val allEmpty = state.lowStockItems.isEmpty() && state.manualItems.isEmpty()
        if (allEmpty && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Your shopping list is empty.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (state.lowStockItems.isNotEmpty()) {
                    item {
                        Text(
                            "Low stock",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(state.lowStockItems, key = { it.id }) { item ->
                        ShoppingItemRow(
                            item = item,
                            onToggle = { viewModel.toggleChecked(item) },
                            onDelete = null,
                            onOpenSource = item.sourceItemId?.let { { onOpenItem(it) } }
                        )
                    }
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                }
                if (state.manualItems.isNotEmpty()) {
                    item {
                        Text(
                            "My list",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(state.manualItems, key = { it.id }) { item ->
                        ShoppingItemRow(
                            item = item,
                            onToggle = { viewModel.toggleChecked(item) },
                            onDelete = { viewModel.deleteItem(item.id) },
                            onOpenSource = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingListItem,
    onToggle: () -> Unit,
    onDelete: (() -> Unit)?,
    onOpenSource: (() -> Unit)?
) {
    ListItem(
        headlineContent = {
            Text(
                text = item.name,
                textDecoration = if (item.checked) TextDecoration.LineThrough else null
            )
        },
        supportingContent = { Text("qty: ${item.quantity}") },
        leadingContent = {
            Checkbox(checked = item.checked, onCheckedChange = { onToggle() })
        },
        trailingContent = {
            Row {
                if (onOpenSource != null) {
                    TextButton(onClick = onOpenSource) { Text("View") }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove from list")
                    }
                }
            }
        }
    )
}
