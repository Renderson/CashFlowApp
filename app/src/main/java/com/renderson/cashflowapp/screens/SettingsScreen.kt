package com.renderson.cashflowapp.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.data.preferences.THEME_DARK
import com.renderson.cashflowapp.data.preferences.THEME_LIGHT
import com.renderson.cashflowapp.data.preferences.THEME_SYSTEM
import com.renderson.cashflowapp.util.components.CashFlowAppBar
import com.renderson.cashflowapp.viewmodel.AuthViewModel
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
    onNavigateToRecurring: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteAccountPassword by remember { mutableStateOf("") }
    var deleteAccountError by remember { mutableStateOf<String?>(null) }
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
                } catch (_: Exception) {
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
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
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

            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                text = stringResource(R.string.settings_recurring_section),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_recurring),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_recurring_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.clickable { onNavigateToRecurring() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                text = stringResource(R.string.auth_section_account),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.auth_sign_out),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.clickable {
                    authViewModel.signOut()
                }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.auth_delete_account),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.auth_delete_account_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable { showDeleteAccountDialog = true }
            )
        }
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteAccountDialog = false
                deleteAccountPassword = ""
                deleteAccountError = null
            },
            title = { Text(stringResource(R.string.auth_delete_account_confirm_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.auth_delete_account_confirm_message))
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    OutlinedTextField(
                        value = deleteAccountPassword,
                        onValueChange = {
                            deleteAccountPassword = it
                            deleteAccountError = null
                        },
                        label = { Text(stringResource(R.string.auth_delete_account_password_hint)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        isError = deleteAccountError != null
                    )
                    deleteAccountError?.let { errorKey ->
                        Spacer(modifier = Modifier.padding(top = 4.dp))
                        Text(
                            text = when (errorKey) {
                                "auth_delete_account_error_password" -> stringResource(R.string.auth_delete_account_error_password)
                                else -> stringResource(R.string.auth_error_generic)
                            },
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.deleteAccount(
                            password = deleteAccountPassword,
                            onSuccess = {
                                showDeleteAccountDialog = false
                                deleteAccountPassword = ""
                                deleteAccountError = null
                            },
                            onError = { errorKey ->
                                deleteAccountError = errorKey
                            }
                        )
                    }
                ) {
                    Text(stringResource(R.string.auth_delete_account), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteAccountDialog = false
                    deleteAccountPassword = ""
                    deleteAccountError = null
                }) {
                    Text(stringResource(R.string.home_cancel), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
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
                            } catch (_: Exception) {
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
