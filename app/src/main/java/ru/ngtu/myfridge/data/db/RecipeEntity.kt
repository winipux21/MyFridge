package ru.ngtu.myfridge.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String? = null,
    val ingredients: String? = null,
    val instructions: String? = null,
    val mealType: String? = null,
    val recipeMode: String? = null
) {
    constructor() : this(0, null, null, null, null, null)
}