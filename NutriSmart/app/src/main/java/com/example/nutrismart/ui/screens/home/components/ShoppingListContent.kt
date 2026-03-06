package com.example.nutrismart.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrismart.ui.screens.home.HomeUiState
import com.example.nutrismart.ui.screens.home.HomeViewModel

@Composable
fun ShoppingListContent(state: HomeUiState, viewModel: HomeViewModel) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Shopping List",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (state.shoppingList?.items.isNullOrEmpty()) {
            Text("Your shopping list is empty.")
        } else {
            LazyColumn {
                items(state.shoppingList!!.items.sortedBy { it.id }) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = item.checked,
                            onCheckedChange = { viewModel.toggleShoppingListItem(item.id, it) }
                        )
                        Column {
                            Text(item.name, fontSize = 16.sp)
                            Text(item.category, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
