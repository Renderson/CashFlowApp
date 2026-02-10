package com.renderson.cashflowapp.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.data.preferences.THEME_DARK
import com.renderson.cashflowapp.data.preferences.THEME_LIGHT
import com.renderson.cashflowapp.data.preferences.THEME_SYSTEM
import com.renderson.cashflowapp.util.components.CashFlowAppBar
import com.renderson.cashflowapp.viewmodel.BackupResult
import com.renderson.cashflowapp.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState(initial = THEME_SYSTEM)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showRestoreConfirmDialog by remember { mutableStateOf<Uri?>(null) }

    val dateFormat = remember { SimpleDateFormat("yyyyMMdd_HHmm", Locale.US) }
    val backupFileName = "${context.getString(R.string.settings_backup_filename_prefix)}_${dateFormat.format(Date())}.json"

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { outputUri ->
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                        val result = viewModel.exportBackup(outputStream)
                        when (result) {
                            is BackupResult.Success ->
                                snackbarHostState.showSnackbar(context.getString(R.string.settings_backup_export_success))
                            is BackupResult.Error ->
                                snackbarHostState.showSnackbar(context.getString(R.string.settings_backup_export_error))
                        }
                    } ?: snackbarHostState.showSnackbar(context.getString(R.string.settings_backup_export_error))
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(context.getString(R.string.settings_backup_export_error))
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { showRestoreConfirmDialog = it }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CashFlowAppBar(
                title = stringResource(R.string.settings_title),
                colorViews = MaterialTheme.colorScheme.onSurface,
                onIconBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                text = stringResource(R.string.settings_appearance),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_theme),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                supportingContent = {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        SegmentedButton(
                            selected = themeMode == THEME_SYSTEM,
                            onClick = { viewModel.setThemeMode(THEME_SYSTEM) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                            label = { Text(stringResource(R.string.settings_theme_system)) }
                        )
                        SegmentedButton(
                            selected = themeMode == THEME_LIGHT,
                            onClick = { viewModel.setThemeMode(THEME_LIGHT) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                            label = { Text(stringResource(R.string.settings_theme_light)) }
                        )
                        SegmentedButton(
                            selected = themeMode == THEME_DARK,
                            onClick = { viewModel.setThemeMode(THEME_DARK) },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                            label = { Text(stringResource(R.string.settings_theme_dark)) }
                        )
                    }
                }
            )

            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                text = stringResource(R.string.settings_backup),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_backup_export),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_backup_export_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Backup,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.clickable { createDocumentLauncher.launch(backupFileName) }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_backup_restore),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_backup_restore_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Restore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.clickable { openDocumentLauncher.launch(arrayOf("application/json", "*/*")) }
            )
        }
    }

    showRestoreConfirmDialog?.let { uri ->
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = null },
            title = { Text(stringResource(R.string.settings_backup_restore_confirm_title)) },
            text = { Text(stringResource(R.string.settings_backup_restore_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmDialog = null
                        scope.launch {
                            try {
                                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                    val result = viewModel.restoreBackup(inputStream)
                                    when (result) {
                                        is BackupResult.Success ->
                                            snackbarHostState.showSnackbar(context.getString(R.string.settings_backup_restore_success))
                                        is BackupResult.Error ->
                                            snackbarHostState.showSnackbar(context.getString(R.string.settings_backup_restore_error))
                                    }
                                } ?: snackbarHostState.showSnackbar(context.getString(R.string.settings_backup_restore_error))
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(context.getString(R.string.settings_backup_restore_error))
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.home_ok), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = null }) {
                    Text(stringResource(R.string.home_cancel), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}
