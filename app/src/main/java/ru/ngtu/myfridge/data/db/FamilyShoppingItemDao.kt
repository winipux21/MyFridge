package ru.ngtu.myfridge.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyShoppingItemDao {
    @Query("SELECT * FROM family_shopping_items WHERE familyId = :familyId")
    fun getFamilyShoppingItems(familyId: String): Flow<List<FamilyShoppingItemEntity>>

    @Insert
    suspend fun insertFamilyShoppingItem(item: FamilyShoppingItemEntity)

    @Query("DELETE FROM family_shopping_items WHERE familyId = :familyId")
    suspend fun deleteFamilyShoppingItems(familyId: String)

    @Query("DELETE FROM family_shopping_items WHERE familyId = :familyId AND id = :itemId")
    suspend fun deleteFamilyShoppingItem(familyId: String, itemId: Int)
}