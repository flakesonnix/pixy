package com.pixelcolor.app.data.notification

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyPuzzleScheduler(private val context: Context) {

    fun scheduleDailyNotification() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelay = calendar.timeInMillis - System.currentTimeMillis()

        val workRequest = PeriodicWorkRequestBuilder<DailyPuzzleWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag("daily_puzzle_notification")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_puzzle_notification",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelDailyNotification() {
        WorkManager.getInstance(context).cancelUniqueWork("daily_puzzle_notification")
    }
}

class DailyPuzzleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val receiver = DailyPuzzleReceiver()
        receiver.onReceive(applicationContext, android.content.Intent())
        return Result.success()
    }
}
