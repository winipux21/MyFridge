package ru.ngtu.myfridge.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: Int)
}