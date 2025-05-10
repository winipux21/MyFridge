package ru.ngtu.myfridge.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.ngtu.myfridge.data.db.FirebaseProductEntity
import ru.ngtu.myfridge.data.db.ProductEntity
import ru.ngtu.myfridge.data.db.RecipeEntity
import ru.ngtu.myfridge.data.db.ShoppingItemEntity
import ru.ngtu.myfridge.presentation.screens.Family
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FirebaseSync {
    val database = FirebaseDatabase.getInstance("https://myfridge-b48d1-default-rtdb.europe-west1.firebasedatabase.app").reference
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: "default"

    fun createFamily(name: String, familyId: String, callback: (Boolean, String?) -> Unit) {
        val familyData = mapOf(
            "name" to name,
            "users" to mapOf(userId to true),
            "creator" to userId
        )
        database.child("families").child(familyId).setValue(familyData)
            .addOnSuccessListener { callback(true, familyId) }
            .addOnFailureListener { exception ->
                Log.e("FirebaseSync", "Ошибка создания семьи: ${exception.message}")
                callback(false, null)
            }
    }

    fun joinFamily(familyId: String, callback: (Boolean) -> Unit) {
        database.child("families").child(familyId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists() && !snapshot.child("users").child(userId).exists()) {
                database.child("families").child(familyId).child("users").child(userId).setValue(true)
                    .addOnSuccessListener { callback(true) }
                    .addOnFailureListener { callback(false) }
            } else {
                callback(false)
            }
        }.addOnFailureListener { callback(false) }
    }

    suspend fun joinFamilySuspend(familyId: String): Boolean = suspendCancellableCoroutine { continuation ->
        database.child("families").child(familyId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists() && !snapshot.child("users").child(userId).exists()) {
                database.child("families").child(familyId).child("users").child(userId).setValue(true)
                    .addOnSuccessListener { continuation.resume(true) }
                    .addOnFailureListener { continuation.resume(false) }
            } else {
                continuation.resume(false)
            }
        }.addOnFailureListener { continuation.resume(false) }
    }

    fun deleteFamily(familyId: String, callback: (Boolean) -> Unit) {
        database.child("families").child(familyId).removeValue()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun deleteFamilyRecipe(familyId: String, recipeId: Int) {
        database.child("families").child(familyId).child("recipes").child(recipeId.toString()).removeValue()
            .addOnSuccessListener { Log.d("FirebaseSync", "Рецепт удалён: $recipeId") }
            .addOnFailureListener { Log.e("FirebaseSync", "Ошибка удаления рецепта") }
    }

    fun updateFamilyName(familyId: String, newName: String) {
        database.child("families").child(familyId).child("name").setValue(newName)
            .addOnSuccessListener { Log.d("FirebaseSync", "Название обновлено: $familyId") }
            .addOnFailureListener { Log.e("FirebaseSync", "Ошибка обновления названия") }
    }

    fun getFamiliesForUserRealtime(callback: (List<Family>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val families = snapshot.children.mapNotNull { snap ->
                    val name = snap.child("name").getValue(String::class.java) ?: return@mapNotNull null
                    val users = snap.child("users").getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {}) ?: emptyMap()
                    val creator = snap.child("creator").getValue(String::class.java)
                    if (users.containsKey(userId)) Family(snap.key!!, name, users, creator) else null
                }
                callback(families)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseSync", "Ошибка получения семей: ${error.message}")
                callback(emptyList())
            }
        }
        database.child("families").addValueEventListener(listener)
        return listener
    }

    fun removeFamiliesListener(listener: ValueEventListener) {
        database.child("families").removeEventListener(listener)
    }

    fun getFamiliesForUser(callback: (List<Family>) -> Unit) {
        database.child("families")
            .orderByChild("users/$userId")
            .equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val families = snapshot.children.mapNotNull { snap ->
                        val name = snap.child("name").getValue(String::class.java) ?: return@mapNotNull null
                        val users = snap.child("users").getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {}) ?: emptyMap()
                        val creator = snap.child("creator").getValue(String::class.java)
                        Family(snap.key!!, name, users, creator)
                    }
                    callback(families)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }

    suspend fun getFamiliesForUserSuspend(): List<Family> = suspendCancellableCoroutine { continuation ->
        database.child("families")
            .orderByChild("users/$userId")
            .equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val families = snapshot.children.mapNotNull { snap ->
                        val name = snap.child("name").getValue(String::class.java) ?: return@mapNotNull null
                        val users = snap.child("users").getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {}) ?: emptyMap()
                        val creator = snap.child("creator").getValue(String::class.java)
                        Family(snap.key!!, name, users, creator)
                    }
                    continuation.resume(families)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            })
    }

    fun listenProducts(familyId: String, callback: (DataSnapshot) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseSync", "Ошибка получения продуктов: ${error.message}")
            }
        }
        database.child("families").child(familyId).child("products").addValueEventListener(listener)
        return listener
    }

    fun removeProductsListener(familyId: String, listener: ValueEventListener) {
        database.child("families").child(familyId).child("products").removeEventListener(listener)
    }

    fun listenRecipes(familyId: String, callback: (DataSnapshot) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseSync", "Ошибка получения рецептов: ${error.message}")
            }
        }
        database.child("families").child(familyId).child("recipes").addValueEventListener(listener)
        return listener
    }

    fun removeRecipesListener(familyId: String, listener: ValueEventListener) {
        database.child("families").child(familyId).child("recipes").removeEventListener(listener)
    }

    fun listenShoppingItems(familyId: String, callback: (DataSnapshot) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseSync", "Ошибка получения списка покупок: ${error.message}")
            }
        }
        database.child("families").child(familyId).child("shopping_items").addValueEventListener(listener)
        return listener
    }

    fun removeShoppingItemsListener(familyId: String, listener: ValueEventListener) {
        database.child("families").child(familyId).child("shopping_items").removeEventListener(listener)
    }

    fun getFamilyId(callback: (String?) -> Unit) {
        database.child("families").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (family in snapshot.children) {
                    if (family.child("users").child(userId).exists()) {
                        callback(family.key)
                        return
                    }
                }
                callback(null)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    // Новый метод для синхронизации рецепта
    fun syncRecipe(recipe: RecipeEntity, familyId: String) {
        val newKey = database.child("families").child(familyId).child("recipes").push().key
            ?: throw IllegalStateException("Не удалось сгенерировать ключ для рецепта")
        val recipeData = mapOf(
            "title" to recipe.title,
            "ingredients" to recipe.ingredients,
            "instructions" to recipe.instructions,
            "mealType" to recipe.mealType,
            "recipeMode" to recipe.recipeMode
        )
        database.child("families").child(familyId).child("recipes").child(newKey).setValue(recipeData)
            .addOnSuccessListener { Log.d("FirebaseSync", "Рецепт синхронизирован: familyId=$familyId, title=${recipe.title}") }
            .addOnFailureListener { Log.e("FirebaseSync", "Ошибка синхронизации рецепта: ${it.message}") }
    }
}