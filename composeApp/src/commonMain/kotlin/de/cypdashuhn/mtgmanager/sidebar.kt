package de.cypdashuhn.mtgmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun sidebar(
    default: String,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    var highlitedOption by remember { mutableStateOf(default) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(200.dp)
            .background(Color(0xFF37474F)) // A dark background color for the sidebar
            .padding(8.dp)
    ) {
        items.forEach { item ->
            Button(
                onClick = {
                    onItemSelected(item)
                    highlitedOption = item // Update the highlighted option
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(if (item == highlitedOption) 0xFF5c7680 else 0xFF455A64),
                    contentColor = Color.White
                )
            ) {
                Text(item)
            }
        }
    }
}
