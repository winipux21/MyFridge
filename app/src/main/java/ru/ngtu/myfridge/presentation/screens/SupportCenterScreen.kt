package ru.ngtu.myfridge.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ngtu.myfridge.R
import ru.ngtu.myfridge.ui.theme.Purple40

@Composable
fun SupportCenterScreen(
    onBack: () -> Unit,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFDEFFF6))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                /* -------- Навбар -------- */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onBack)
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Справочный центр",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Purple40
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                /* -------- Вспомогательные функции -------- */
                @Composable
                fun sectionTitle(text: String) {
                    Text(
                        text = text,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Purple40
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                @Composable
                fun bodyText(text: String) {
                    Text(
                        text = text,
                        style = TextStyle(fontSize = 14.sp, color = Color.Black),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                /* -------- Содержимое -------- */
                sectionTitle("Регистрация")
                bodyText(
                    "Для начала работы с приложением вы можете выбрать аккаунт Google из списка или добавить новый. " +
                            "После этого вы автоматически войдете в MyFridge."
                )

                sectionTitle("Основные функции")
                bodyText("Добавление продуктов:")

                /* --- Вручную --- */
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "* Вручную:\n1. Нажмите на иконку ",
                            style = TextStyle(fontSize = 14.sp, color = Color.Black)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.plusik),
                            contentDescription = "Плюсик",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = ".",
                            style = TextStyle(fontSize = 14.sp, color = Color.Black)
                        )
                    }
                    Text(
                        text = "2. Заполните название, количество, единицу измерения и срок годности.\n" +
                                "3. Нажмите на кнопку “Добавить”. После чего продукт будет находиться в вашем холодильнике.",
                        style = TextStyle(fontSize = 14.sp, color = Color.Black)
                    )
                }

                /* --- Фото‑распознавание --- */
                bodyText(
                    "* Фото-распознавание:\n" +
                            "  1. Используйте кнопку 📸 (камера или галерея).\n" +
                            "  2. Нажмите “Анализировать фото”.\n" +
                            "  **Если не удалось распознать продукты, попробуйте повторить попытку.**\n" +
                            "  3. Отредактируйте список и нажмите “Добавить в холодильник”."
                )

                /* --- Кнопки «Редактировать» и «Удалить» --- */
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ruchka),
                        contentDescription = "Ручка",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "С помощью этой кнопки вы можете отредактировать информацию о продукте.",
                        style = TextStyle(fontSize = 14.sp, color = Color.Black),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.korzina),
                        contentDescription = "Корзина",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "С помощью этой кнопки вы можете удалить продукт из списка.",
                        style = TextStyle(fontSize = 14.sp, color = Color.Black),
                        modifier = Modifier.weight(1f)
                    )
                }

                /* -------- Список покупок -------- */
                sectionTitle("Список покупок")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "• Для пополнения списка нажмите на иконку ",
                        style = TextStyle(fontSize = 14.sp, color = Color.Black)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.plusik),
                        contentDescription = "Плюсик",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = ".",
                        style = TextStyle(fontSize = 14.sp, color = Color.Black)
                    )
                }
                bodyText("• Отметьте продукт — он будет вычеркнут из вашего списка.")
                Image(
                    painter = painterResource(id = R.drawable.spisokpokupok),
                    contentDescription = "Список покупок",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                )

                /* -------- Рецепты -------- */
                sectionTitle("Рецепты")
                bodyText(
                    "Для генерации используются продукты из вашего холодильника (преимущественно с меньшим сроком годности).\n" +
                            "Выберите тип блюда (завтрак/обед/ужин), режим (обычный/ПП) и количество рецептов.\n" +
                            "Нажмите “Сгенерировать рецепт”. *Не забудьте сохранить рецепт*."
                )

                /* -------- Сохраненные рецепты -------- */
                sectionTitle("Сохраненные рецепты")
                bodyText("Сохраненный рецепт можно посмотреть во вкладке “Мои рецепты”.")
                Image(
                    painter = painterResource(id = R.drawable.rezepti),
                    contentDescription = "Рецепты",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                )

                /* -------- Профиль -------- */
                sectionTitle("Возможности профиля")
                bodyText(
                    "Во вкладке “Профиль” вы можете выбрать фотографию и имя пользователя. " +
                            "Именно эти данные будут видеть другие пользователи.\n" +
                            "Также вы можете настроить время уведомлений — чтобы не пропустить просрочку."
                )
                Image(
                    painter = painterResource(id = R.drawable.profil),
                    contentDescription = "Профиль",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                )

                /* -------- Семейный доступ -------- */
                sectionTitle("Семейный доступ")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.podelizasemya),
                        contentDescription = "Поделиться с семьей",
                        modifier = Modifier
                            .size(width = 141.dp, height = 293.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "Во вкладке “Моя семья” есть возможность создать семейную группу и " +
                                "делиться продуктами из холодильника, списком покупок и сгенерированными рецептами.",
                        style = TextStyle(fontSize = 14.sp, color = Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(80.dp))
            }
        }

        /* -------- Кнопка «Связаться с нами» -------- */
        Button(
            onClick = onContactClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)
        ) {
            Text(text = "Связаться с нами", color = Color.White)
        }
    }
}
