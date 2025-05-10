package ru.ngtu.myfridge.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val quantity: Int,
    val unit: String,
    val isPurchased: Boolean = false
) {
    // Добавляем конструктор без аргументов для Firebase
    constructor() : this(0, "", 0, "", false)
}