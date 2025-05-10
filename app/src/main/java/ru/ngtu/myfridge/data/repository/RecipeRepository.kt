package ru.ngtu.myfridge.data.repository

import kotlinx.coroutines.flow.Flow
import ru.ngtu.myfridge.data.db.RecipeDao
import ru.ngtu.myfridge.data.db.RecipeEntity

class RecipeRepository(private val recipeDao: RecipeDao) {
    val allRecipes: Flow<List<RecipeEntity>> = recipeDao.getAllRecipes()

    suspend fun insertRecipe(recipe: RecipeEntity) {
        recipeDao.insertRecipe(recipe)
    }

    suspend fun deleteRecipe(id: Int) {
        recipeDao.deleteRecipe(id)
    }
}