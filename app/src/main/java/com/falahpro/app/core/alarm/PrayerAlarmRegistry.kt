package com.falahpro.app.core.alarm

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.alarmRegistryStore by preferencesDataStore(name = "prayer_alarm_registry")

/**
 * Persists the engine's view of scheduled alarms for verification on next boot.
 */
class PrayerAlarmRegistry(private val context: Context) {

    private val registryKey = stringPreferencesKey("alarm_registry_v1")

    suspend fun load(): Map<Int, ExpectedAlarm> {
        val raw = context.alarmRegistryStore.data.first()[registryKey] ?: return emptyMap()
        return parse(raw)
    }

    suspend fun save(alarms: List<ExpectedAlarm>) {
        val serialized = alarms.joinToString(";") { alarm ->
            "${alarm.requestCode}|${alarm.prayerName}|${alarm.dayOffset}|${alarm.triggerAtMillis}"
        }
        context.alarmRegistryStore.edit { prefs ->
            prefs[registryKey] = serialized
        }
    }

    suspend fun clear() {
        context.alarmRegistryStore.edit { prefs ->
            prefs.remove(registryKey)
        }
    }

    private fun parse(raw: String): Map<Int, ExpectedAlarm> {
        if (raw.isBlank()) return emptyMap()
        return raw.split(";").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size != 4) return@mapNotNull null
            val requestCode = parts[0].toIntOrNull() ?: return@mapNotNull null
            val prayerName = parts[1]
            val dayOffset = parts[2].toIntOrNull() ?: return@mapNotNull null
            val triggerAt = parts[3].toLongOrNull() ?: return@mapNotNull null
            requestCode to ExpectedAlarm(prayerName, dayOffset, requestCode, triggerAt)
        }.toMap()
    }

    companion object {
        @Volatile
        private var instance: PrayerAlarmRegistry? = null

        fun getInstance(context: Context): PrayerAlarmRegistry {
            return instance ?: synchronized(this) {
                instance ?: PrayerAlarmRegistry(context.applicationContext).also { instance = it }
            }
        }
    }
}
