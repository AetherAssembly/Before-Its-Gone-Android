package org.aetherassembly.beforeitsgone.ui.screen.wastelog

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.aetherassembly.beforeitsgone.domain.model.WasteLogEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasteLogScreen(
    onBack: () -> Unit,
    viewModel: WasteLogViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear waste log?") },
            text = { Text("All waste log entries will be deleted permanently.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAll()
                    showClearDialog = false
                }) { Text("Delete all") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Waste log") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.entries.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear all")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.entries.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No waste recorded yet. Great job!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (state.chartData.byCategory.isNotEmpty()) {
                    item {
                        WasteCategoryChart(
                            data = state.chartData.byCategory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                items(state.entries, key = { it.id }) { entry ->
                    WasteLogEntryCard(
                        entry = entry,
                        onDelete = { viewModel.deleteEntry(entry.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WasteLogEntryCard(
    entry: WasteLogEntry,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = entry.itemName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "qty ${entry.quantity} · ${entry.wastedAt.take(10)}",
                    style = MaterialTheme.typography.bodySmall
                )
                entry.category?.let { cat ->
                    SuggestionChip(onClick = {}, label = { Text(cat) })
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete entry")
            }
        }
    }
}

@Composable
private fun WasteCategoryChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val barColors = listOf(
        Color(0xFF42A5F5), Color(0xFF66BB6A), Color(0xFFFFA726),
        Color(0xFFEF5350), Color(0xFFAB47BC), Color(0xFF26C6DA)
    )
    val entries = data.entries.toList()
    val maxVal = data.values.maxOrNull() ?: 1

    Column(modifier = modifier) {
        Text("Waste by category", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val barCount = entries.size
            if (barCount == 0) return@Canvas
            val gap = 8.dp.toPx()
            val barWidth = (size.width - gap * (barCount - 1)) / barCount
            entries.forEachIndexed { idx, (_, count) ->
                val barHeight = (count.toFloat() / maxVal) * size.height
                val x = idx * (barWidth + gap)
                drawRect(
                    color = barColors[idx % barColors.size],
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            entries.take(6).forEachIndexed { idx, (label, count) ->
                Text("$label ($count)", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
