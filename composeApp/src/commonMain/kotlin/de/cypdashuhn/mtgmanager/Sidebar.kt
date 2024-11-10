package de.cypdashuhn.mtgmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Sidebar(
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(200.dp)
            .background(Color(0xFF37474F)) // A dark background color for the sidebar
            .padding(8.dp)
    ) {
        items.forEach { item ->
            Button(
                onClick = { onItemSelected(item) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF455A64),
                    contentColor = Color.White
                )
            ) {
                Text(item)
            }
        }
    }
}