package ru.ngtu.myfridge.utils

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ru.ngtu.myfridge.workers.ExpiryCheckWorker
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

// Функция планирования уведомлений на выбранное время
fun scheduleExpiryCheck(context: Context) {
    val (hour, minute) = getNotificationTime(context) // Получаем сохранённое время (по умолчанию 8:00)
    val currentTime = LocalDateTime.now()
    val notificationTime = LocalDateTime.now()
        .withHour(hour)
        .withMinute(minute)
        .withSecond(0)
        .withNano(0)

    // Вычисляем задержку до первого запуска
    val delay = if (currentTime.isBefore(notificationTime)) {
        ChronoUnit.MILLIS.between(currentTime, notificationTime)
    } else {
        ChronoUnit.MILLIS.between(currentTime, notificationTime.plusDays(1))
    }

    val workRequest = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "expiryCheck",
        ExistingPeriodicWorkPolicy.REPLACE, // Заменяем старое расписание, если оно было
        workRequest
    )
    Log.d("NotificationUtils", "ExpiryCheckWorker запланирован с начальной задержкой $delay мс")
}

// Функция получения времени уведомлений из SharedPreferences
private fun getNotificationTime(context: Context): Pair<Int, Int> {
    val sharedPreferences = context.getSharedPreferences("MyFridgePrefs", Context.MODE_PRIVATE)
    val hour = sharedPreferences.getInt("notificationHour", 8) // По умолчанию 8:00
    val minute = sharedPreferences.getInt("notificationMinute", 0)
    return Pair(hour, minute)
}