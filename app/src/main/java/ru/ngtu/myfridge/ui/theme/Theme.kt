package ru.ngtu.myfridge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MyFridgeColorScheme = lightColorScheme(
    primary = Purple40,              // Основной цвет
    secondary = PurpleGrey40,        // Вторичный цвет
    tertiary = Pink40,               // Третичный цвет
    background = Color(0xFFDEFFF6),  // Фон всех экранов
    surface = Color(0xFFDEFFF6),     // Поверхности (карточки, диалоги)
    onPrimary = Color.White,         // Текст на primary
    onSecondary = Color.White,       // Текст на secondary
    onTertiary = Color.White,        // Текст на tertiary
    onBackground = Color.Black,      // Текст на фоне
    onSurface = Color.Black          // Текст на поверхностях
)

@Composable
fun MyFridgeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MyFridgeColorScheme,
        typography = Typography,
        content = content
    )
}