package ru.ngtu.myfridge.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import ru.ngtu.myfridge.data.db.RecipeEntity
import ru.ngtu.myfridge.presentation.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecipesScreen(
    viewModel: ProductViewModel,
    onBack: () -> Unit
) {
    val recipes by viewModel.savedRecipes.collectAsState(initial = emptyList())

    // Перехват системной кнопки "Назад"
    BackHandler(enabled = true) {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои рецепты") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("У вас пока нет сохранённых рецептов.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp)) // Добавляем отступ 12.dp сверху
                }
                items(recipes.size) { index ->
                    val recipe = recipes[index]
                    RecipeItem(recipe = recipe)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun RecipeItem(recipe: RecipeEntity) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = recipe.title ?: "Без названия",
                style = MaterialTheme.typography.titleMedium
            )
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ингредиенты:", style = MaterialTheme.typography.bodyMedium)
                val ingredients = recipe.ingredients?.split(", ")?.filter { it.isNotEmpty() } ?: emptyList()
                if (ingredients.isEmpty()) {
                    Text("Ингредиенты отсутствуют")
                } else {
                    ingredients.forEach { ingredient ->
                        Text("- $ingredient")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Инструкции:", style = MaterialTheme.typography.bodyMedium)
                Text(recipe.instructions ?: "Инструкции отсутствуют")
            }
        }
    }
}