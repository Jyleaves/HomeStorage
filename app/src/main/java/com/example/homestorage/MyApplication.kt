package com.example.homestorage

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.homestorage.worker.ExpirationCheckWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleExpirationCheckWork()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "expiration_channel"
            val channelName = "有效期提醒"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "提醒用户物品即将到期"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleExpirationCheckWork() {
        // 计算下一个零点的时间
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val initialDelay = calendar.timeInMillis - now
        val workRequest = PeriodicWorkRequestBuilder<ExpirationCheckWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expiration_check",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
