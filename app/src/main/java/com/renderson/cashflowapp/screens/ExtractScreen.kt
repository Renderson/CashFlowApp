package com.renderson.cashflowapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtractScreen(name: String, onClick: () -> Unit) {

    var text by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var color = if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
    var padding = if (active) 0.dp else 16.dp

    // Get list DB
    var items = remember {
        mutableStateListOf(
            "Escola",
            "Carro",
            "Casa"
        )
    }

    Scaffold{
        Column(
            modifier = Modifier.background(color)
        ) {
            SearchBar(
                modifier = Modifier.fillMaxWidth(),//.padding(horizontal = padding),
                query = text,
                onQueryChange = {
                   text = it
                },
                onSearch = {
                    if (text.isNotEmpty()) items.add(text)
                    active = false
                    text = ""
                },
                active = active,
                onActiveChange = {
                    active = it
                },
                placeholder = {
                    Text(text = "Search")
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                },
                trailingIcon = {
                    if (active) {
                        Icon(
                            modifier = Modifier.clickable {
                                if (text.isNotEmpty()) {
                                    text = ""
                                } else {
                                    active = false
                                }
                            },
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Icon"
                        )
                    }
                }
            ) {
                items.forEach {
                    Row(
                        modifier = Modifier.padding(all = 14.dp)
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 10.dp),
                            imageVector = Icons.Default.History,
                            contentDescription = "History Icon"
                        )
                        Text(text = it)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewExtractScreen() {
    ExtractScreen("", {})
}