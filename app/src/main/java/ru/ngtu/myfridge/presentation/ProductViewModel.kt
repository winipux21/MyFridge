package ru.ngtu.myfridge.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.ngtu.myfridge.data.db.*
import ru.ngtu.myfridge.data.repository.FamilyRepository
import ru.ngtu.myfridge.data.repository.ProductRepository
import ru.ngtu.myfridge.data.repository.RecipeRepository
import java.time.LocalDate

/**
 * ViewModel‑логика холодильника.
 * Все названия продуктов нормализуются:
 *  – убираются двойные пробелы/хвостовые знаки пунктуации
 *  – первый символ делается заглавным
 * Благодаря этому «Яблоко» и «яблоко» считаются одним продуктом.
 */
class ProductViewModel(
    private val productRepository: ProductRepository,
    private val recipeRepository: RecipeRepository,
    private val familyRepository: FamilyRepository
) : ViewModel() {

    /* ---------------------------------------------------------------------------
       Переменные‑потоки
       --------------------------------------------------------------------------- */
    val products: StateFlow<List<ProductEntity>> = productRepository.allProducts
        .map { list ->
            Log.d("ProductViewModel", "Обновление products: ${list.size} элементов")
            list
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val availableProducts: StateFlow<List<ProductEntity>> = products
        .map { list ->
            val filtered = list.filter { it.expiryDate?.isAfter(LocalDate.now().minusDays(1)) ?: true }
            Log.d("ProductViewModel", "Обновление availableProducts: ${filtered.size} элементов")
            filtered
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val savedRecipes: StateFlow<List<RecipeEntity>> =
        recipeRepository.allRecipes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allFamilies: StateFlow<List<FamilyEntity>> = familyRepository.allFamilies

    /* ---------------------------------------------------------------------------
       Вспомогательные функции
       --------------------------------------------------------------------------- */

    /** Делает первую букву заглавной и убирает лишние пробелы/знаки препинания. */
    private fun normalizeName(raw: String): String =
        raw.trim()
            .trimEnd(',', '.', ';')
            .replace("""\s{2,}""".toRegex(), " ")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    /* ---------------------------------------------------------------------------
       Добавление продукта вручную
       --------------------------------------------------------------------------- */
    fun addProduct(name: String, quantity: Int, unit: String, expiryDate: LocalDate) {
        val entity = ProductEntity(
            name = normalizeName(name),
            quantity = quantity,
            unit = unit,
            expiryDate = expiryDate
        )
        viewModelScope.launch {
            productRepository.insertProduct(entity)
            Log.d("ProductViewModel", "Продукт добавлен: $entity")
        }
    }

    /* ---------------------------------------------------------------------------
       Парсинг ответа ИИ
       --------------------------------------------------------------------------- */
    fun parseProductsFromAnalysis(lines: List<String>): List<ProductEntity> {
        val qtyRegex = Regex(
            pattern = """(\d+(?:[.,]\d+)?)\s*(шт|штук|шт\.?|kg|кг|g|гр|г|l|л|ml|мл)?$""",
            option = RegexOption.IGNORE_CASE
        )
        val daysRegex = Regex("""(\d+)""")

        return lines.mapNotNull { raw ->
            val parts = raw.split(" СГ ", ignoreCase = true, limit = 2)
            if (parts.size != 2) return@mapNotNull null

            /* --------- срок годности --------- */
            val days = daysRegex.find(parts[1])?.groupValues?.get(1)?.toLongOrNull() ?: 1L

            /* --------- название / кол-во / ед. --------- */
            val nameAndQty = parts[0].trim()
            val qtyMatch = qtyRegex.find(nameAndQty)

            val quantity: Int
            val unit: String
            val cleanName: String

            if (qtyMatch != null) {
                quantity = qtyMatch.groupValues[1].replace(",", ".").toDouble().toInt().coerceAtLeast(1)
                unit = when (qtyMatch.groupValues[2].lowercase()) {
                    "kg", "кг"        -> "кг"
                    "g",  "гр", "г"   -> "г"
                    "l",  "л"         -> "л"
                    "ml", "мл"        -> "мл"
                    else              -> "шт"
                }
                cleanName = nameAndQty.removeRange(qtyMatch.range)
            } else {
                quantity = 1
                unit = "шт"
                cleanName = nameAndQty
            }

            val finalName = normalizeName(cleanName)

            if (finalName.isNotBlank()) {
                ProductEntity(
                    name = finalName,
                    quantity = quantity,
                    unit = unit,
                    expiryDate = LocalDate.now().plusDays(days)
                )
            } else null
        }
    }

    /* ---------------------------------------------------------------------------
       Массовое добавление после парсинга
       --------------------------------------------------------------------------- */
    fun addProducts(list: List<ProductEntity>) {
        viewModelScope.launch {
            list.forEach { product ->
                productRepository.insertProduct(
                    product.copy(name = normalizeName(product.name ?: ""))
                )
            }
            Log.d("ProductViewModel", "Добавлено ${list.size} продуктов")
        }
    }

    /* ---------------------------------------------------------------------------
       Обычные CRUD‑операции
       --------------------------------------------------------------------------- */
    fun updateProduct(product: ProductEntity) = viewModelScope.launch {
        productRepository.updateProduct(product.copy(name = normalizeName(product.name ?: "")))
    }

    fun deleteProduct(product: ProductEntity) = viewModelScope.launch {
        productRepository.deleteProduct(product)
    }

    /* ---------------------------------------------------------------------------
       Семейный функционал (логика прежняя)
       --------------------------------------------------------------------------- */
    fun deleteFamilyProduct(familyId: String, product: ProductEntity, currentUserId: String) =
        viewModelScope.launch { familyRepository.deleteFamilyProduct(familyId, product, currentUserId) }

    fun deleteFamilyRecipe(familyId: String, recipe: RecipeEntity, currentUserId: String) =
        viewModelScope.launch { familyRepository.deleteFamilyRecipe(familyId, recipe, currentUserId) }

    fun deleteFamilyShoppingItem(familyId: String, item: ShoppingItemEntity, currentUserId: String) =
        viewModelScope.launch { familyRepository.deleteFamilyShoppingItem(familyId, item, currentUserId) }

    fun saveRecipe(
        title: String,
        ingredients: List<String>,
        instructions: String,
        mealType: String,
        recipeMode: String
    ) {
        val entity = RecipeEntity(
            title = title,
            ingredients = ingredients.joinToString(", "),
            instructions = instructions,
            mealType = mealType,
            recipeMode = recipeMode
        )
        viewModelScope.launch { recipeRepository.insertRecipe(entity) }
    }

    fun createFamily(name: String, creatorId: String): String {
        var id = ""
        viewModelScope.launch { id = familyRepository.createFamily(name, creatorId) }
        return id
    }

    suspend fun joinFamily(familyId: String): Boolean = familyRepository.joinFamily(familyId)

    fun deleteFamily(familyId: String, currentUserId: String): Boolean {
        var ok = false
        viewModelScope.launch { ok = familyRepository.deleteFamily(familyId, currentUserId) }
        return ok
    }

    fun updateFamilyName(familyId: String, newName: String) =
        viewModelScope.launch { familyRepository.updateFamilyName(familyId, newName) }

    fun getFamilyProducts(familyId: String): StateFlow<List<FamilyProductEntity>> =
        familyRepository.getFamilyProducts(familyId)

    fun getFamilyRecipes(familyId: String): StateFlow<List<FamilyRecipeEntity>> =
        familyRepository.getFamilyRecipes(familyId)

    fun getFamilyShoppingItems(familyId: String): StateFlow<List<FamilyShoppingItemEntity>> =
        familyRepository.getFamilyShoppingItems(familyId)

    fun shareProducts(familyId: String, products: List<ProductEntity>) =
        viewModelScope.launch { familyRepository.shareProducts(familyId, products) }

    fun shareRecipe(familyId: String, recipe: RecipeEntity) =
        viewModelScope.launch { familyRepository.shareRecipe(familyId, recipe) }

    fun shareShoppingItems(familyId: String, items: List<ShoppingItemEntity>) =
        viewModelScope.launch { familyRepository.shareShoppingItems(familyId, items) }

    fun removeFamilyListeners() = familyRepository.removeListeners()
}
