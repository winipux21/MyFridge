package ru.ngtu.myfridge.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onProductAdded: (name: String, quantity: Int, unit: String, expiryDate: LocalDate) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf(LocalDate.now().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить продукт") },
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
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Срок годности (гггг-мм-дд)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val quantity = quantityText.toIntOrNull() ?: 0
                onProductAdded(name, quantity, unit, LocalDate.parse(expiryDate))
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