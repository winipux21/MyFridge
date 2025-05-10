package ru.ngtu.myfridge.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import ru.ngtu.myfridge.data.db.ShoppingItemEntity
import ru.ngtu.myfridge.presentation.ShoppingListViewModel // Используем ShoppingListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel) { // Заменили FridgeViewModel
    val shoppingItemsState = viewModel.shoppingItems.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить в список покупок")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(shoppingItemsState.value) { item ->
                    ShoppingItem(
                        item = item,
                        onPurchasedChange = { isPurchased ->
                            viewModel.updateShoppingItem(item.copy(isPurchased = isPurchased))
                        },
                        onDelete = { viewModel.deleteShoppingItem(item) }
                    )
                }
            }
            if (showAddDialog) {
                AddShoppingItemDialog(
                    onDismiss = { showAddDialog = false },
                    onItemAdded = { name, quantity, unit ->
                        viewModel.addShoppingItem(name, quantity, unit)
                    }
                )
            }
        }
    }
}

@Composable
fun ShoppingItem(
    item: ShoppingItemEntity,
    onPurchasedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Checkbox(
                    checked = item.isPurchased,
                    onCheckedChange = onPurchasedChange
                )
                Text(
                    text = "${item.name}, ${item.quantity} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Удалить элемент")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShoppingItemDialog(
    onDismiss: () -> Unit,
    onItemAdded: (name: String, quantity: Int, unit: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить в список покупок") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Количество") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Единица измерения") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val quantity = quantityText.toIntOrNull() ?: 0
                onItemAdded(name, quantity, unit)
                onDismiss()
            }) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}