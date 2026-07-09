package org.aetherassembly.beforeitsgone.ui.screen.additem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    itemId: String?,
    onScan: () -> Unit,
    onDone: () -> Unit,
    viewModel: AddEditItemViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.expiresAt.takeIf { it.isNotBlank() }?.let {
                runCatching {
                    LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                }.getOrNull()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        viewModel.onExpiresAtChange(date.toString())
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId == null) "Add item" else "Edit item") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.barcode,
                onValueChange = viewModel::onBarcodeChange,
                label = { Text("Barcode") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = onScan) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan barcode")
                    }
                }
            )
            // Date picker field — Box overlay captures taps because Modifier.clickable
            // is @Composable in Compose 1.7+ and cannot be used on a Modifier= argument.
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.expiresAt.takeIf { it.isNotBlank() }?.let { raw ->
                        runCatching { LocalDate.parse(raw).format(DISPLAY_FORMAT) }.getOrElse { raw }
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Expiry date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick expiry date")
                    }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }
            OutlinedTextField(
                value = state.quantity,
                onValueChange = viewModel::onQuantityChange,
                label = { Text("Quantity") },
                modifier = Modifier.fillMaxWidth()
            )
            // Location dropdown
            var locationExpanded by remember { mutableStateOf(false) }
            val locations = listOf("fridge" to "Fridge", "freezer" to "Freezer", "pantry" to "Pantry")
            ExposedDropdownMenuBox(
                expanded = locationExpanded,
                onExpandedChange = { locationExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = locations.firstOrNull { it.first == state.location }?.second
                        ?: state.location.replaceFirstChar { it.uppercaseChar() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Location") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false }
                ) {
                    locations.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.onLocationChange(value)
                                locationExpanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = state.category,
                onValueChange = viewModel::onCategoryChange,
                label = { Text("Category (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.shelfLifeDays,
                onValueChange = viewModel::onShelfLifeDaysChange,
                label = { Text("Shelf life in days (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.depletionThreshold,
                onValueChange = viewModel::onDepletionThresholdChange,
                label = { Text("Low-stock threshold (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.save(onDone) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.name.isNotBlank() && state.expiresAt.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}
