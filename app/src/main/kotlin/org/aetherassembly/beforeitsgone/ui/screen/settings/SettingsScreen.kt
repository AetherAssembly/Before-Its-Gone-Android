package org.aetherassembly.beforeitsgone.ui.screen.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.aetherassembly.beforeitsgone.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val syncSettings by viewModel.syncSettings.collectAsStateWithLifecycle()
    val androidSettings by viewModel.androidSettings.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportJson(it) } }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let { viewModel.exportCsv(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.importFile(it)
            scope.launch { snackbarHost.showSnackbar("Import complete.") }
        }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = androidSettings.notificationHour,
        initialMinute = androidSettings.notificationMinute
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateAndroidSettings(
                        androidSettings.copy(
                            notificationHour = timePickerState.hour,
                            notificationMinute = timePickerState.minute
                        )
                    )
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHost) { data -> Snackbar(snackbarData = data) }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader("Inventory defaults")
            OutlinedTextField(
                value = settings.defaultLocation,
                onValueChange = { viewModel.updateSettings(settings.copy(defaultLocation = it)) },
                label = { Text("Default location") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = settings.expiryWarningDays.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { days ->
                        viewModel.updateSettings(settings.copy(expiryWarningDays = days))
                    }
                },
                label = { Text("Expiry warning days") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            SectionHeader("Notifications")
            SwitchRow(
                label = "Expiring soon",
                checked = settings.notificationsExpiring,
                onCheckedChange = { viewModel.updateSettings(settings.copy(notificationsExpiring = it)) }
            )
            SwitchRow(
                label = "Expired",
                checked = settings.notificationsExpired,
                onCheckedChange = { viewModel.updateSettings(settings.copy(notificationsExpired = it)) }
            )
            SwitchRow(
                label = "Low stock",
                checked = settings.notificationsLowStock,
                onCheckedChange = { viewModel.updateSettings(settings.copy(notificationsLowStock = it)) }
            )
            ListItem(
                headlineContent = { Text("Notification time") },
                supportingContent = {
                    Text("%02d:%02d".format(androidSettings.notificationHour, androidSettings.notificationMinute))
                },
                trailingContent = {
                    TextButton(onClick = { showTimePicker = true }) { Text("Change") }
                }
            )
            HorizontalDivider()

            SectionHeader("Appearance")
            ListItem(
                headlineContent = { Text("Theme") },
                trailingContent = {
                    Row {
                        listOf("System" to 0, "Light" to 1, "Dark" to 2).forEach { (label, value) ->
                            TextButton(
                                onClick = {
                                    viewModel.updateAndroidSettings(androidSettings.copy(themeOverride = value))
                                },
                                colors = if (androidSettings.themeOverride == value)
                                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                else ButtonDefaults.textButtonColors()
                            ) { Text(label) }
                        }
                    }
                }
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SwitchRow(
                    label = "Dynamic color",
                    checked = androidSettings.dynamicColor,
                    onCheckedChange = {
                        viewModel.updateAndroidSettings(androidSettings.copy(dynamicColor = it))
                    }
                )
            }
            HorizontalDivider()

            SectionHeader("Cloud sync")
            SwitchRow(
                label = "Enable sync",
                checked = syncSettings.enabled,
                onCheckedChange = { viewModel.updateSyncSettings(syncSettings.copy(enabled = it)) }
            )
            OutlinedTextField(
                value = syncSettings.supabaseUrl,
                onValueChange = { viewModel.updateSyncSettings(syncSettings.copy(supabaseUrl = it)) },
                label = { Text("Supabase URL") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = syncSettings.supabaseAnonKey,
                onValueChange = { viewModel.updateSyncSettings(syncSettings.copy(supabaseAnonKey = it)) },
                label = { Text("Anon key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            syncSettings.lastSyncedAt?.let { ts ->
                Text(
                    "Last synced: ${ts.take(16).replace("T", " ")}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.syncNow { success ->
                        scope.launch {
                            snackbarHost.showSnackbar(if (success) "Sync complete." else "Sync failed.")
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                enabled = syncSettings.enabled && syncSettings.supabaseUrl.isNotBlank()
            ) { Text("Sync now") }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            SectionHeader("Data")
            OutlinedButton(
                onClick = { exportJsonLauncher.launch("inventory_export.json") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) { Text("Export JSON") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { exportCsvLauncher.launch("inventory_export.csv") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) { Text("Export CSV") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { importLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) { Text("Import JSON or CSV") }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            SectionHeader("About")
            ListItem(
                headlineContent = { Text("Before It's Gone  v${BuildConfig.VERSION_NAME}") },
                supportingContent = {
                    Column {
                        TextButton(
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/AetherAssembly/Before-Its-Gone-Android"))
                                )
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text(
                                buildAnnotatedString {
                                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                                        append("GitHub")
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            "AGPL-v3 · AetherAssembly",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}
