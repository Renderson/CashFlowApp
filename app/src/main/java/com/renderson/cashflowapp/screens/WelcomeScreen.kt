package com.renderson.cashflowapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.data.credentials.CredentialRepository
import com.renderson.cashflowapp.util.components.CashFlowButtonPrimary
import com.renderson.cashflowapp.util.components.CashFlowButtonSecondary
import com.renderson.cashflowapp.util.components.skeletonLoading
import com.renderson.cashflowapp.viewmodel.AuthViewModel

@Composable
fun WelcomeScreen(
    viewModel: AuthViewModel,
    credentialRepository: CredentialRepository,
    onAccessSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val isLoading by viewModel.isLoading.collectAsState()
    var hasStoredCredential by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    LaunchedEffect(activity) {
        if (activity != null) {
            hasStoredCredential = credentialRepository.hasStoredCredential(activity)
        }
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
        Spacer(modifier = Modifier.height(48.dp))

        if (activity != null && hasStoredCredential) {
            CashFlowButtonPrimary(
                isLoading = isLoading,
                text = stringResource(R.string.auth_welcome_access),
                onClick = {
                    viewModel.signInWithCredential(
                        activity = activity,
                        credentialRepository = credentialRepository,
                        onSuccess = onAccessSuccess,
                        onFailure = onLoginClick
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        CashFlowButtonSecondary(
            text = stringResource(R.string.auth_welcome_login),
            onClick = onLoginClick
        )
        Spacer(modifier = Modifier.height(12.dp))

        CashFlowButtonSecondary(
            text = stringResource(R.string.auth_create_account),
            onClick = onCreateAccountClick
        )
    }
}
