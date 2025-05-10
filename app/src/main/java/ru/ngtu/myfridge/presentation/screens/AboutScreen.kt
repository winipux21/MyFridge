package ru.ngtu.myfridge.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ngtu.myfridge.R
import ru.ngtu.myfridge.ui.theme.Purple40

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val harlowFont = FontFamily(Font(R.font.harlow_solid_italic))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFDEFFF6)), // Тот же фон, что на SplashScreen
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Кастомный навбар
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBack)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "О приложении",
                style = TextStyle(fontSize = 40.sp),
                color = Purple40
            )
        }

        // Вертикальный Spacer для центрирования
        Spacer(modifier = Modifier.weight(1f))

        // Основное содержимое
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Иконка приложения
            Image(
                painter = painterResource(id = R.drawable.display),
                contentDescription = "Иконка приложения",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 16.dp)
            )

            // Название приложения
            Text(
                text = "My Fridge",
                style = TextStyle(
                    fontSize = 36.sp,
                    fontFamily = harlowFont,
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Версия приложения
            Text(
                text = "Версия 1.4", // Соответствует versionName в build.gradle
                style = TextStyle(fontSize = 20.sp),
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Дата последнего обновления
            Text(
                text = "Последнее обновление: 17 апреля 2025", // Указываем текущую дату как дату обновления
                style = TextStyle(fontSize = 16.sp),
                color = Color.Black
            )
        }

        // Вертикальный Spacer для центрирования
        Spacer(modifier = Modifier.weight(1f))
    }
}