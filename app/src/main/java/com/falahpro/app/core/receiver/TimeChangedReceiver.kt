package com.falahpro.app.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.falahpro.app.core.prayer.PrayerRepository
import com.falahpro.app.core.scheduler.PrayerEngine
import com.falahpro.app.core.util.PrayerLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Reschedules alarms when the user manually changes the device clock.
 */
class TimeChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_TIME_CHANGED &&
            intent?.action != "android.intent.action.TIME_SET"
        ) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                PrayerLog.timeChanged()
                PrayerRepository.getInstance(context).invalidateCache()
                PrayerEngine.rescheduleAllSync(context, reason = "time_changed")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
