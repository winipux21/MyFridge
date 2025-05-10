package ru.ngtu.myfridge.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "family_products",
    foreignKeys = [ForeignKey(
        entity = FamilyEntity::class,
        parentColumns = ["id"],
        childColumns = ["familyId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FamilyProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyId: String,
    val firebaseKey: String, // Уникальный ключ от Firebase
    val name: String,
    val quantity: Int,
    val unit: String,
    val expiryDate: LocalDate?
) {
    constructor() : this(0, "", "", "", 0, "", null) // Конструктор для Firebase
}