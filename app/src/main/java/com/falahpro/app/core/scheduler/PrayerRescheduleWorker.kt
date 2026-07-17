package com.falahpro.app.core.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.falahpro.app.core.util.PrayerLog
import java.util.concurrent.TimeUnit

/**
 * Safety-net reschedule — NOT used for exact prayer timing.
 * Recovers from missed midnight rollover when the process was killed.
 */
class PrayerRescheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        PrayerLog.rescheduleStarted("workmanager_safety_net")
        PrayerEngine.rescheduleAllSync(applicationContext, reason = "workmanager_safety_net")
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "prayer_reschedule_safety_net"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PrayerRescheduleWorker>(
                12, TimeUnit.HOURS
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
