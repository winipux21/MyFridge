package ru.ngtu.myfridge.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ngtu.myfridge.ui.theme.Purple40

// Импорт новых экранов
import ru.ngtu.myfridge.presentation.screens.SupportCenterScreen
import ru.ngtu.myfridge.presentation.screens.ContactInfoScreen

@Composable
fun HelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf("help") }

    // Системная кнопка «Назад»
    BackHandler(enabled = true) {
        when (currentScreen) {
            "help"            -> onBack()
            "about", "termsAndPrivacy" -> currentScreen = "help"
            "support"         -> currentScreen = "help"
            "contact"         -> currentScreen = "support"
        }
    }

    when (currentScreen) {
        // === Главное меню «Помощь» ===
        "help" -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(0.dp)
            ) {
                // Навбар
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
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Помощь",
                        style = TextStyle(fontSize = 40.sp),
                        color = Purple40
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Справочный центр
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { currentScreen = "support" },
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Help,
                                contentDescription = "Справочный центр",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "Справочный центр",
                                style = TextStyle(fontSize = 20.sp),
                                color = Purple40
                            )
                        }
                    }

                    // Условия и Политика
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { currentScreen = "termsAndPrivacy" },
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Условия и Политика",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "Условия и Политика конфиденциальности",
                                style = TextStyle(fontSize = 20.sp),
                                color = Purple40
                            )
                        }
                    }

                    // О приложении
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { currentScreen = "about" },
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "О приложении",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "О приложении",
                                style = TextStyle(fontSize = 20.sp),
                                color = Purple40
                            )
                        }
                    }
                }
            }
        }

        // === Существующие экраны ===
        "about" -> AboutScreen(
            onBack = { currentScreen = "help" },
            modifier = modifier
        )
        "termsAndPrivacy" -> TermsAndPrivacyScreen(
            onBack = { currentScreen = "help" },
            modifier = modifier
        )

        // === Новый экран «Справочный центр» ===
        "support" -> SupportCenterScreen(
            onBack = { currentScreen = "help" },
            onContactClick = { currentScreen = "contact" },
            modifier = modifier
        )

        // === Экран Контактов ===
        "contact" -> ContactInfoScreen(
            onBack = { currentScreen = "support" },
            modifier = modifier
        )
    }
}
