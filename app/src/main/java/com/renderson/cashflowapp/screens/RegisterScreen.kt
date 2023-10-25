package com.renderson.cashflowapp.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@Composable
fun RegisterScreen(
    viewModel: CashFlowViewModel = hiltViewModel(),
    name: String,
    onClick: () -> Unit
) {
    Text(text = "Novo Registro")
}