package com.zikrmate.app.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zikrmate.app.core.prayer.PrayerRepository
import com.zikrmate.app.core.scheduler.PrayerEngine
import com.zikrmate.app.core.util.PrayerLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Reschedules alarms when the device timezone changes.
 */
class TimezoneChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_TIMEZONE_CHANGED) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                PrayerLog.timezoneChanged()
                PrayerRepository.getInstance(context).invalidateCache()
                PrayerEngine.rescheduleAllSync(context, reason = "timezone_changed")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
