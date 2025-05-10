package ru.ngtu.myfridge.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.ngtu.myfridge.data.db.ProductDao
import ru.ngtu.myfridge.data.db.ProductEntity
import android.util.Log

class ProductRepository(private val productDao: ProductDao) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Удаляем автоматическую синхронизацию с семейным холодильником
        // Если нужно синхронизировать локальный холодильник с семейным при старте,
        // это должно быть явным действием пользователя
    }

    suspend fun insertProduct(product: ProductEntity) {
        val existingProducts = productDao.getAllProducts().first()
        val existingProduct = existingProducts.find {
            it.name == product.name &&
                    it.unit == product.unit &&
                    it.expiryDate == product.expiryDate
        }
        if (existingProduct != null) {
            val updatedProduct = existingProduct.copy(
                quantity = (existingProduct.quantity ?: 0) + (product.quantity ?: 0)
            )
            Log.d("ProductRepository", "Обновление продукта: $updatedProduct")
            productDao.updateProduct(updatedProduct)
        } else {
            Log.d("ProductRepository", "Добавление нового продукта: $product")
            productDao.insertProduct(product)
        }
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    suspend fun clearAll() {
        productDao.clearAll()
    }
}