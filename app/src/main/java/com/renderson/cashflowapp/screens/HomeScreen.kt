package com.renderson.cashflowapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.renderson.cashflowapp.util.components.CashFlowAppBar

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(name: String, onClick: () -> Unit) {
    Scaffold (
        modifier = Modifier
            .fillMaxSize(),
        topBar =  {
            CashFlowAppBar(
                title = name,
                iconBackVisible = false
            )
        },
        content = {
            Text(text = "")
        }
    )
}

@Preview
@Composable
fun PreviewHomeScreen() {
    HomeScreen("", {})
}