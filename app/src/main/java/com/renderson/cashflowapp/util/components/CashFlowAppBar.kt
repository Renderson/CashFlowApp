package com.renderson.cashflowapp.util.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashFlowAppBar(
    title: String = "",
    colorViews: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector = Icons.Filled.Home,
    iconBackVisible: Boolean = true,
    iconHomeVisible: Boolean = false,
    onIconBackClick: (() -> Unit)? = null,
    onIconHomeClick: (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        title = {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    color = colorViews,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        navigationIcon = {
            if (iconBackVisible) {
                IconButton(onClick = { onIconBackClick?.let { click -> click() } }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,//painterResource(id = R.drawable.ic_back_native),
                        tint = colorViews,
                        contentDescription = "Voltar"
                    )
                }
            }
        },
        actions = {
            Row {
                if (iconHomeVisible) {
                    IconButton(onClick = { onIconHomeClick?.invoke() }) {
                        Icon(
                            imageVector = icon,
                            modifier = Modifier.size(23.dp),
                            tint = colorViews,
                            contentDescription = "Home"
                        )
                    }
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewQuantaAppBar() {
    CashFlowAppBar(title = "Page Title")
}