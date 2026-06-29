package com.zikrmate.app.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zikrmate.app.core.prayer.PrayerRepository
import com.zikrmate.app.core.scheduler.PrayerEngine
import com.zikrmate.app.core.util.PrayerLog
import com.zikrmate.app.data.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Reschedules alarms at midnight and resets daily prayer tracker state.
 */
class DateChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_DATE_CHANGED) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                PrayerLog.dateChanged()
                DataStoreManager.checkAndResetIfNewDay(context)
                PrayerRepository.getInstance(context).invalidateCache()
                PrayerEngine.rescheduleAllSync(context, reason = "date_changed")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
