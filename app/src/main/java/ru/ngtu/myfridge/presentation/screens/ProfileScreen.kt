package ru.ngtu.myfridge.presentation.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.ngtu.myfridge.R
import ru.ngtu.myfridge.utils.scheduleExpiryCheck
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    avatarBitmap: Bitmap?,
    onAvatarBitmapChanged: (Bitmap?) -> Unit,
    onBack: () -> Unit,
    onReturnToMainScreen: () -> Unit
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        Log.e("ProfileScreen", "Пользователь не авторизован")
        Toast.makeText(context, "Пользователь не авторизован", Toast.LENGTH_LONG).show()
        onBack()
        return
    }
    val userId = user.uid
    val database = FirebaseDatabase.getInstance("https://myfridge-b48d1-default-rtdb.europe-west1.firebasedatabase.app")
        .reference.child("users").child(userId)
    var nickname by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) } // Для выбора времени уведомлений

    // Загружаем сохранённый путь к аватарке из SharedPreferences (только если avatarBitmap ещё не установлен)
    val sharedPreferences = context.getSharedPreferences("MyFridgePrefs", Context.MODE_PRIVATE)
    LaunchedEffect(Unit) {
        if (avatarBitmap == null) {
            val savedAvatarPath = sharedPreferences.getString("avatarPath_$userId", null)
            if (savedAvatarPath != null) {
                val avatarFile = File(savedAvatarPath)
                if (avatarFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(avatarFile.absolutePath)
                    onAvatarBitmapChanged(bitmap)
                    Log.d("ProfileScreen", "Аватарка загружена из SharedPreferences: $savedAvatarPath")
                }
            }
        }
    }

    // Загрузка данных в реальном времени (только для никнейма)
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                nickname = snapshot.child("nickname").getValue(String::class.java) ?: ""
                Log.d("ProfileScreen", "Данные загружены из базы: nickname=$nickname")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileScreen", "Ошибка загрузки данных: ${error.message}")
                Toast.makeText(context, "Ошибка загрузки данных: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        database.addValueEventListener(listener)
        onDispose {
            database.removeEventListener(listener)
        }
    }

    // Лаунчер для выбора изображения из галереи
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Log.d("ProfileScreen", "Выбрано изображение: $uri")
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Log.e("ProfileScreen", "Не удалось открыть InputStream для URI: $uri")
                    Toast.makeText(context, "Не удалось открыть изображение", Toast.LENGTH_SHORT).show()
                    return@let
                }

                val selectedBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (selectedBitmap == null) {
                    Log.e("ProfileScreen", "Не удалось декодировать изображение")
                    Toast.makeText(context, "Ошибка: не удалось загрузить изображение", Toast.LENGTH_SHORT).show()
                    return@let
                }

                val avatarFile = File(context.filesDir, "avatar_$userId.jpg")
                avatarFile.outputStream().use { output ->
                    selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                }

                if (avatarFile.exists() && avatarFile.length() > 0) {
                    Log.d("ProfileScreen", "Файл успешно сохранён: ${avatarFile.absolutePath}, размер: ${avatarFile.length()} байт")
                    sharedPreferences.edit().putString("avatarPath_$userId", avatarFile.absolutePath).apply()
                    onAvatarBitmapChanged(selectedBitmap)
                    Toast.makeText(context, "Аватарка обновлена", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ProfileScreen", "Файл не сохранён или пустой: ${avatarFile.absolutePath}")
                    Toast.makeText(context, "Ошибка: файл не сохранён", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Ошибка сохранения файла: ${e.message}", e)
                Toast.makeText(context, "Ошибка сохранения файла: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Log.w("ProfileScreen", "URI изображения равен null")
            Toast.makeText(context, "Изображение не выбрано", Toast.LENGTH_SHORT).show()
        }
    }

    // Запрос разрешения на доступ к галерее
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Log.d("ProfileScreen", "Разрешение READ_MEDIA_IMAGES предоставлено")
            galleryLauncher.launch("image/*")
        } else {
            Log.w("ProfileScreen", "Разрешение READ_MEDIA_IMAGES отклонено")
            Toast.makeText(context, "Требуется разрешение для доступа к галерее", Toast.LENGTH_SHORT).show()
        }
    }

    // Перехват системной кнопки "Назад"
    BackHandler(enabled = true) {
        Log.d("ProfileScreen", "Системная кнопка 'Назад' нажата")
        onBack() // Возвращаемся на главный экран
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Профиль",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.harlow_solid_italic)),
                            fontSize = 24.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onReturnToMainScreen) {
                        Icon(Icons.Filled.Home, contentDescription = "Вернуться на главный экран")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Аватарка
            if (avatarBitmap != null) {
                Image(
                    bitmap = avatarBitmap.asImageBitmap(),
                    contentDescription = "Аватарка",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 8.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_default_avatar),
                    contentDescription = "Аватарка по умолчанию",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 8.dp)
                )
            }
            Button(onClick = {
                Log.d("ProfileScreen", "Нажата кнопка 'Сменить аватарку'")
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }) {
                Text("Сменить аватарку")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Никнейм
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Никнейм") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                Log.d("ProfileScreen", "Нажата кнопка 'Сохранить никнейм'")
                database.child("nickname").setValue(nickname)
                    .addOnSuccessListener {
                        Log.d("ProfileScreen", "Никнейм успешно сохранён: $nickname")
                        Toast.makeText(context, "Никнейм сохранён", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProfileScreen", "Ошибка сохранения никнейма: ${e.message}")
                        Toast.makeText(context, "Ошибка сохранения никнейма: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }) {
                Text("Сохранить никнейм")
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Кнопка для настройки времени уведомлений
            Button(onClick = { showTimePicker = true }) {
                Text("Настроить время уведомлений")
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        // Диалоговое окно для выбора времени уведомлений
        if (showTimePicker) {
            TimePickerDialog(
                onDismiss = { showTimePicker = false },
                onTimeSelected = { hour, minute ->
                    saveNotificationTime(context, hour, minute)
                    Toast.makeText(context, "Уведомления будут приходить в $hour:$minute", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// Функция сохранения времени уведомлений в SharedPreferences
private fun saveNotificationTime(context: Context, hour: Int, minute: Int) {
    val sharedPreferences = context.getSharedPreferences("MyFridgePrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().apply {
        putInt("notificationHour", hour)
        putInt("notificationMinute", minute)
        apply()
    }
    // Перепланируем уведомления после изменения времени
    scheduleExpiryCheck(context)
}

// Компонент TimePickerDialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите время уведомлений") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(timePickerState.hour, timePickerState.minute)
                onDismiss()
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}