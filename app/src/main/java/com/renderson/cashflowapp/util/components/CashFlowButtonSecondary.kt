package com.renderson.cashflowapp.util.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@Composable
fun CashFlowButtonSecondary(
    isLoading: Boolean = false,
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .skeletonLoading(isLoading),
        colors = if (isLoading) ButtonDefaults.buttonColors(containerColor = Color.Transparent) else ButtonDefaults.outlinedButtonColors()
    ) {
        Text(
            text,
            color = if (isLoading) Color.Transparent else MaterialTheme.colorScheme.primary
        )
    }
}