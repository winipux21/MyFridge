package ru.ngtu.myfridge.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyRecipeDao {
    @Query("SELECT * FROM family_recipes WHERE familyId = :familyId")
    fun getFamilyRecipes(familyId: String): Flow<List<FamilyRecipeEntity>>

    @Insert
    suspend fun insertFamilyRecipe(recipe: FamilyRecipeEntity)

    @Query("DELETE FROM family_recipes WHERE familyId = :familyId")
    suspend fun deleteFamilyRecipes(familyId: String)

    @Query("DELETE FROM family_recipes WHERE familyId = :familyId AND id = :recipeId")
    suspend fun deleteFamilyRecipe(familyId: String, recipeId: Int)
}