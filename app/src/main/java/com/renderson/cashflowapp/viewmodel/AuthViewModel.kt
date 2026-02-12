package com.renderson.cashflowapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.renderson.cashflowapp.data.repository.AuthRepository
import com.renderson.cashflowapp.data.repository.ClashFlowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val clashFlowRepository: ClashFlowRepository
) : ViewModel() {

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun clearError() {
        _authError.value = null
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            _isLoading.value = true
            authRepository.signIn(email.trim(), password)
                .onSuccess {
                    clashFlowRepository.migrateLegacyDataIfNeeded(authRepository.getCurrentUserId()!!)
                    onSuccess()
                }
                .onFailure { e ->
                    _authError.value = when (e) {
                        is FirebaseAuthInvalidUserException,
                        is FirebaseAuthInvalidCredentialsException -> "auth_error_invalid_credentials"
                        else -> "auth_error_generic"
                    }
                }
            _isLoading.value = false
        }
    }

    fun createAccount(email: String, password: String, confirmPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            if (password != confirmPassword) {
                _authError.value = "auth_error_password_mismatch"
                return@launch
            }
            if (password.length < 6) {
                _authError.value = "auth_error_weak_password"
                return@launch
            }
            _isLoading.value = true
            authRepository.createAccount(email.trim(), password)
                .onSuccess {
                    clashFlowRepository.migrateLegacyDataIfNeeded(authRepository.getCurrentUserId()!!)
                    onSuccess()
                }
                .onFailure { e ->
                    _authError.value = when (e) {
                        is FirebaseAuthWeakPasswordException -> "auth_error_weak_password"
                        is FirebaseAuthUserCollisionException -> "auth_error_email_in_use"
                        else -> "auth_error_generic"
                    }
                }
            _isLoading.value = false
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun deleteAccount(password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            val userId = authRepository.getCurrentUserId() ?: return@launch
            authRepository.deleteAccount(password)
                .onSuccess {
                    clashFlowRepository.deleteAllDataForUser(userId)
                    authRepository.signOut()
                    onSuccess()
                }
                .onFailure { e ->
                    onError(
                        when (e) {
                            is FirebaseAuthInvalidCredentialsException -> "auth_delete_account_error_password"
                            else -> "auth_error_generic"
                        }
                    )
                }
        }
    }
}
