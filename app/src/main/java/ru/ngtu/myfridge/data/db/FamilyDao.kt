package ru.ngtu.myfridge.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {

    @Query("SELECT * FROM families")
    fun getAllFamilies(): Flow<List<FamilyEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFamily(family: FamilyEntity): Long

    @Update
    suspend fun updateFamily(family: FamilyEntity)

    @Query("DELETE FROM families WHERE id = :familyId")
    suspend fun deleteFamily(familyId: String)

    // Новый метод для upsert
    suspend fun upsertFamily(family: FamilyEntity) {
        val id = insertFamily(family)
        if (id == -1L) { // Если вставка не удалась из-за конфликта, обновляем
            updateFamily(family)
        }
    }
}