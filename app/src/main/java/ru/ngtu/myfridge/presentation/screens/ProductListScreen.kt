package ru.ngtu.myfridge.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.ngtu.myfridge.data.db.ProductEntity
import ru.ngtu.myfridge.presentation.ProductViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    productViewModel: ProductViewModel,
    onNavigateToPhoto: () -> Unit
) {
    val productsState = productViewModel.products.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить продукт")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column {
                Button(
                    onClick = onNavigateToPhoto,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Добавить через фото")
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(productsState.value) { product ->
                        ProductItem(
                            product = product,
                            onDelete = { productViewModel.deleteProduct(product) },
                            onEdit = { showEditDialog = product }
                        )
                    }
                }
            }
            if (showAddDialog) {
                AddProductDialog(
                    onDismiss = { showAddDialog = false },
                    onProductAdded = { name, quantity, unit, expiryDate ->
                        productViewModel.addProduct(name, quantity, unit, expiryDate)
                    }
                )
            }
            showEditDialog?.let { product ->
                EditProductDialog(
                    product = product,
                    onDismiss = { showEditDialog = null },
                    onProductUpdated = { updatedProduct ->
                        productViewModel.updateProduct(updatedProduct)
                    }
                )
            }
        }
    }
}

@Composable
fun ProductItem(
    product: ProductEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val isExpired = product.expiryDate?.isBefore(LocalDate.now()) ?: false
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
            Column {
                Text(
                    text = product.name ?: "Без названия",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isExpired) Color.Red else Color.Black
                )
                Text(text = "Кол-во: ${product.quantity ?: 0} ${product.unit ?: ""}")
                Text(text = "Срок годности: ${product.expiryDate ?: "Не указан"}")
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Редактировать продукт")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Удалить продукт")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onProductUpdated: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(product.name ?: "") }
    var quantityText by remember { mutableStateOf(product.quantity?.toString() ?: "") }
    var unit by remember { mutableStateOf(product.unit ?: "") }
    var expiryDate by remember { mutableStateOf(product.expiryDate?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать продукт") },
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
                val quantity = quantityText.toIntOrNull() ?: product.quantity ?: 0
                val updatedProduct = product.copy(
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    expiryDate = if (expiryDate.isNotEmpty()) LocalDate.parse(expiryDate) else product.expiryDate
                )
                onProductUpdated(updatedProduct)
                onDismiss()
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}