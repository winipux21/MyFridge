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

object OpenAIRecipes {
    private const val baseUrl = "https://api.openai.com/v1/chat/completions"
    private val client = OkHttpClient()

    suspend fun fetchRecipes(
        products: List<ProductEntity>,
        mealType: MealType,
        recipeMode: RecipeMode,
        numberOfRecipes: Int
    ): List<Recipe> {
        return withContext(Dispatchers.IO) {
            try {
                // Формируем список продуктов для промпта
                val productList = products.joinToString(", ") { "${it.name} (${it.quantity} ${it.unit})" }

                // Универсальный промпт
                val mealTypeText = when (mealType) {
                    MealType.BREAKFAST -> "breakfast"
                    MealType.LUNCH -> "lunch"
                    MealType.DINNER -> "dinner"
                }
                val modeText = when (recipeMode) {
                    RecipeMode.REGULAR -> "regular"
                    RecipeMode.HEALTHY -> "healthy (low-calorie, balanced, suitable for a healthy diet)"
                }
                val prompt = """
                    I have the following ingredients in my fridge: $productList.
                    Suggest $numberOfRecipes $modeText recipes for $mealTypeText. For each recipe, provide:
                    - A title
                    - A list of ingredients with quantities
                    - Step-by-step cooking instructions
                    Use only English for all text, including ingredient names.
                    Format the response as:
                    Recipe 1: [Title]
                    Ingredients:
                    - [Ingredient 1 with quantity]
                    - [Ingredient 2 with quantity]
                    Instructions:
                    1. [Step 1]
                    2. [Step 2]
                """.trimIndent()

                // Формируем JSON-запрос
                val jsonObject = JSONObject().apply {
                    put("model", "gpt-4o")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "You are a helpful assistant that provides recipes based on available ingredients.")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    })
                    put("max_tokens", 1000)
                }

                val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(baseUrl)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer ${ApiKeys.OPENAI_API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: return@withContext emptyList()
                    Log.d("OpenAIRecipes", "Raw response: $responseBody")
                    val json = JSONObject(responseBody)
                    val content = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    // Парсим ответ в рецепты
                    return@withContext parseRecipes(content, numberOfRecipes)
                } else {
                    Log.e("OpenAIRecipes", "Response code: ${response.code}, message: ${response.message}")
                    return@withContext emptyList()
                }
            } catch (e: Exception) {
                Log.e("OpenAIRecipes", "Exception: ${e.message}", e)
                return@withContext emptyList()
            }
        }
    }

    private fun parseRecipes(content: String, numberOfRecipes: Int): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        val lines = content.lines()

        var currentIndex = 0
        while (currentIndex < lines.size) {
            // Ищем начало рецепта
            val titleLineIndex = lines.subList(currentIndex, lines.size)
                .indexOfFirst { it.trim().matches(Regex("Recipe \\d+:.*")) }
            if (titleLineIndex == -1) break

            currentIndex += titleLineIndex
            val titleLine = lines[currentIndex]
            val title = titleLine.substringAfter(":").trim()

            // Ищем начало следующего рецепта, чтобы определить конец текущего
            val nextRecipeIndex = lines.subList(currentIndex + 1, lines.size)
                .indexOfFirst { it.trim().matches(Regex("Recipe \\d+:.*")) }
            val endIndex = if (nextRecipeIndex != -1) {
                currentIndex + 1 + nextRecipeIndex
            } else {
                lines.size
            }

            // Извлекаем блок текущего рецепта
            val recipeLines = lines.subList(currentIndex, endIndex)

            // Ищем ингредиенты
            val ingredientsStartIndex = recipeLines.indexOfFirst { it.trim() == "Ingredients:" }
            val instructionsStartIndex = recipeLines.indexOfFirst { it.trim() == "Instructions:" }
            val ingredients = if (ingredientsStartIndex != -1 && instructionsStartIndex != -1 && instructionsStartIndex > ingredientsStartIndex) {
                recipeLines.subList(ingredientsStartIndex + 1, instructionsStartIndex)
                    .filter { it.trim().startsWith("-") }
                    .map { it.trim().removePrefix("-").trim() }
                    .filter { it.isNotEmpty() }
            } else {
                emptyList()
            }

            // Ищем инструкции
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