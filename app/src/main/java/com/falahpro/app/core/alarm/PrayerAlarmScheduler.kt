package com.falahpro.app.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.falahpro.app.core.receiver.PrayerAlarmReceiver
import com.falahpro.app.core.util.PrayerConstants
import com.falahpro.app.core.util.PrayerLog

/**
 * Schedules exact prayer alarms via AlarmManager.
 * Uses upsert scheduling — never cancels all alarms at once during normal sync.
 */
class PrayerAlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * True if a matching PendingIntent token exists.
     * Does not guarantee AlarmManager still holds a live alarm.
     */
    fun isAlarmPending(prayerName: String, dayOffset: Int): Boolean {
        return getExistingPendingIntent(prayerName, dayOffset) != null
    }

    fun scheduleExactAlarm(
        prayerName: String,
        triggerAtMillis: Long,
        dayOffset: Int
    ): Boolean {
        if (!canScheduleExactAlarms()) {
            PrayerLog.exactAlarmDenied()
            return false
        }

        val requestCode = PrayerConstants.requestCodeFor(prayerName, dayOffset)
        val pendingIntent = createPendingIntent(prayerName, triggerAtMillis, dayOffset, requestCode)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        PrayerLog.alarmScheduled(prayerName, triggerAtMillis, dayOffset, requestCode)
        return true
    }

    fun cancelAlarm(prayerName: String, dayOffset: Int) {
        val requestCode = PrayerConstants.requestCodeFor(prayerName, dayOffset)
        val existing = getExistingPendingIntent(prayerName, dayOffset)
        if (existing != null) {
            alarmManager.cancel(existing)
            existing.cancel()
            PrayerLog.alarmCancelled(prayerName, dayOffset, requestCode)
        }
    }

    /** Cancels only slots not in [keepRequestCodes] — never touches alarms we intend to keep. */
    fun cancelAlarmsExcept(keepRequestCodes: Set<Int>) {
        PrayerConstants.allAlarmSlots().forEach { (prayer, dayOffset) ->
            val code = PrayerConstants.requestCodeFor(prayer, dayOffset)
            if (code !in keepRequestCodes) {
                cancelAlarm(prayer, dayOffset)
            }
        }
    }

    private fun createPendingIntent(
        prayerName: String,
        triggerAtMillis: Long,
        dayOffset: Int,
        requestCode: Int
    ): PendingIntent {
        val intent = alarmIntent(prayerName, triggerAtMillis, dayOffset)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getExistingPendingIntent(prayerName: String, dayOffset: Int): PendingIntent? {
        val requestCode = PrayerConstants.requestCodeFor(prayerName, dayOffset)
        val intent = alarmIntent(prayerName, triggerAtMillis = 0L, dayOffset)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun alarmIntent(prayerName: String, triggerAtMillis: Long, dayOffset: Int): Intent =
        Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerConstants.ACTION_PRAYER_ALARM
            putExtra(PrayerConstants.EXTRA_PRAYER_NAME, prayerName)
            putExtra(PrayerConstants.EXTRA_TRIGGER_AT_MILLIS, triggerAtMillis)
            putExtra(PrayerConstants.EXTRA_DAY_OFFSET, dayOffset)
        }

    companion object {
        @Volatile
        private var instance: PrayerAlarmScheduler? = null

        fun getInstance(context: Context): PrayerAlarmScheduler {
            return instance ?: synchronized(this) {
                instance ?: PrayerAlarmScheduler(context.applicationContext).also { instance = it }
            }
        }
    }
}
