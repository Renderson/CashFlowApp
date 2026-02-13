package com.renderson.cashflowapp.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.BuildConfig
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.util.components.CashFlowAppBar

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showLicensesDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CashFlowAppBar(
                title = stringResource(R.string.about_title),
                colorViews = MaterialTheme.colorScheme.onSurface,
                onIconBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.about_privacy_policy),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val url = context.getString(R.string.about_privacy_policy_url)
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.about_terms_of_use),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val url = context.getString(R.string.about_terms_url)
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.about_licenses),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLicensesDialog = true }
            )
        }
    }

    if (showLicensesDialog) {
        LicensesDialog(
            onDismiss = { showLicensesDialog = false }
        )
    }
}

@Composable
private fun LicensesDialog(
    onDismiss: () -> Unit
) {
    val licenses = listOf(
        "AndroidX (Room, Compose, Lifecycle, Navigation, DataStore, Credentials)" to "Apache License 2.0",
        "Firebase" to "Apache License 2.0",
        "Hilt" to "Apache License 2.0",
        "Gson" to "Apache License 2.0",
        "Lottie" to "Apache License 2.0",
        "MPAndroidChart" to "Apache License 2.0"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.about_licenses)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                licenses.forEach { (name, license) ->
                    Text(
                        text = "$name - $license",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.home_ok), color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
