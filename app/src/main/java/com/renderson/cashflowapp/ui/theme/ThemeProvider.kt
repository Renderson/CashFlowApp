package com.renderson.cashflowapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.renderson.cashflowapp.data.preferences.SettingsDataStore
import com.renderson.cashflowapp.data.preferences.THEME_DARK
import com.renderson.cashflowapp.data.preferences.THEME_LIGHT

@Composable
fun ThemeProvider(
    settingsDataStore: SettingsDataStore,
    content: @Composable () -> Unit
) {
    val themeMode by settingsDataStore.themeModeFlow.collectAsState(initial = "system")
    val darkTheme = when (themeMode) {
        THEME_DARK -> true
        THEME_LIGHT -> false
        else -> isSystemInDarkTheme()
    }
    CashFlowAppTheme(darkTheme = darkTheme, dynamicColor = false, content = content)
}
