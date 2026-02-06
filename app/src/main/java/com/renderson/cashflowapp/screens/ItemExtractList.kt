package com.renderson.cashflowapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.automirrored.outlined.CallReceived
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.extensions.formatForBrazilianCurrency
import com.renderson.cashflowapp.model.Transaction

@Composable
fun ItemExtractList(item: Transaction) {
    val icon = if (item.type != TypeExtract.DEPOSIT) Icons.AutoMirrored.Outlined.CallMade else Icons.AutoMirrored.Outlined.CallReceived
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
                    text = item.description,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = item.amount.formatForBrazilianCurrency(),
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
    ItemExtractList(Transaction(date = "2023-05-15", description = "Roupa", amount = 100.0, type = TypeExtract.DEPOSIT))
}