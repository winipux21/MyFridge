package ru.ngtu.myfridge.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.ngtu.myfridge.data.db.FamilyProductDao
import ru.ngtu.myfridge.data.db.FamilyRecipeDao
import ru.ngtu.myfridge.data.repository.FamilyRepository
import ru.ngtu.myfridge.data.repository.ProductRepository
import ru.ngtu.myfridge.data.repository.RecipeRepository

class ProductViewModelFactory(
    private val productRepository: ProductRepository,
    private val recipeRepository: RecipeRepository,
    private val familyRepository: FamilyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(productRepository, recipeRepository, familyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}