package com.renderson.cashflowapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renderson.cashflowapp.data.preferences.SettingsDataStore
import com.renderson.cashflowapp.data.preferences.THEME_DARK
import com.renderson.cashflowapp.data.preferences.THEME_LIGHT
import com.renderson.cashflowapp.data.preferences.THEME_SYSTEM
import com.renderson.cashflowapp.data.cloud.CloudBackupRepository
import com.renderson.cashflowapp.data.repository.AuthRepository
import com.renderson.cashflowapp.data.repository.ClashFlowRepository
import com.renderson.cashflowapp.usecase.ExportBackupUseCase
import com.renderson.cashflowapp.usecase.RestoreBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

sealed class BackupResult {
    data object Success : BackupResult()
    data class Error(val message: String) : BackupResult()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val repository: ClashFlowRepository,
    private val authRepository: AuthRepository,
    private val cloudBackupRepository: CloudBackupRepository,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase
) : ViewModel() {

    val themeMode = settingsDataStore.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = THEME_SYSTEM
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsDataStore.setThemeMode(mode)
        }
    }

    suspend fun exportBackup(outputStream: OutputStream): BackupResult {
        return runCatching {
            val extract = repository.getExtractSnapshot()
            val transactions = extract.years
                .flatMap { it.months }
                .flatMap { it.transactions }
            exportBackupUseCase(transactions, outputStream).getOrThrow()
            BackupResult.Success
        }.getOrElse { BackupResult.Error(it.message ?: "Unknown error") }
    }

    suspend fun restoreBackup(inputStream: InputStream): BackupResult {
        return restoreBackupUseCase(inputStream).fold(
            onSuccess = { BackupResult.Success },
            onFailure = { BackupResult.Error(it.message ?: "Unknown error") }
        )
    }

    suspend fun uploadBackupToCloud(): BackupResult {
        val userId = authRepository.getCurrentUserId()
            ?: return BackupResult.Error("Not logged in")
        return runCatching {
            val extract = repository.getExtractSnapshot()
            val transactions = extract.years
                .flatMap { it.months }
                .flatMap { it.transactions }
            val outputStream = ByteArrayOutputStream()
            exportBackupUseCase(transactions, outputStream).getOrThrow()
            cloudBackupRepository.uploadBackup(userId, outputStream.toByteArray()).getOrThrow()
            BackupResult.Success
        }.getOrElse { BackupResult.Error(it.message ?: "Unknown error") }
    }

    suspend fun restoreBackupFromCloud(): BackupResult {
        val userId = authRepository.getCurrentUserId()
            ?: return BackupResult.Error("Not logged in")
        return runCatching {
            val bytes = cloudBackupRepository.downloadBackup(userId).getOrThrow()
            val inputStream = ByteArrayInputStream(bytes)
            restoreBackupUseCase(inputStream).getOrThrow()
            BackupResult.Success
        }.getOrElse { e ->
            val message = when {
                e.message?.contains("object does not exist", ignoreCase = true) == true -> "settings_backup_cloud_empty"
                else -> e.message ?: "Unknown error"
            }
            BackupResult.Error(message)
        }
    }
}
