package ru.ngtu.myfridge.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyProductDao {
    @Query("SELECT * FROM family_products WHERE familyId = :familyId")
    fun getFamilyProducts(familyId: String): Flow<List<FamilyProductEntity>>

    @Insert
    suspend fun insertFamilyProduct(product: FamilyProductEntity)

    @Query("DELETE FROM family_products WHERE familyId = :familyId")
    suspend fun deleteFamilyProducts(familyId: String)

    @Query("DELETE FROM family_products WHERE familyId = :familyId AND id = :productId")
    suspend fun deleteFamilyProduct(familyId: String, productId: Int)
}