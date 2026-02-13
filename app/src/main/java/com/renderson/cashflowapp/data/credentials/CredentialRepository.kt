package com.renderson.cashflowapp.data.credentials

import android.app.Activity
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.exceptions.GetCredentialException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class StoredCredentials(val email: String, val password: String)

@Singleton
class CredentialRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun getCredential(activity: Activity): Result<StoredCredentials> = withContext(Dispatchers.IO) {
        try {
            val request = GetCredentialRequest(
                listOf(GetPasswordOption())
            )
            val result = credentialManager.getCredential(
                context = activity,
                request = request
            )
            val credential = result.credential
            if (credential is androidx.credentials.PasswordCredential) {
                Result.success(
                    StoredCredentials(
                        email = credential.id,
                        password = credential.password
                    )
                )
            } else {
                Result.failure(Exception("Unexpected credential type"))
            }
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveCredential(activity: Activity, email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = CreatePasswordRequest(
                id = email,
                password = password
            )
            credentialManager.createCredential(
                context = activity,
                request = request
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Indica se o botão Acessar deve ser mostrado.
     * A API prepareGetCredential não está disponível no androidx.credentials 1.2,
     * então retorna true (mostra o botão; ao tocar sem credencial, o fluxo navega para LoginScreen).
     */
    suspend fun hasStoredCredential(activity: Activity): Boolean = withContext(Dispatchers.IO) {
        true
    }
}
