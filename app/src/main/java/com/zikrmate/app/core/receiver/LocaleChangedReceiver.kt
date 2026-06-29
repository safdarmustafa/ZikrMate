package com.zikrmate.app.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zikrmate.app.core.scheduler.PrayerEngine
import com.zikrmate.app.core.util.PrayerLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Re-geocodes city name when device locale changes (no prayer recalculation).
 */
class LocaleChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_LOCALE_CHANGED) return

        PrayerLog.localeChanged()
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                PrayerEngine.onLocaleChanged(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
