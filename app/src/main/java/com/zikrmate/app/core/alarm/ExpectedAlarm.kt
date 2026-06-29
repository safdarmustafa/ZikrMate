package com.zikrmate.app.core.alarm

/**
 * A prayer alarm the engine expects AlarmManager to hold.
 */
data class ExpectedAlarm(
    val prayerName: String,
    val dayOffset: Int,
    val requestCode: Int,
    val triggerAtMillis: Long
)
