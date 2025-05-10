package ru.ngtu.myfridge.presentation.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import com.google.firebase.auth.FirebaseAuth
import ru.ngtu.myfridge.data.db.*
import ru.ngtu.myfridge.presentation.ProductViewModel
import ru.ngtu.myfridge.presentation.ShoppingListViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDetailScreen(
    family: Family,
    viewModel: ProductViewModel,
    shoppingListViewModel: ShoppingListViewModel,
    onBack: () -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
    val products by viewModel.products.collectAsState(initial = emptyList())
    val familyProducts by viewModel.getFamilyProducts(family.id).collectAsState(initial = emptyList())
    val recipes by viewModel.savedRecipes.collectAsState(initial = emptyList())
    val familyRecipes by viewModel.getFamilyRecipes(family.id).collectAsState(initial = emptyList())
    val shoppingItems by viewModel.getFamilyShoppingItems(family.id).collectAsState(initial = emptyList())
    var showShareDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val isCreator by remember { mutableStateOf(family.creator == userId) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        Log.d("FamilyDetailScreen", "Экран открыт для семьи: ${family.id}, userId: $userId, family.creator: ${family.creator}, isCreator: $isCreator")
    }

    LaunchedEffect(family.id) {
        viewModel.getFamilyProducts(family.id)
        viewModel.getFamilyRecipes(family.id)
        viewModel.getFamilyShoppingItems(family.id)
    }

    BackHandler(enabled = true) {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "My Fridge",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                    }
                    Log.d("FamilyDetailScreen", "Рендеринг заголовка TopAppBar: My Fridge")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                        Log.d("FamilyDetailScreen", "Рендеринг кнопки Назад")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = family.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (isCreator) {
                    Row {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Редактировать название семьи")
                        }
                        IconButton(onClick = {
                            Log.d("FamilyDetailScreen", "Нажата кнопка удаления семьи для ${family.id}")
                            coroutineScope.launch {
                                val success = viewModel.deleteFamily(family.id, userId)
                                if (success) {
                                    Toast.makeText(context, "Семья удалена", Toast.LENGTH_SHORT).show()
                                    onBack()
                                } else {
                                    Toast.makeText(context, "Только создатель может удалить семью", Toast.LENGTH_SHORT).show()
                                    Log.e("FamilyDetailScreen", "Удаление не удалось: creatorId не совпадает или ошибка Firebase")
                                }
                            }
                        }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Удалить семью",
                                tint = Color.Red
                            )
                        }
                    }
                } else {
                    Text(
                        "Не создатель",
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Text(
                text = "ID семьи: ${family.id}",
                modifier = Modifier.clickable {
                    clipboardManager.setText(AnnotatedString(family.id))
                    Toast.makeText(context, "ID скопирован: ${family.id}", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showShareDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Поделиться с семьёй")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Семейные продукты:", style = MaterialTheme.typography.titleMedium)
            if (familyProducts.isEmpty()) {
                Text("Нет семейных продуктов")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                    items(familyProducts.size) { index ->
                        val product = familyProducts[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${product.name}: ${product.quantity} ${product.unit} (до ${product.expiryDate ?: "не указан"})",
                                modifier = Modifier.weight(1f)
                            )
                            if (isCreator) {
                                Log.d("FamilyDetailScreen", "Рендеринг кнопки удаления продукта: ${product.name}")
                                IconButton(onClick = {
                                    Log.d("FamilyDetailScreen", "Нажата кнопка удаления продукта: ${product.name}")
                                    coroutineScope.launch {
                                        viewModel.deleteFamilyProduct(family.id, ProductEntity(product.id, product.name, product.quantity, product.unit, product.expiryDate), userId)
                                        Toast.makeText(context, "Продукт удалён", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Удалить продукт")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Семейные рецепты:", style = MaterialTheme.typography.titleMedium)
            if (familyRecipes.isEmpty()) {
                Text("Нет семейных рецептов")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                    items(familyRecipes.size) { index ->
                        val recipe = familyRecipes[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FamilyRecipeItem(recipe = RecipeEntity(recipe.id, recipe.title, recipe.ingredients, recipe.instructions, recipe.mealType, recipe.recipeMode), modifier = Modifier.weight(1f))
                            if (isCreator) {
                                Log.d("FamilyDetailScreen", "Рендеринг кнопки удаления рецепта: ${recipe.title}")
                                IconButton(onClick = {
                                    Log.d("FamilyDetailScreen", "Нажата кнопка удаления рецепта: ${recipe.title}")
                                    coroutineScope.launch {
                                        viewModel.deleteFamilyRecipe(family.id, RecipeEntity(recipe.id, recipe.title, recipe.ingredients, recipe.instructions, recipe.mealType, recipe.recipeMode), userId)
                                        Toast.makeText(context, "Рецепт удалён", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Удалить рецепт")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Семейный список покупок:", style = MaterialTheme.typography.titleMedium)
            if (shoppingItems.isEmpty()) {
                Text("Нет элементов в семейном списке покупок")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                    items(shoppingItems.size) { index ->
                        val item = shoppingItems[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.name}, ${item.quantity} ${item.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null,
                                modifier = Modifier.weight(1f)
                            )
                            if (isCreator) {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        viewModel.deleteFamilyShoppingItem(family.id, ShoppingItemEntity(item.id, item.name, item.quantity, item.unit, item.isPurchased), userId)
                                        Toast.makeText(context, "Элемент удалён", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Удалить элемент")
                                }
                            }
                        }
                    }
                }
            }

            if (showEditDialog) {
                EditFamilyNameDialog(
                    currentName = family.name,
                    onDismiss = { showEditDialog = false },
                    onNameUpdated = { newName ->
                        viewModel.updateFamilyName(family.id, newName)
                        Toast.makeText(context, "Название семьи обновлено", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    }
                )
            }

            if (showShareDialog) {
                AlertDialog(
                    onDismissRequest = { showShareDialog = false },
                    title = { Text("Поделиться с семьёй") },
                    text = {
                        Column {
                            Text("Выберите, что поделиться:")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Продукты в вашем холодильнике:", style = MaterialTheme.typography.titleMedium)
                            if (products.isEmpty()) {
                                Text("Ваш холодильник пуст")
                            } else {
                                LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                                    items(products.size) { index ->
                                        val product = products[index]
                                        Text("${product.name ?: "Без названия"}: ${product.quantity ?: 0} ${product.unit ?: ""} (до ${product.expiryDate ?: "не указан"})")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                coroutineScope.launch {
                                    try {
                                        // Фильтруем продукты, у которых срок годности не истёк
                                        val currentDate = LocalDate.now()
                                        val validProducts = products.filter { product ->
                                            product.expiryDate?.let { expiry ->
                                                !expiry.isBefore(currentDate)
                                            } ?: true // Если expiryDate null, считаем продукт действительным
                                        }
                                        val expiredProducts = products - validProducts.toSet()

                                        if (expiredProducts.isNotEmpty()) {
                                            val expiredNames = expiredProducts.joinToString(", ") { it.name ?: "Без названия" }
                                            Toast.makeText(
                                                context,
                                                "Просроченные продукты исключены: $expiredNames",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                        if (validProducts.isNotEmpty()) {
                                            viewModel.shareProducts(family.id, validProducts)
                                            Toast.makeText(context, "Холодильник поделён с семьёй", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Нет действительных продуктов для передачи", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                    showShareDialog = false
                                }
                            }) {
                                Text("Поделиться холодильником")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ваши сохранённые рецепты:", style = MaterialTheme.typography.titleMedium)
                            if (recipes.isEmpty()) {
                                Text("У вас нет сохранённых рецептов")
                            } else {
                                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                                    items(recipes.size) { index ->
                                        val recipe = recipes[index]
                                        val isAlreadyShared = familyRecipes.any { it.title == recipe.title && it.mealType == recipe.mealType }
                                        Button(
                                            onClick = {
                                                if (recipe.title == null || recipe.ingredients == null ||
                                                    recipe.instructions == null || recipe.mealType == null ||
                                                    recipe.recipeMode == null) {
                                                    Toast.makeText(context, "Рецепт содержит неполные данные и не может быть добавлен", Toast.LENGTH_LONG).show()
                                                    showShareDialog = false
                                                    return@Button
                                                }
                                                if (!isAlreadyShared) {
                                                    viewModel.shareRecipe(family.id, recipe)
                                                    Toast.makeText(context, "Рецепт ${recipe.title} поделён с семьёй", Toast.LENGTH_SHORT).show()
                                                    showShareDialog = false
                                                } else {
                                                    showShareDialog = false
                                                }
                                            },
                                            enabled = !isAlreadyShared
                                        ) {
                                            Text(
                                                if (isAlreadyShared) "Рецепт ${recipe.title ?: "Без названия"} уже добавлен"
                                                else "Поделиться рецептом: ${recipe.title ?: "Без названия"}"
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ваш список покупок:", style = MaterialTheme.typography.titleMedium)
                            val personalShoppingItems by shoppingListViewModel.shoppingItems.collectAsState(initial = emptyList())
                            if (personalShoppingItems.isEmpty()) {
                                Text("Ваш список покупок пуст")
                            } else {
                                LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                                    items(personalShoppingItems.size) { index ->
                                        val item = personalShoppingItems[index]
                                        Text("${item.name}, ${item.quantity} ${item.unit}")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                coroutineScope.launch {
                                    try {
                                        viewModel.shareShoppingItems(family.id, personalShoppingItems)
                                        Toast.makeText(context, "Список покупок поделён с семьёй", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                    showShareDialog = false
                                }
                            }) {
                                Text("Поделиться списком покупок")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showShareDialog = false }) {
                            Text("Закрыть")
                        }
                    }
                )
            }
        }
    }
}

// Функция для отображения типа блюда на русском языке
fun getDisplayMealType(mealType: String?): String {
    return when (mealType) {
        "BREAKFAST" -> "Завтрак"
        "LUNCH" -> "Обед"
        "DINNER" -> "Ужин"
        else -> mealType ?: "Не указан"
    }
}

@Composable
fun FamilyRecipeItem(recipe: RecipeEntity, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${recipe.title ?: "Без названия"} (${getDisplayMealType(recipe.mealType)})", // Используем функцию перевода
                style = MaterialTheme.typography.titleMedium
            )
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ингредиенты:", style = MaterialTheme.typography.bodyMedium)
                val ingredients = recipe.ingredients?.split(", ")?.filter { it.isNotEmpty() } ?: emptyList()
                ingredients.forEach { ingredient ->
                    Text("- $ingredient")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Инструкции:", style = MaterialTheme.typography.bodyMedium)
                Text(recipe.instructions ?: "Инструкции отсутствуют")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFamilyNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onNameUpdated: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать название семьи") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название семьи") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onNameUpdated(name)
                }
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