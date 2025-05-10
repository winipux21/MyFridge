package ru.ngtu.myfridge.presentation.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ngtu.myfridge.ui.theme.Purple40

@Composable
fun ContactInfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFDEFFF6))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Связаться с нами",
                style = TextStyle(fontSize = 24.sp, color = Purple40)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Email: myfridge@mail.ru",
            style = TextStyle(fontSize = 16.sp, color = Color.Black),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = "Телефон: +7 904 217-86-52",
            style = TextStyle(fontSize = 16.sp, color = Color.Black),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = "Адрес:\nНижегородская область,\nНижний Новгород,\nНГТУ им. Р. Алексеева",
            style = TextStyle(fontSize = 16.sp, color = Color.Black),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}