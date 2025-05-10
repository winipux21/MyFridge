package ru.ngtu.myfridge

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.ngtu.myfridge.data.db.AppDatabase
import ru.ngtu.myfridge.data.repository.FamilyRepository
import ru.ngtu.myfridge.data.repository.ProductRepository
import ru.ngtu.myfridge.data.repository.RecipeRepository
import ru.ngtu.myfridge.data.repository.ShoppingListRepository
import ru.ngtu.myfridge.presentation.ProductViewModel
import ru.ngtu.myfridge.presentation.ProductViewModelFactory
import ru.ngtu.myfridge.presentation.ShoppingListViewModel
import ru.ngtu.myfridge.presentation.ShoppingListViewModelFactory
import ru.ngtu.myfridge.presentation.screens.*
import ru.ngtu.myfridge.ui.theme.MyFridgeTheme
import ru.ngtu.myfridge.R
import androidx.compose.ui.graphics.asImageBitmap
import ru.ngtu.myfridge.utils.scheduleExpiryCheck
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val productRepository = ProductRepository(database.productDao())
        val shoppingListRepository = ShoppingListRepository(database.shoppingItemDao())
        val recipeRepository = RecipeRepository(database.recipeDao())
        val familyRepository = FamilyRepository(
            familyDao = database.familyDao(),
            familyProductDao = database.familyProductDao(),
            familyRecipeDao = database.familyRecipeDao(),
            familyShoppingItemDao = database.familyShoppingItemDao()
        )
        val productFactory = ProductViewModelFactory(productRepository, recipeRepository, familyRepository)
        val shoppingListFactory = ShoppingListViewModelFactory(shoppingListRepository)

        FirebaseDatabase.getInstance("https://myfridge-b48d1-default-rtdb.europe-west1.firebasedatabase.app")

        // Планирование проверки сроков годности
        scheduleExpiryCheck(this)

        setContent {
            MyFridgeTheme {
                val productViewModel: ProductViewModel = viewModel(factory = productFactory)
                val shoppingListViewModel: ShoppingListViewModel = viewModel(factory = shoppingListFactory)

                // Запрос разрешения на уведомления
                RequestNotificationPermission()
                AppNavigation(productViewModel, shoppingListViewModel)
            }
        }
    }

    @Composable
    fun RequestNotificationPermission() {
        val context = LocalContext.current
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Разрешение на уведомления отклонено", Toast.LENGTH_SHORT).show()
            }
        }

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(productViewModel: ProductViewModel, shoppingListViewModel: ShoppingListViewModel) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    var user by remember { mutableStateOf(auth.currentUser) }

    val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    user = auth.currentUser
                } else {
                    Toast.makeText(context, "Ошибка авторизации: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка Google Sign-In: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    if (user == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { signInLauncher.launch(googleSignInClient.signInIntent) }) {
                Text("Войти через Google")
            }
        }
    } else {
        MainScreen(
            productViewModel = productViewModel,
            shoppingListViewModel = shoppingListViewModel,
            onSignOut = {
                auth.signOut()
                googleSignInClient.signOut().addOnCompleteListener {
                    user = null
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    productViewModel: ProductViewModel,
    shoppingListViewModel: ShoppingListViewModel,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Холодильник", "Список покупок", "Рецепты")
    var showDropdown by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("main") }
    var selectedFamily by remember { mutableStateOf<Family?>(null) }
    var backPressedOnce by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
    val database = FirebaseDatabase.getInstance("https://myfridge-b48d1-default-rtdb.europe-west1.firebasedatabase.app")
        .reference.child("users").child(userId)
    var nickname by remember { mutableStateOf("") }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val sharedPreferences = context.getSharedPreferences("MyFridgePrefs", Context.MODE_PRIVATE)
    val savedAvatarPath = sharedPreferences.getString("avatarPath_$userId", null)
    LaunchedEffect(Unit) {
        if (savedAvatarPath != null) {
            val avatarFile = File(savedAvatarPath)
            if (avatarFile.exists()) {
                avatarBitmap = BitmapFactory.decodeFile(avatarFile.absolutePath)
                Log.d("MainScreen", "Аватарка загружена из SharedPreferences: $savedAvatarPath")
            }
        }
    }

    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                nickname = snapshot.child("nickname").getValue(String::class.java) ?: ""
                Log.d("MainScreen", "Данные профиля: nickname=$nickname")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainScreen", "Ошибка загрузки данных профиля: ${error.message}")
            }
        }
        database.addValueEventListener(listener)
        onDispose {
            database.removeEventListener(listener)
        }
    }

    val harlowFont = FontFamily(Font(R.font.harlow_solid_italic))

    // Обработка системной кнопки "Назад"
    BackHandler(enabled = true) {
        when (currentScreen) {
            "main" -> {
                if (backPressedOnce) {
                    (context as ComponentActivity).finish()
                } else {
                    backPressedOnce = true
                    Toast.makeText(context, "Нажмите ещё раз для выхода", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({ backPressedOnce = false }, 2000)
                }
            }
            "help" -> {
                currentScreen = "main"
                selectedTab = 0
            }
            else -> {
                currentScreen = "main"
                selectedTab = 0
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Fridge",
                        style = TextStyle(fontFamily = harlowFont, fontSize = 24.sp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF82ACDC),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    Box {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (nickname.isNotEmpty()) {
                                Text(nickname, modifier = Modifier.padding(end = 8.dp))
                            }
                            IconButton(onClick = { showDropdown = true }) {
                                if (avatarBitmap != null) {
                                    Image(
                                        bitmap = avatarBitmap!!.asImageBitmap(),
                                        contentDescription = "Аватарка",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_default_avatar),
                                        contentDescription = "Аватарка по умолчанию",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        }
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Профиль") },
                                onClick = {
                                    currentScreen = "profile"
                                    showDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Мои рецепты") },
                                onClick = {
                                    currentScreen = "myRecipes"
                                    showDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Моя семья") },
                                onClick = {
                                    currentScreen = "myFamily"
                                    showDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Помощь") },
                                onClick = {
                                    currentScreen = "help"
                                    showDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Выйти") },
                                onClick = {
                                    productViewModel.removeFamilyListeners()
                                    onSignOut()
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            )
        },
        content = { paddingValues ->
            @Composable
            fun ScaffoldContent() {
                when (currentScreen) {
                    "profile" -> ProfileScreen(
                        avatarBitmap = avatarBitmap,
                        onAvatarBitmapChanged = { newBitmap -> avatarBitmap = newBitmap },
                        onBack = { currentScreen = "main" },
                        onReturnToMainScreen = {
                            currentScreen = "main"
                            selectedTab = 0
                        }
                    )
                    "photo" -> PhotoRecognitionScreen(
                        onBack = { currentScreen = "main" },
                        viewModel = productViewModel
                    )
                    "myRecipes" -> MyRecipesScreen(
                        viewModel = productViewModel,
                        onBack = { currentScreen = "main" }
                    )
                    "myFamily" -> MyFamilyScreen(
                        viewModel = productViewModel,
                        onBack = { currentScreen = "main" },
                        onFamilySelected = { family ->
                            selectedFamily = family
                            currentScreen = "familyDetail"
                        }
                    )
                    "familyDetail" -> selectedFamily?.let {
                        FamilyDetailScreen(
                            family = it,
                            viewModel = productViewModel,
                            shoppingListViewModel = shoppingListViewModel,
                            onBack = { currentScreen = "myFamily" }
                        )
                    }
                    "help" -> HelpScreen(
                        onBack = {
                            currentScreen = "main"
                            selectedTab = 0
                        },
                        modifier = Modifier.padding(paddingValues) // Учитываем отступы от главного навбара
                    )
                    "main" -> Column(Modifier.padding(paddingValues)) {
                        TabRow(selectedTabIndex = selectedTab) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    text = { Text(title) },
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index }
                                )
                            }
                        }
                        when (selectedTab) {
                            0 -> ProductListScreen(
                                productViewModel = productViewModel,
                                onNavigateToPhoto = { currentScreen = "photo" }
                            )
                            1 -> ShoppingListScreen(shoppingListViewModel)
                            2 -> RecipeScreen(viewModel = productViewModel)
                        }
                    }
                }
            }
            ScaffoldContent()
        }
    )
}