package de.cypdashuhn.mtgmanager.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.cypdashuhn.mtgmanager.CardReader

data class InventoryItem(val id: Int, val name: String, val quantity: Int)

@Composable
fun inventoryView() {
    val items = remember {
        CardReader.getCards()
    }

    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        BasicTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color(0xFF2e464f), MaterialTheme.shapes.small)
                .padding(8.dp),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (searchText.text.isEmpty()) {
                        Text("Search...", color = Color.Gray)
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Data Grid
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2e464f), MaterialTheme.shapes.small)
                .padding(8.dp)
        ) {
            items(items) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF263a42), MaterialTheme.shapes.medium)
                        .padding(2.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    Text("Name: ${item.name}", modifier = Modifier.weight(1f))
                    Text("Set: ${item.set}", modifier = Modifier.weight(1f))
                    Text("Type: ${item.typeLine}", modifier = Modifier.weight(1f))
                    Text("cost: ${item.manaCost}", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
