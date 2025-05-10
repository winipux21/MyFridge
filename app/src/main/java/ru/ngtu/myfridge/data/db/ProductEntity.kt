package ru.ngtu.myfridge.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String? = null,
    val quantity: Int? = null,
    val unit: String? = null,
    val expiryDate: LocalDate? = null
) {
    // Конструктор без аргументов для Firebase
    constructor() : this(0, null, null, null, null)
}