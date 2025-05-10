package ru.ngtu.myfridge.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "families")
data class FamilyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val creatorId: String
)