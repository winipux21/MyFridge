package ru.ngtu.myfridge.data.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.ngtu.myfridge.data.db.ShoppingItemDao
import ru.ngtu.myfridge.data.db.ShoppingItemEntity

class ShoppingListRepository(private val shoppingItemDao: ShoppingItemDao) {
    val allShoppingItems: Flow<List<ShoppingItemEntity>> = shoppingItemDao.getAllItems()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            shoppingItemDao.getAllItems().collect { items ->
                Log.d("ShoppingListRepository", "Локальный список покупок обновлён: ${items.size}")
            }
        }
    }

    suspend fun insertShoppingItem(item: ShoppingItemEntity) {
        // Проверяем, существует ли элемент с таким именем
        val existingItems = shoppingItemDao.getAllItems().first()
        val existingItem = existingItems.find { it.name == item.name }
        if (existingItem != null) {
            // Если элемент уже существует, обновляем количество
            val updatedItem = existingItem.copy(
                quantity = existingItem.quantity + item.quantity
            )
            Log.d("ShoppingListRepository", "Обновление элемента: $updatedItem")
            shoppingItemDao.updateItem(updatedItem)
        } else {
            // Если элемента нет, добавляем новый
            Log.d("ShoppingListRepository", "Добавление нового элемента: $item")
            shoppingItemDao.insertItem(item)
        }
    }

    suspend fun updateShoppingItem(item: ShoppingItemEntity) {
        shoppingItemDao.updateItem(item)
        Log.d("ShoppingListRepository", "Элемент обновлён: $item")
    }

    suspend fun deleteShoppingItem(item: ShoppingItemEntity) {
        shoppingItemDao.deleteItem(item)
        Log.d("ShoppingListRepository", "Элемент удалён: $item")
    }

    suspend fun clearAll() {
        shoppingItemDao.clearAll()
        Log.d("ShoppingListRepository", "Список покупок очищен")
    }
}