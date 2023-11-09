package com.renderson.cashflowapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.renderson.cashflowapp.util.components.CashFlowAppBar
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreen(
    viewModel: CashFlowViewModel = hiltViewModel(),
    name: String,
    onClick: () -> Unit
) {
    Scaffold (
        modifier = Modifier
            .fillMaxSize(),
        topBar =  {
            CashFlowAppBar(
                title = name,
                onIconBackClick = { onClick.invoke() }
            )
        },
        content = {
            Text(text = "Renderson", color = Color.White)
        }
    )
}

@Preview
@Composable
fun PreviewRegisterScreen() {
    RegisterScreen(viewModel = hiltViewModel(), "", {})
}