package com.falahpro.app

import android.app.Application
import com.falahpro.app.core.scheduler.PrayerEngine
import com.falahpro.app.core.scheduler.PrayerRescheduleWorker
import com.falahpro.app.core.util.PrayerLog

/**
 * Application entry point — bootstraps the prayer engine without requiring UI navigation.
 */
class FalahProApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PrayerLog.engineBoot()
        PrayerRescheduleWorker.schedule(this)
        PrayerEngine.bootstrap(this)
    }
}
