package ru.ngtu.myfridge.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "family_shopping_items",
    foreignKeys = [ForeignKey(
        entity = FamilyEntity::class,
        parentColumns = ["id"],
        childColumns = ["familyId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FamilyShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyId: String,
    val firebaseKey: String, // Уникальный ключ от Firebase
    val name: String,
    val quantity: Int,
    val unit: String,
    val isPurchased: Boolean = false
) {
    constructor() : this(0, "", "", "", 0, "", false)
}