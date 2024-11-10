package de.cypdashuhn.mtgmanager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview


@Preview
@Composable
fun App() {
    var selectedItem by remember { mutableStateOf("Home") }

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            Sidebar(
                items = listOf("Inventory", "Decks", "Settings OOOOO"),
                onItemSelected = { selectedItem = it }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("Selected: $selectedItem")
            }
        }
    }
}