package com.renderson.cashflowapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.CallMade
import androidx.compose.material.icons.outlined.CallReceived
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.model.Extract

@Composable
fun ItemExtractList(item: Extract) {
    val icon = if (item.type != TypeExtract.DEPOSIT) Icons.Outlined.CallMade else Icons.Outlined.CallReceived
    val tint = if (item.type != TypeExtract.DEPOSIT) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .padding(
                top = 16.dp,
                start = 8.dp,
                end = 8.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .padding(all = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSecondary,
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 20.dp
                        )
                    )
            ) {
                Icon(
                    modifier = Modifier
                        .padding(8.dp),
                    imageVector = icon,
                    tint = tint,
                    contentDescription = "icon"
                )
            }
            Column(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = item.title ?: "",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = item.cash ?: "",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewItemExtractList() {
    ItemExtractList(Extract(cash = "R$ 5.000", description = "Transporte", title = "TESTE", type = TypeExtract.DEPOSIT))
}