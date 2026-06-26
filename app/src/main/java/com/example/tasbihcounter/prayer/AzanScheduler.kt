package com.example.tasbihcounter.prayer

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object AzanScheduler {

    fun schedulePrayer(
        context: Context,
        prayerName: String,
        prayerTime: LocalDateTime
    ) {

        val delay = Duration.between(
            LocalDateTime.now(),
            prayerTime
        ).toMillis()

        if (delay <= 0) return

        val data = androidx.work.workDataOf("prayer" to prayerName)

        val request = OneTimeWorkRequestBuilder<AzanWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
