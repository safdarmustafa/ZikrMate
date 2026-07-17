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
 * Reschedules all prayer alarms after device reboot.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        PrayerLog.bootCompleted()
        PrayerLog.rescheduleStarted("boot_completed")
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                PrayerRepository.getInstance(context).recordBootEvent()
                PrayerEngine.rescheduleAllSync(context, reason = "boot_completed")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
