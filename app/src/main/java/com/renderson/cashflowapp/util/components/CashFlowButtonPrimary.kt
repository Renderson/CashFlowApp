package com.renderson.cashflowapp.util.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@Composable
fun CashFlowButtonPrimary(
    isLoading: Boolean = false,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .skeletonLoading(isLoading),
        enabled = enabled,
        colors = if (isLoading) ButtonDefaults.buttonColors(containerColor = Color.Transparent) else ButtonDefaults.buttonColors()
    ) {
        Text(
            text,
            color = if (isLoading) Color.Transparent else MaterialTheme.colorScheme.onPrimary
        )
    }
}