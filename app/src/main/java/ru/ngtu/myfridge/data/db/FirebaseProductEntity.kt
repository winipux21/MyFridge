package ru.ngtu.myfridge.data.db

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class FirebaseProductEntity(
    val name: String? = null,
    val quantity: Int? = null,
    val unit: String? = null,
    val expiryDate: String? = null // Храним дату как строку
) {
    constructor() : this(null, null, null, null)

    fun toProductEntity(): ProductEntity {
        return ProductEntity(
            id = 0, // Локальный id не используется для семейных данных
            name = name,
            quantity = quantity,
            unit = unit,
            expiryDate = expiryDate?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
        )
    }

    companion object {
        fun fromProductEntity(product: ProductEntity): FirebaseProductEntity {
            return FirebaseProductEntity(
                name = product.name,
                quantity = product.quantity,
                unit = product.unit,
                expiryDate = product.expiryDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
            )
        }
    }
}