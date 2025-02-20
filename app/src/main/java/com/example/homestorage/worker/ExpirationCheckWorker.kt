package com.example.homestorage.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.homestorage.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ExpirationCheckWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val currentTime = System.currentTimeMillis()

            // 1. 取出所有物品
            val allItems = db.itemDao().getAllItems().first() // 这里你可改成一次性查询

            // 2. 根据 reminderDays 和 expirationDate 进行筛选
            val itemsToNotify = allItems.filter { item ->
                // 如果没有设置 expirationDate 或 reminderDays，跳过
                item.expirationDate != null && item.reminderDays != null
                        // 判断是否在提醒区间内（未过期且到期日 - 当前时间 <= 提醒天数 * 1天毫秒数）
                        && (item.expirationDate > currentTime)
                        && ((item.expirationDate - currentTime) <= item.reminderDays * 24 * 60 * 60 * 1000)
            }

            if (itemsToNotify.isNotEmpty()) {
                val names = itemsToNotify.joinToString(separator = ", ") { it.name }
                val contentText = "以下物品即将到期：$names"

                val notification = NotificationCompat.Builder(applicationContext, "expiration_channel")
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("有效期提醒")
                    .setContentText(contentText)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()

                // 检查通知权限
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(applicationContext).notify(1002, notification)
                }
            }
            Result.success()
        }
    }
}

