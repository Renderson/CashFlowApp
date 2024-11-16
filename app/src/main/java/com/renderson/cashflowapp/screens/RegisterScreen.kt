package com.renderson.cashflowapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renderson.cashflowapp.util.components.CashFlowAppBar
import com.renderson.cashflowapp.util.components.CashFlowTextField
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreen(
    viewModel: CashFlowViewModel,
    name: String,
    onClick: () -> Unit
) {
    Scaffold (
        modifier = Modifier
            .fillMaxSize(),
        topBar =  {
            CashFlowAppBar(
                title = name,
                colorViews = MaterialTheme.colorScheme.onSurface,
                onIconBackClick = { onClick.invoke() }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        start = 8.dp,
                        end = 8.dp
                    )
            ) {
                val colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    text = "Valor",
                    textAlign = TextAlign.Center
                )
                CashFlowTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = "",
                    hint = "R$ 0,00",
                    maxLength = 50,
                    colors = colors,
                    onInputChange = {
                        println("Value Input $it")
                    }
                )
                CashFlowTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = "",
                    hint = "Descrição",
                    maxLength = 50,
                    onInputChange = {
                        println("Value Input $it")
                    }
                )
            }
        }
    )
}

@Preview
@Composable
fun PreviewRegisterScreen() {
    RegisterScreen(viewModel = viewModel(), "Registros") {}
}