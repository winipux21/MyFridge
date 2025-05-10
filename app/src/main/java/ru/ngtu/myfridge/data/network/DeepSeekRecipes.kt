package ru.ngtu.myfridge.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import ru.ngtu.myfridge.data.ApiKeys
import ru.ngtu.myfridge.data.db.ProductEntity
import ru.ngtu.myfridge.presentation.screens.MealType
import ru.ngtu.myfridge.presentation.screens.Recipe
import ru.ngtu.myfridge.presentation.screens.RecipeMode
import java.util.concurrent.TimeUnit

object DeepSeekRecipes {
    private const val baseUrl = "https://api.deepseek.com/chat/completions"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    suspend fun fetchRecipes(
        products: List<ProductEntity>,
        mealType: MealType,
        recipeMode: RecipeMode,
        numberOfRecipes: Int
    ): List<Recipe> {
        return withContext(Dispatchers.IO) {
            try {
                val productList = products.joinToString(", ") { "${it.name} (${it.quantity} ${it.unit})" }

                // Используем русские названия из перечислений
                val mealTypeText = mealType.russianName.lowercase()
                val modeText = recipeMode.russianName.lowercase()
                val prompt = """
                    У меня есть следующие продукты в холодильнике: $productList.
                    Предложи $numberOfRecipes $modeText рецептов для $mealTypeText. Для каждого рецепта укажи:
                    - Название
                    - Список ингредиентов с количеством
                    - Пошаговые инструкции по приготовлению
                    Все тексты, включая названия рецептов, ингредиенты и инструкции, должны быть на русском языке.
                    ОБЯЗАТЕЛЬНО Форматируй ответ следующим образом:
                    Рецепт 1: [Название]
                    Ингредиенты:
                    - [Ингредиент 1 с количеством]
                    - [Ингредиент 2 с количеством]
                    - [Ингредиент 3 с количеством]
                    Инструкции:
                    1. [Шаг 1]
                    2. [Шаг 2]
                    3. [Шаг 3]
                """.trimIndent()

                val jsonObject = JSONObject().apply {
                    put("model", "deepseek-chat")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "Ты полезный помощник, который предлагает рецепты на основе доступных ингредиентов.")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    })
                    put("max_tokens", 2000)
                    put("temperature", 1.3)
                }

                val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(baseUrl)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer ${ApiKeys.DEEPSEEK_API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .build()

                Log.d("DeepSeekRecipes", "Отправка запроса: ${jsonObject.toString()}")

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: return@withContext emptyList()
                    Log.d("DeepSeekRecipes", "Raw response: $responseBody")
                    val json = JSONObject(responseBody)
                    val content = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    return@withContext parseRecipes(content, numberOfRecipes)
                } else {
                    Log.e("DeepSeekRecipes", "Response code: ${response.code}, message: ${response.message}")
                    return@withContext emptyList()
                }
            } catch (e: Exception) {
                Log.e("DeepSeekRecipes", "Exception: ${e.message}", e)
                return@withContext emptyList()
            }
        }
    }

    private fun parseRecipes(content: String, numberOfRecipes: Int): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        val lines = content.lines()

        var currentIndex = 0
        while (currentIndex < lines.size) {
            val titleLineIndex = lines.subList(currentIndex, lines.size)
                .indexOfFirst { it.trim().matches(Regex("Рецепт \\d+:.*")) }
            if (titleLineIndex == -1) break

            currentIndex += titleLineIndex
            val titleLine = lines[currentIndex]
            val title = titleLine.substringAfter(":").trim()

            val nextRecipeIndex = lines.subList(currentIndex + 1, lines.size)
                .indexOfFirst { it.trim().matches(Regex("Рецепт \\d+:.*")) }
            val endIndex = if (nextRecipeIndex != -1) {
                currentIndex + 1 + nextRecipeIndex
            } else {
                lines.size
            }

            val recipeLines = lines.subList(currentIndex, endIndex)

            val ingredientsStartIndex = recipeLines.indexOfFirst { it.trim() == "Ингредиенты:" }
            val instructionsStartIndex = recipeLines.indexOfFirst { it.trim() == "Инструкции:" }
            val ingredients = if (ingredientsStartIndex != -1 && instructionsStartIndex != -1 && instructionsStartIndex > ingredientsStartIndex) {
                recipeLines.subList(ingredientsStartIndex + 1, instructionsStartIndex)
                    .filter { it.trim().startsWith("-") }
                    .map { it.trim().removePrefix("-").trim() }
                    .filter { it.isNotEmpty() }
            } else {
                emptyList()
            }

            val instructions = if (instructionsStartIndex != -1) {
                recipeLines.subList(instructionsStartIndex + 1, recipeLines.size)
                    .filter { it.trim().matches(Regex("\\d+\\..*")) || it.trim().isNotEmpty() }
                    .joinToString("\n")
                    .trim()
            } else {
                ""
            }

            recipes.add(Recipe(title, ingredients, instructions))
            currentIndex = endIndex
        }

        return recipes.take(numberOfRecipes)
    }
}