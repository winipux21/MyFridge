package ru.ngtu.myfridge.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn // Добавляем импорт для stateIn
import kotlinx.coroutines.launch
import ru.ngtu.myfridge.data.db.ShoppingItemEntity
import ru.ngtu.myfridge.data.repository.ShoppingListRepository

class ShoppingListViewModel(private val repository: ShoppingListRepository) : ViewModel() {
    val shoppingItems: StateFlow<List<ShoppingItemEntity>> = repository.allShoppingItems
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addShoppingItem(name: String, quantity: Int, unit: String) {
        val newItem = ShoppingItemEntity(
            name = name,
            quantity = quantity,
            unit = unit
        )
        viewModelScope.launch {
            repository.insertShoppingItem(newItem)
        }
    }

    fun updateShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.updateShoppingItem(item)
        }
    }

    fun deleteShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }
}