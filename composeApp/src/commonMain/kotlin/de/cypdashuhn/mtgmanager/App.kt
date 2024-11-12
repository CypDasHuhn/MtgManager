package de.cypdashuhn.mtgmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.cypdashuhn.mtgmanager.views.decksView
import de.cypdashuhn.mtgmanager.views.inventoryView
import de.cypdashuhn.mtgmanager.views.settingsView

@Composable
fun App() {
    var selectedView by remember { mutableStateOf("Inventory") }

    // Map each view name to the corresponding composable function
    val viewMap: Map<String, @Composable () -> Unit> = mapOf(
        "Inventory" to { inventoryView() },
        "Decks" to { decksView() },
        "Settings" to { settingsView() }
    )

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize().background(Color(0xFF27364F))) {
            sidebar(
                default = "Inventory",
                items = viewMap.keys.toList(),
                onItemSelected = { selectedView = it }
            )
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                viewMap[selectedView]?.invoke()
            }
        }
    }
}