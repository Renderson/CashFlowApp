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
import com.renderson.cashflowapp.data.preferences.SettingsDataStore
import com.renderson.cashflowapp.graphs.RootNavigationGraph
import com.renderson.cashflowapp.ui.theme.ThemeProvider
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemeProvider(settingsDataStore = settingsDataStore) {
                val viewModel: CashFlowViewModel = hiltViewModel()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavigationGraph(
                        navController = rememberNavController(),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}