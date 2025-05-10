package ru.ngtu.myfridge.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import ru.ngtu.myfridge.R
import ru.ngtu.myfridge.data.db.AppDatabase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ExpiryCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(applicationContext)
        val products = database.productDao().getAllProducts().first()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "expiry_channel"

        // Создание канала уведомлений
        val channel = NotificationChannel(
            channelId,
            "Уведомления о сроке годности",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        // Группировка продуктов по категориям
        val expiredProducts = mutableListOf<String>()
        val expiresTomorrow = mutableListOf<String>()
        val expiresInThreeDays = mutableListOf<String>()

        products.forEach { product ->
            product.expiryDate?.let { expiryDate ->
                val daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate).toInt()
                when {
                    daysUntilExpiry <= 0 -> expiredProducts.add(product.name ?: "Без названия")
                    daysUntilExpiry == 1 -> expiresTomorrow.add(product.name ?: "Без названия")
                    daysUntilExpiry == 3 -> expiresInThreeDays.add(product.name ?: "Без названия")
                }
            }
        }

        // Если нет продуктов для уведомления, выходим
        if (expiredProducts.isEmpty() && expiresTomorrow.isEmpty() && expiresInThreeDays.isEmpty()) {
            return@withContext Result.success()
        }

        // Создание основного уведомления
        val summaryNotification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Сроки годности продуктов")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Используем InboxStyle для группировки
        val inboxStyle = NotificationCompat.InboxStyle()
        var totalCount = 0

        if (expiredProducts.isNotEmpty()) {
            inboxStyle.addLine("Просроченные продукты (${expiredProducts.size}):")
            expiredProducts.take(3).forEach { inboxStyle.addLine(" - $it") } // Показываем до 3 продуктов
            totalCount += expiredProducts.size
        }
        if (expiresTomorrow.isNotEmpty()) {
            inboxStyle.addLine("Истекает завтра (${expiresTomorrow.size}):")
            expiresTomorrow.take(3).forEach { inboxStyle.addLine(" - $it") }
            totalCount += expiresTomorrow.size
        }
        if (expiresInThreeDays.isNotEmpty()) {
            inboxStyle.addLine("Истекает через 3 дня (${expiresInThreeDays.size}):")
            expiresInThreeDays.take(3).forEach { inboxStyle.addLine(" - $it") }
            totalCount += expiresInThreeDays.size
        }

        // Настройка текста уведомления
        summaryNotification.setStyle(inboxStyle)
            .setContentText("Проверьте $totalCount продуктов в холодильнике")
            .setNumber(totalCount) // Показывает общее количество в системной строке

        // Отправка уведомления
        val notificationId = 1 // Фиксированный ID для обновления одного уведомления
        notificationManager.notify(notificationId, summaryNotification.build())

        Result.success()
    }
}