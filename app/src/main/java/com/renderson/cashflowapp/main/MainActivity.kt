package com.renderson.cashflowapp.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.renderson.cashflowapp.data.credentials.CredentialRepository
import com.renderson.cashflowapp.data.preferences.SettingsDataStore
import com.renderson.cashflowapp.data.repository.AuthRepository
import com.renderson.cashflowapp.data.session.SessionManager
import com.renderson.cashflowapp.graphs.RootNavigationGraph
import com.renderson.cashflowapp.ui.theme.ThemeProvider
import com.renderson.cashflowapp.viewmodel.AuthViewModel
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var credentialRepository: CredentialRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            authRepository.signOut()
        }
        setContent {
            ThemeProvider(settingsDataStore = settingsDataStore) {
                val cashFlowViewModel: CashFlowViewModel = hiltViewModel()
                val authViewModel: AuthViewModel = hiltViewModel()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavigationGraph(
                        navController = rememberNavController(),
                        cashFlowViewModel = cashFlowViewModel,
                        authViewModel = authViewModel,
                        authRepository = authRepository,
                        credentialRepository = credentialRepository
                    )
                }
            }
        }
    }
}