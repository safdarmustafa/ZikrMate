package com.falahpro.app.core.util

/**
 * In-memory runtime state for diagnostics (process-local).
 */
object PrayerRuntimeState {

    @Volatile var foregroundServiceRunning: Boolean = false

    @Volatile var wakeLockHeld: Boolean = false

    @Volatile var mediaPlayerActive: Boolean = false

    @Volatile var audioFocusHeld: Boolean = false

    @Volatile var lastNotificationPrayer: String? = null

    @Volatile var lastNotificationAtMillis: Long = 0L

    @Volatile var lastAzanAtMillis: Long = 0L

    @Volatile var lastReceiverPrayer: String? = null

    @Volatile var lastReceiverAtMillis: Long = 0L

    fun snapshot(): RuntimeSnapshot = RuntimeSnapshot(
        foregroundServiceRunning = foregroundServiceRunning,
        wakeLockHeld = wakeLockHeld,
        mediaPlayerActive = mediaPlayerActive,
        audioFocusHeld = audioFocusHeld,
        lastNotificationPrayer = lastNotificationPrayer,
        lastNotificationAtMillis = lastNotificationAtMillis,
        lastAzanAtMillis = lastAzanAtMillis,
        lastReceiverPrayer = lastReceiverPrayer,
        lastReceiverAtMillis = lastReceiverAtMillis
    )

    data class RuntimeSnapshot(
        val foregroundServiceRunning: Boolean,
        val wakeLockHeld: Boolean,
        val mediaPlayerActive: Boolean,
        val audioFocusHeld: Boolean,
        val lastNotificationPrayer: String?,
        val lastNotificationAtMillis: Long,
        val lastAzanAtMillis: Long,
        val lastReceiverPrayer: String?,
        val lastReceiverAtMillis: Long
    )
}
