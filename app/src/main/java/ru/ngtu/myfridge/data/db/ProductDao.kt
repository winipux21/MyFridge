package ru.ngtu.myfridge.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY expiryDate ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products")
    suspend fun clearAll()
}