package ru.ngtu.myfridge.presentation.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.ngtu.myfridge.data.db.ProductEntity
import ru.ngtu.myfridge.data.network.DeepSeekResponse
import ru.ngtu.myfridge.data.network.OpenAIApi
import ru.ngtu.myfridge.presentation.ProductViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoRecognitionScreen(
    onBack: () -> Unit,
    viewModel: ProductViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var analysisResponse by remember { mutableStateOf<DeepSeekResponse?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var currentEmoji by remember { mutableStateOf("🍎") }
    val productsToAdd = remember { mutableStateListOf<ProductEntity>() }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }
    val existingProducts by viewModel.products.collectAsState()

    // Список смайликов
    val emojis = listOf("🍎", "🍌", "🍗", "🧀", "🥕", "🌶", "🍕", "🍩", "🍟", "🥚", "🍭")

    // Эффект для смены смайликов каждые 0.25 секунды
    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            var emojiIndex = 0
            while (isAnalyzing) {
                currentEmoji = emojis[emojiIndex]
                emojiIndex = (emojiIndex + 1) % emojis.size
                delay(250) // 0.25 секунды
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        bmp?.let { bitmap = it }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val stream = context.contentResolver.openInputStream(it)
            val selectedBitmap = BitmapFactory.decodeStream(stream)
            stream?.close()
            bitmap = selectedBitmap
        }
    }

    // Обработка системной кнопки "Назад"
    BackHandler {
        onBack() // Возвращаемся на главный экран
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Распознавание продуктов") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { cameraLauncher.launch(null) }) {
                        Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "Камера")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Камера")
                    }
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = "Галерея")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Галерея")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            bitmap?.let { bmp ->
                item {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Изображение",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isAnalyzing = true
                                val response = OpenAIApi.analyzeFridgePhoto(bmp)
                                isAnalyzing = false
                                if (response == null) {
                                    Toast.makeText(context, "Ошибка анализа фото", Toast.LENGTH_SHORT).show()
                                } else {
                                    analysisResponse = response
                                    productsToAdd.clear()
                                    productsToAdd.addAll(viewModel.parseProductsFromAnalysis(response.products))
                                    Toast.makeText(context, "Фото проанализировано", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isAnalyzing
                    ) {
                        Text("Анализировать фото")
                    }

                    if (isAnalyzing) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ищем продукты $currentEmoji")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (productsToAdd.isNotEmpty()) {
                item {
                    Text("Распознанные продукты:")
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(productsToAdd) { product ->
                    RecognizedProductItem(
                        product = product,
                        existsInFridge = existingProducts.any { it.name == product.name },
                        onEdit = { editingProduct = product },
                        onDelete = { productsToAdd.remove(product) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showConfirmation = true },
                        enabled = productsToAdd.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Добавить в холодильник")
                    }
                }
            }
        }

        if (editingProduct != null) {
            EditRecognizedProductDialog(
                product = editingProduct!!,
                onDismiss = { editingProduct = null },
                onSave = { updatedProduct ->
                    val index = productsToAdd.indexOf(editingProduct)
                    if (index != -1) {
                        productsToAdd[index] = updatedProduct
                    }
                    editingProduct = null
                }
            )
        }

        if (showConfirmation) {
            AlertDialog(
                onDismissRequest = { showConfirmation = false },
                title = { Text("Подтверждение") },
                text = { Text("Вы уверены, что хотите добавить эти продукты?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.addProducts(productsToAdd.toList())
                        Toast.makeText(context, "Продукты добавлены в холодильник", Toast.LENGTH_SHORT).show()
                        showConfirmation = false
                        onBack()
                    }) {
                        Text("Да")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmation = false }) {
                        Text("Нет")
                    }
                }
            )
        }
    }
}

@Composable
fun RecognizedProductItem(
    product: ProductEntity,
    existsInFridge: Boolean,
    onEdit: () -> Unit,
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
            Column {
                Text(
                    text = product.name ?: "Без названия",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (existsInFridge) Color(0xFFFF8C42) else Color.Black
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
fun EditRecognizedProductDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
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
                onSave(updatedProduct)
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