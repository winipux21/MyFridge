package ru.ngtu.myfridge.data.db

import android.content.Context
import androidx.room.*
import ru.ngtu.myfridge.utils.LocalDateConverter

@Database(
    entities = [
        ProductEntity::class,
        ShoppingItemEntity::class,
        RecipeEntity::class,
        FamilyEntity::class,
        FamilyProductEntity::class,
        FamilyRecipeEntity::class,
        FamilyShoppingItemEntity::class
    ],
    version = 10, // Увеличили версию с 9 до 10
    exportSchema = false
)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun recipeDao(): RecipeDao
    abstract fun familyDao(): FamilyDao
    abstract fun familyProductDao(): FamilyProductDao
    abstract fun familyRecipeDao(): FamilyRecipeDao
    abstract fun familyShoppingItemDao(): FamilyShoppingItemDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_fridge_database"
                )
                    .fallbackToDestructiveMigration() // Используем для упрощённой миграции
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}