package ru.ngtu.myfridge.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "family_recipes",
    foreignKeys = [ForeignKey(
        entity = FamilyEntity::class,
        parentColumns = ["id"],
        childColumns = ["familyId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FamilyRecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyId: String? = null,
    val firebaseKey: String, // Уникальный ключ от Firebase
    val title: String? = null,
    val ingredients: String? = null,
    val instructions: String? = null,
    val mealType: String? = null,
    val recipeMode: String? = null
) {
    constructor() : this(0, null, "", null, null, null, null, null)
}