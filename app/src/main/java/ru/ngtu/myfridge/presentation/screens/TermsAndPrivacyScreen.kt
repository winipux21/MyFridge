package ru.ngtu.myfridge.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ngtu.myfridge.ui.theme.Purple40

@Composable
fun TermsAndPrivacyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Обработка системной кнопки "Назад"
    BackHandler(enabled = true) {
        onBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Кастомный навбар
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBack)
                .padding(vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Условия и Политика конфиденциальности",
                style = TextStyle(fontSize = 24.sp, color = Purple40)
            )
        }

        // Прокручиваемый текст
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Центрируем и делаем чёрным
                Text(
                    text = "Добро пожаловать в приложение MyFridge! Настоящий документ регулирует условия использования приложения и объясняет, как мы собираем, храним и защищаем ваши данные. Используя MyFridge, вы соглашаетесь с этой политикой.",
                    style = TextStyle(fontSize = 14.sp, color = Color.Black,fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Заголовок фиолетовый
                Text(
                    text = "Какие данные мы собираем?",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Purple40),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Информация при регистрации:",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
                Text(
                    text = "• При создании аккаунта через email: имя, адрес электронной почты, пароль.\n" +
                            "• При входе через Google: имя, email, фото профиля (опционально).",
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Данные о продуктах:",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
                Text(
                    text = "• Названия, сроки годности, количество, категории добавленных продуктов.",
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Технические данные:",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
                Text(
                    text = "• Версия ОС, тип устройства, логи действий (для анализа ошибок и улучшения сервиса).",
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Следующий раздел
                Text(
                    text = "Как мы используем ваши данные?",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Purple40),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "• Уведомления о сроке годности продуктов.\n" +
                            "• Синхронизация данных между устройствами.\n" +
                            "• Формирование списка покупок.\n" +
                            "• Анализ анонимизированной статистики использования.\n" +
                            "• Отправка важных уведомлений (например, об изменении политики).",
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Хранение и защита данных",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Purple40),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "• Все данные хранятся на защищённых серверах с использованием шифрования (SSL/TLS).\n" +
                            "• Доступ к вашей личной информации имеют только сотрудники, необходимые для технической поддержки.\n" +
                            "• Пароли хранятся в хешированном виде.",
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Передача данных третьим лицам",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Purple40),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "• Мы не передаём ваши персональные данные рекламодателям или сторонним сервисам без вашего согласия.\n" +
                            "• Исключения:\n" +
                            "   – Интеграции с сервисами, указанными в приложении (например, Google Sign-In).\n" +
                            "   – Требования закона (по решению суда или государственных органов).",
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ваши права",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Purple40),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "• Доступ и изменение данных: редактируйте профиль в разделе «Настройки аккаунта».\n" +
                            "• Удаление аккаунта: напишите в поддержку или используйте опцию в приложении (данные удаляются в течение 30 дней).\n" +
                            "• Отказ от рассылки: отпишитесь от уведомлений в настройках.",
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Условия использования",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Purple40),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "• Приложение для пользователей старше 13 лет.\n" +
                            "• Запрещено использовать MyFridge для незаконных целей, взламывать или распространять вредоносный код, нарушать авторские права.\n" +
                            "• Мы не несем ответственности за неточности в сроках годности, указанных пользователем.",
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Изменения в политике",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Purple40),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "• Мы можем обновлять этот документ. О значимых изменениях вы получите уведомление в приложении или на email.\n" +
                            "• Актуальная версия всегда доступна в разделе «Условия и политика».",
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
