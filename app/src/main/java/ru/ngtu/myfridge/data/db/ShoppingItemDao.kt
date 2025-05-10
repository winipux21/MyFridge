package ru.ngtu.myfridge.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {

    @Query("SELECT * FROM shopping_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity)

    @Delete
    suspend fun deleteItem(item: ShoppingItemEntity)

    @Update
    suspend fun updateItem(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items")
    suspend fun clearAll()
}