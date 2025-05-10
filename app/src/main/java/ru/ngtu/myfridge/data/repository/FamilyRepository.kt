package ru.ngtu.myfridge.data.repository

import android.util.Log
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.ngtu.myfridge.data.db.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Random
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FamilyRepository(
    private val familyDao: FamilyDao,
    private val familyProductDao: FamilyProductDao,
    private val familyRecipeDao: FamilyRecipeDao,
    private val familyShoppingItemDao: FamilyShoppingItemDao
) {
    private val _allFamilies = MutableStateFlow<List<FamilyEntity>>(emptyList())
    val allFamilies: StateFlow<List<FamilyEntity>> = _allFamilies.asStateFlow()

    private val _familyProducts = MutableStateFlow<Map<String, List<FamilyProductEntity>>>(emptyMap())
    val familyProducts: StateFlow<Map<String, List<FamilyProductEntity>>> = _familyProducts.asStateFlow()

    private val _familyRecipes = MutableStateFlow<Map<String, List<FamilyRecipeEntity>>>(emptyMap())
    val familyRecipes: StateFlow<Map<String, List<FamilyRecipeEntity>>> = _familyRecipes.asStateFlow()

    private val _familyShoppingItems = MutableStateFlow<Map<String, List<FamilyShoppingItemEntity>>>(emptyMap())
    val familyShoppingItems: StateFlow<Map<String, List<FamilyShoppingItemEntity>>> = _familyShoppingItems.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private val productListeners = mutableMapOf<String, ValueEventListener>()
    private val recipeListeners = mutableMapOf<String, ValueEventListener>()
    private val shoppingItemListeners = mutableMapOf<String, ValueEventListener>()
    private var familyListener: ValueEventListener? = null

    init {
        scope.launch {
            familyDao.getAllFamilies().collect { families ->
                _allFamilies.value = families
                Log.d("FamilyRepository", "Локальные семьи обновлены: ${families.size}")
            }
        }

        familyListener = FirebaseSync.getFamiliesForUserRealtime { families ->
            scope.launch {
                val familyEntities = families.map { family ->
                    FamilyEntity(
                        id = family.id,
                        name = family.name,
                        creatorId = family.creator ?: "unknown"
                    )
                }
                familyEntities.forEach { familyDao.upsertFamily(it) }
                Log.d("FamilyRepository", "Синхронизация семей из Firebase: ${familyEntities.size}")

                val firebaseFamilyIds = families.map { it.id }.toSet()
                val localFamilies = familyDao.getAllFamilies().first()
                localFamilies.filter { it.id !in firebaseFamilyIds }
                    .forEach { familyDao.deleteFamily(it.id) }

                families.forEach { syncFamilyData(it.id) }
            }
        }
        scope.launch {
            Runtime.getRuntime().addShutdownHook(Thread {
                familyListener?.let { FirebaseSync.removeFamiliesListener(it) }
            })
        }
    }

    fun removeListeners() {
        familyListener?.let {
            FirebaseSync.removeFamiliesListener(it)
            familyListener = null
            Log.d("FamilyRepository", "Удалён слушатель семей")
        }
        productListeners.forEach { (familyId, listener) ->
            FirebaseSync.removeProductsListener(familyId, listener)
            Log.d("FamilyRepository", "Удалён слушатель продуктов для семьи $familyId")
        }
        recipeListeners.forEach { (familyId, listener) ->
            FirebaseSync.removeRecipesListener(familyId, listener)
            Log.d("FamilyRepository", "Удалён слушатель рецептов для семьи $familyId")
        }
        shoppingItemListeners.forEach { (familyId, listener) ->
            FirebaseSync.removeShoppingItemsListener(familyId, listener)
            Log.d("FamilyRepository", "Удалён слушатель списка покупок для семьи $familyId")
        }
        productListeners.clear()
        recipeListeners.clear()
        shoppingItemListeners.clear()
    }

    private suspend fun syncFamilyData(familyId: String) {
        getFamilyProducts(familyId)
        getFamilyRecipes(familyId)
        getFamilyShoppingItems(familyId)
    }

    suspend fun createFamily(name: String, creatorId: String): String = suspendCancellableCoroutine { continuation ->
        val familyId = generateShortId()
        val family = FamilyEntity(id = familyId, name = name, creatorId = creatorId)
        Log.d("FamilyRepository", "Создание семьи: id=$familyId, name=$name, creatorId=$creatorId")

        scope.launch {
            familyDao.insertFamily(family)
        }

        FirebaseSync.createFamily(name, familyId) { result, firebaseFamilyId ->
            if (result && firebaseFamilyId != null) {
                continuation.resume(familyId)
            } else {
                scope.launch {
                    familyDao.deleteFamily(familyId)
                    Log.e("FamilyRepository", "Ошибка создания в Firebase, удаляем локально: $familyId")
                }
                continuation.resumeWithException(Exception("Failed to create family in Firebase"))
            }
        }
    }

    private fun generateShortId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        return (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }

    suspend fun joinFamily(familyId: String): Boolean {
        val families = familyDao.getAllFamilies().first()
        if (families.any { it.id == familyId }) {
            Log.d("FamilyRepository", "Семья $familyId уже существует локально")
            return true
        }

        val success = FirebaseSync.joinFamilySuspend(familyId)
        if (success) {
            val updatedFamilies = FirebaseSync.getFamiliesForUserSuspend()
            val family = updatedFamilies.find { it.id == familyId }
            if (family != null) {
                val familyEntity = FamilyEntity(
                    id = family.id,
                    name = family.name,
                    creatorId = family.creator ?: "unknown"
                )
                familyDao.upsertFamily(familyEntity)
                syncFamilyData(familyId)
                Log.d("FamilyRepository", "Присоединились к семье: id=$familyId")
                return true
            }
        }
        Log.e("FamilyRepository", "Ошибка присоединения к семье: $familyId")
        return false
    }

    suspend fun deleteFamily(familyId: String, currentUserId: String): Boolean {
        val family = familyDao.getAllFamilies().first().find { it.id == familyId }
            ?: return false.also { Log.w("FamilyRepository", "Семья $familyId не найдена") }

        if (family.creatorId != currentUserId) {
            Log.w("FamilyRepository", "Удаление отклонено: $currentUserId не создатель")
            return false
        }

        familyProductDao.deleteFamilyProducts(familyId)
        familyRecipeDao.deleteFamilyRecipes(familyId)
        familyShoppingItemDao.deleteFamilyShoppingItems(familyId)
        familyDao.deleteFamily(familyId)
        Log.d("FamilyRepository", "Семья удалена локально: $familyId")

        productListeners[familyId]?.let { FirebaseSync.removeProductsListener(familyId, it) }
        recipeListeners[familyId]?.let { FirebaseSync.removeRecipesListener(familyId, it) }
        shoppingItemListeners[familyId]?.let { FirebaseSync.removeShoppingItemsListener(familyId, it) }
        productListeners.remove(familyId)
        recipeListeners.remove(familyId)
        shoppingItemListeners.remove(familyId)

        var success = false
        FirebaseSync.deleteFamily(familyId) { result ->
            success = result
            if (!result) Log.e("FamilyRepository", "Ошибка удаления в Firebase: $familyId")
        }
        return success
    }

    suspend fun deleteFamilyProduct(familyId: String, product: ProductEntity, currentUserId: String): Boolean {
        val family = familyDao.getAllFamilies().first().find { it.id == familyId }
            ?: return false.also { Log.w("FamilyRepository", "Семья $familyId не найдена") }

        if (family.creatorId != currentUserId) {
            Log.w("FamilyRepository", "Удаление продукта отклонено: $currentUserId не создатель")
            return false
        }

        // Find the product locally to get its firebaseKey
        val familyProduct = familyProductDao.getFamilyProducts(familyId).first().find { it.id == product.id }
        if (familyProduct != null) {
            familyProductDao.deleteFamilyProduct(familyId, familyProduct.id)
            FirebaseSync.database.child("families").child(familyId).child("products").child(familyProduct.firebaseKey).removeValue()
            Log.d("FamilyRepository", "Продукт удалён: familyId=$familyId, firebaseKey=${familyProduct.firebaseKey}")
        } else {
            Log.w("FamilyRepository", "Продукт не найден локально: familyId=$familyId, productId=${product.id}")
        }
        return true
    }

    suspend fun deleteFamilyRecipe(familyId: String, recipe: RecipeEntity, currentUserId: String): Boolean {
        val family = familyDao.getAllFamilies().first().find { it.id == familyId }
            ?: return false.also { Log.w("FamilyRepository", "Семья $familyId не найдена") }

        if (family.creatorId != currentUserId) {
            Log.w("FamilyRepository", "Удаление рецепта отклонено: $currentUserId не создатель")
            return false
        }

        // Find the recipe locally to get its firebaseKey
        val familyRecipe = familyRecipeDao.getFamilyRecipes(familyId).first().find { it.id == recipe.id }
        if (familyRecipe != null) {
            familyRecipeDao.deleteFamilyRecipe(familyId, familyRecipe.id)
            FirebaseSync.database.child("families").child(familyId).child("recipes").child(familyRecipe.firebaseKey).removeValue()
            Log.d("FamilyRepository", "Рецепт удалён: familyId=$familyId, firebaseKey=${familyRecipe.firebaseKey}")
        } else {
            Log.w("FamilyRepository", "Рецепт не найден локально: familyId=$familyId, recipeId=${recipe.id}")
        }
        return true
    }

    suspend fun deleteFamilyShoppingItem(familyId: String, item: ShoppingItemEntity, currentUserId: String): Boolean {
        val family = familyDao.getAllFamilies().first().find { it.id == familyId }
            ?: return false.also { Log.w("FamilyRepository", "Семья $familyId не найдена") }

        if (family.creatorId != currentUserId) {
            Log.w("FamilyRepository", "Удаление элемента списка покупок отклонено: $currentUserId не создатель")
            return false
        }

        // Find the item locally to get its firebaseKey
        val familyItem = familyShoppingItemDao.getFamilyShoppingItems(familyId).first().find { it.id == item.id }
        if (familyItem != null) {
            familyShoppingItemDao.deleteFamilyShoppingItem(familyId, familyItem.id)
            FirebaseSync.database.child("families").child(familyId).child("shopping_items").child(familyItem.firebaseKey).removeValue()
            Log.d("FamilyRepository", "Элемент списка покупок удалён: familyId=$familyId, firebaseKey=${familyItem.firebaseKey}")
        } else {
            Log.w("FamilyRepository", "Элемент не найден локально: familyId=$familyId, itemId=${item.id}")
        }
        return true
    }

    suspend fun updateFamilyName(familyId: String, newName: String) {
        val family = familyDao.getAllFamilies().first().find { it.id == familyId }
        if (family != null) {
            familyDao.updateFamily(family.copy(name = newName))
            FirebaseSync.updateFamilyName(familyId, newName)
            Log.d("FamilyRepository", "Название семьи обновлено: id=$familyId, newName=$newName")
        }
    }

    fun getFamilyProducts(familyId: String): StateFlow<List<FamilyProductEntity>> {
        if (productListeners[familyId] == null) {
            scope.launch {
                familyProductDao.getFamilyProducts(familyId).collect { familyProducts ->
                    _familyProducts.value = _familyProducts.value.toMutableMap().apply {
                        this[familyId] = familyProducts
                    }
                    Log.d("FamilyRepository", "Локальные семейные продукты обновлены: familyId=$familyId, count=${familyProducts.size}")
                }
            }

            val listener = FirebaseSync.listenProducts(familyId) { snapshot ->
                scope.launch {
                    familyProductDao.deleteFamilyProducts(familyId)
                    snapshot.children.forEach { dataSnapshot ->
                        val key = dataSnapshot.key ?: return@forEach
                        val product = dataSnapshot.getValue(FirebaseProductEntity::class.java) ?: return@forEach
                        familyProductDao.insertFamilyProduct(
                            FamilyProductEntity(
                                familyId = familyId,
                                firebaseKey = key,
                                name = product.name ?: "Без названия",
                                quantity = product.quantity ?: 0,
                                unit = product.unit ?: "",
                                expiryDate = product.expiryDate?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
                            )
                        )
                    }
                    Log.d("FamilyRepository", "Продукты синхронизированы из Firebase: familyId=$familyId, count=${snapshot.childrenCount}")
                }
            }
            productListeners[familyId] = listener
        }
        return _familyProducts.map { it[familyId] ?: emptyList() }.stateIn(scope, SharingStarted.Lazily, emptyList())
    }

    fun getFamilyRecipes(familyId: String): StateFlow<List<FamilyRecipeEntity>> {
        if (recipeListeners[familyId] == null) {
            scope.launch {
                familyRecipeDao.getFamilyRecipes(familyId).collect { familyRecipes ->
                    _familyRecipes.value = _familyRecipes.value.toMutableMap().apply {
                        this[familyId] = familyRecipes
                    }
                    Log.d("FamilyRepository", "Локальные семейные рецепты обновлены: familyId=$familyId, count=${familyRecipes.size}")
                }
            }

            val listener = FirebaseSync.listenRecipes(familyId) { snapshot ->
                scope.launch {
                    familyRecipeDao.deleteFamilyRecipes(familyId)
                    snapshot.children.forEach { dataSnapshot ->
                        val key = dataSnapshot.key ?: return@forEach
                        val recipeData = dataSnapshot.value as? Map<String, Any> ?: return@forEach
                        familyRecipeDao.insertFamilyRecipe(
                            FamilyRecipeEntity(
                                familyId = familyId,
                                firebaseKey = key,
                                title = recipeData["title"] as? String,
                                ingredients = recipeData["ingredients"] as? String,
                                instructions = recipeData["instructions"] as? String,
                                mealType = recipeData["mealType"] as? String,
                                recipeMode = recipeData["recipeMode"] as? String
                            )
                        )
                    }
                    Log.d("FamilyRepository", "Рецепты синхронизированы из Firebase: familyId=$familyId, count=${snapshot.childrenCount}")
                }
            }
            recipeListeners[familyId] = listener
        }
        return _familyRecipes.map { it[ familyId] ?: emptyList() }.stateIn(scope, SharingStarted.Lazily, emptyList())
    }

    fun getFamilyShoppingItems(familyId: String): StateFlow<List<FamilyShoppingItemEntity>> {
        if (shoppingItemListeners[familyId] == null) {
            scope.launch {
                familyShoppingItemDao.getFamilyShoppingItems(familyId).collect { familyItems ->
                    _familyShoppingItems.value = _familyShoppingItems.value.toMutableMap().apply {
                        this[familyId] = familyItems
                    }
                    Log.d("FamilyRepository", "Локальные семейные элементы списка покупок обновлены: familyId=$familyId, count=${familyItems.size}")
                }
            }

            val listener = FirebaseSync.listenShoppingItems(familyId) { snapshot ->
                scope.launch {
                    familyShoppingItemDao.deleteFamilyShoppingItems(familyId)
                    snapshot.children.forEach { dataSnapshot ->
                        val key = dataSnapshot.key ?: return@forEach
                        val itemData = dataSnapshot.value as? Map<String, Any> ?: return@forEach
                        familyShoppingItemDao.insertFamilyShoppingItem(
                            FamilyShoppingItemEntity(
                                familyId = familyId,
                                firebaseKey = key,
                                name = itemData["name"] as? String ?: "",
                                quantity = (itemData["quantity"] as? Long)?.toInt() ?: 0,
                                unit = itemData["unit"] as? String ?: "",
                                isPurchased = itemData["isPurchased"] as? Boolean ?: false
                            )
                        )
                    }
                    Log.d("FamilyRepository", "Список покупок синхронизирован из Firebase: familyId=$familyId, count=${snapshot.childrenCount}")
                }
            }
            shoppingItemListeners[familyId] = listener
        }
        return _familyShoppingItems.map { it[familyId] ?: emptyList() }.stateIn(scope, SharingStarted.Lazily, emptyList())
    }

    suspend fun shareProducts(familyId: String, products: List<ProductEntity>) {
        products.forEach { product ->
            val newKey = FirebaseSync.database.child("families").child(familyId).child("products").push().key
                ?: throw Exception("Не удалось сгенерировать ключ в Firebase")
            val firebaseProduct = FirebaseProductEntity.fromProductEntity(product)
            FirebaseSync.database.child("families").child(familyId).child("products").child(newKey).setValue(firebaseProduct)
            familyProductDao.insertFamilyProduct(
                FamilyProductEntity(
                    familyId = familyId,
                    firebaseKey = newKey,
                    name = product.name ?: "Без названия",
                    quantity = product.quantity ?: 0,
                    unit = product.unit ?: "",
                    expiryDate = product.expiryDate
                )
            )
        }
        Log.d("FamilyRepository", "Продукты синхронизированы: familyId=$familyId, count=${products.size}")
    }

    suspend fun shareRecipe(familyId: String, recipe: RecipeEntity) {
        val newKey = FirebaseSync.database.child("families").child(familyId).child("recipes").push().key
            ?: throw Exception("Не удалось сгенерировать ключ в Firebase")
        val recipeData = mapOf(
            "title" to recipe.title,
            "ingredients" to recipe.ingredients,
            "instructions" to recipe.instructions,
            "mealType" to recipe.mealType,
            "recipeMode" to recipe.recipeMode
        )
        FirebaseSync.database.child("families").child(familyId).child("recipes").child(newKey).setValue(recipeData)
        familyRecipeDao.insertFamilyRecipe(
            FamilyRecipeEntity(
                familyId = familyId,
                firebaseKey = newKey,
                title = recipe.title,
                ingredients = recipe.ingredients,
                instructions = recipe.instructions,
                mealType = recipe.mealType,
                recipeMode = recipe.recipeMode
            )
        )
        Log.d("FamilyRepository", "Рецепт синхронизирован: familyId=$familyId, title=${recipe.title}")
    }

    suspend fun shareShoppingItems(familyId: String, items: List<ShoppingItemEntity>) {
        items.forEach { item ->
            val newKey = FirebaseSync.database.child("families").child(familyId).child("shopping_items").push().key
                ?: throw Exception("Не удалось сгенерировать ключ в Firebase")
            val itemData = mapOf(
                "name" to item.name,
                "quantity" to item.quantity,
                "unit" to item.unit,
                "isPurchased" to item.isPurchased
            )
            FirebaseSync.database.child("families").child(familyId).child("shopping_items").child(newKey).setValue(itemData)
            familyShoppingItemDao.insertFamilyShoppingItem(
                FamilyShoppingItemEntity(
                    familyId = familyId,
                    firebaseKey = newKey,
                    name = item.name,
                    quantity = item.quantity,
                    unit = item.unit,
                    isPurchased = item.isPurchased
                )
            )
        }
        Log.d("FamilyRepository", "Список покупок синхронизирован: familyId=$familyId, count=${items.size}")
    }
}