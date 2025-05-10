package ru.ngtu.myfridge.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.ngtu.myfridge.data.db.ProductEntity
import ru.ngtu.myfridge.data.db.RecipeEntity
import ru.ngtu.myfridge.data.network.DeepSeekRecipes
import ru.ngtu.myfridge.data.repository.FirebaseSync
import ru.ngtu.myfridge.presentation.ProductViewModel

// Перечисление MealType с русскими названиями
enum class MealType(val russianName: String) {
    BREAKFAST("Завтрак"),
    LUNCH("Обед"),
    DINNER("Ужин")
}

// Перечисление RecipeMode с русскими названиями
enum class RecipeMode(val russianName: String) {
    REGULAR("Обычный"),
    HEALTHY("ПП")
}

data class Recipe(val title: String, val ingredients: List<String>, val instructions: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(viewModel: ProductViewModel) {
    val scope = rememberCoroutineScope()
    val products by viewModel.availableProducts.collectAsState()
    var mealType by remember { mutableStateOf(MealType.BREAKFAST) }
    var recipeMode by remember { mutableStateOf(RecipeMode.REGULAR) }
    var numberOfRecipes by remember { mutableStateOf(1) }
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var families by remember { mutableStateOf<List<Family>>(emptyList()) }
    var showShareDialog by remember { mutableStateOf<Recipe?>(null) }
    val context = LocalContext.current // Добавляем контекст для Toast

    LaunchedEffect(Unit) {
        FirebaseSync.getFamiliesForUser { families = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(13.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Доступные продукты", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        if (products.isEmpty()) {
            Text("В холодильнике нет продуктов с действующим сроком годности.")
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                items(products.size) { index ->
                    val product = products[index]
                    Text("${product.name}: ${product.quantity} ${product.unit} (до ${product.expiryDate})")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Тип блюда", style = MaterialTheme.typography.titleMedium)
        Row {
            MealType.values().forEach { type ->
                Row(Modifier.padding(end = 8.dp)) {
                    RadioButton(selected = mealType == type, onClick = { mealType = type })
                    Text(type.russianName) // Используем русское название
                }
            }
        }

        Text("Режим рецепта", style = MaterialTheme.typography.titleMedium)
        Row {
            RecipeMode.values().forEach { mode ->
                Row(Modifier.padding(end = 8.dp)) {
                    RadioButton(selected = recipeMode == mode, onClick = { recipeMode = mode })
                    Text(mode.russianName) // Используем русское название
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Количество рецептов", style = MaterialTheme.typography.titleMedium)
        Row {
            listOf(1, 2, 3).forEach { num ->
                Row(Modifier.padding(end = 8.dp)) {
                    RadioButton(selected = numberOfRecipes == num, onClick = { numberOfRecipes = num })
                    Text(num.toString())
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (products.isNotEmpty()) {
                    scope.launch {
                        isLoading = true
                        recipes = DeepSeekRecipes.fetchRecipes(products, mealType, recipeMode, numberOfRecipes)
                        isLoading = false
                    }
                }
            },
            enabled = products.isNotEmpty() && !isLoading
        ) {
            Text(if (isLoading) "Генерация..." else "Сгенерировать рецепты")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (recipes.isNotEmpty()) {
            Text("Рецепты", style = MaterialTheme.typography.titleLarge)
            recipes.forEach { recipe ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(recipe.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ингредиенты:", style = MaterialTheme.typography.bodyMedium)
                        recipe.ingredients.forEach { ingredient -> Text("- $ingredient") }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Инструкции:", style = MaterialTheme.typography.bodyMedium)
                        Text(recipe.instructions)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                scope.launch {
                                    viewModel.saveRecipe(
                                        title = recipe.title,
                                        ingredients = recipe.ingredients,
                                        instructions = recipe.instructions,
                                        mealType = mealType.russianName, // Русское название
                                        recipeMode = recipeMode.russianName // Русское название
                                    )
                                    // Уведомляем пользователя о сохранении
                                    Toast.makeText(context, "Рецепт \"${recipe.title}\" сохранён", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("Сохранить")
                            }
                            Button(onClick = { showShareDialog = recipe }) {
                                Text("Поделиться")
                            }
                        }
                    }
                }
            }
        }

        showShareDialog?.let { recipe ->
            AlertDialog(
                onDismissRequest = { showShareDialog = null },
                title = { Text("Поделиться рецептом: ${recipe.title}") },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(families.size) { index ->
                            val family = families[index]
                            Button(onClick = {
                                val recipeEntity = RecipeEntity(
                                    title = recipe.title,
                                    ingredients = recipe.ingredients.joinToString(", "),
                                    instructions = recipe.instructions,
                                    mealType = mealType.russianName, // Русское название
                                    recipeMode = recipeMode.russianName // Русское название
                                )
                                FirebaseSync.syncRecipe(recipeEntity, family.id)
                                showShareDialog = null
                                // Уведомляем о том, что рецепт поделён
                                Toast.makeText(context, "Рецепт \"${recipe.title}\" поделён с семьёй ${family.name}", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Семья: ${family.name}")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showShareDialog = null }) {
                        Text("Закрыть")
                    }
                }
            )
        }
    }
}