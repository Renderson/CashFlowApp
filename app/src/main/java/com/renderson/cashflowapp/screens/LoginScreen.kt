package com.renderson.cashflowapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.data.credentials.CredentialRepository
import com.renderson.cashflowapp.util.components.CashFlowButtonPrimary
import com.renderson.cashflowapp.util.components.CashFlowButtonSecondary
import com.renderson.cashflowapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    credentialRepository: CredentialRepository,
    onLoginSuccess: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val authError by viewModel.authError.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as? android.app.Activity

    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.auth_email)) },
            placeholder = { Text(stringResource(R.string.auth_email_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.auth_password)) },
            placeholder = { Text(stringResource(R.string.auth_password_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = stringResource(if (passwordVisible) R.string.auth_hide_password else R.string.auth_show_password)
                    )
                }
            }
        )

        TextButton(
            onClick = {
                viewModel.clearError()
                showResetPasswordDialog = true
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = stringResource(R.string.auth_forgot_password),
                style = MaterialTheme.typography.bodySmall
            )
        }

        authError?.let { errorKey ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (errorKey) {
                    "auth_error_invalid_credentials" -> stringResource(R.string.auth_error_invalid_credentials)
                    "auth_error_generic" -> stringResource(R.string.auth_error_generic)
                    else -> errorKey
                },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            CashFlowButtonPrimary(
                isLoading = isLoading,
                text = stringResource(R.string.auth_login),
                onClick = {
                    viewModel.signIn(email, password, onSuccess = {
                        activity?.let { act ->
                            scope.launch {
                                credentialRepository.saveCredential(act, email, password)
                            }
                        }
                        onLoginSuccess()
                    })
                }
            )

            CashFlowButtonSecondary(
                text = stringResource(R.string.auth_create_account),
                onClick = onCreateAccountClick
            )
        }
    }

    if (showResetPasswordDialog) {
        ResetPasswordDialog(
            onDismiss = {
                showResetPasswordDialog = false
                viewModel.clearError()
            },
            viewModel = viewModel
        )
    }
}

@Composable
private fun ResetPasswordDialog(
    onDismiss: () -> Unit,
    viewModel: AuthViewModel
) {
    var resetEmail by remember { mutableStateOf("") }
    val authError by viewModel.authError.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.auth_reset_password_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    label = { Text(stringResource(R.string.auth_email)) },
                    placeholder = { Text(stringResource(R.string.auth_email_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = authError != null
                )
                authError?.let { errorKey ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (errorKey) {
                            "auth_error_empty_email" -> stringResource(R.string.auth_error_empty_email)
                            "auth_error_invalid_email" -> stringResource(R.string.auth_error_invalid_email)
                            else -> stringResource(R.string.auth_error_generic)
                        },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.resetPassword(
                        email = resetEmail,
                        onSuccess = onDismiss,
                        onError = { /* authError will be shown */ }
                    )
                }
            ) {
                Text(stringResource(R.string.auth_reset_password))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.home_cancel))
            }
        }
    )
}
