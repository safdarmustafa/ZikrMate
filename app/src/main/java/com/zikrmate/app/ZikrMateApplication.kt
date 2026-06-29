package com.zikrmate.app

import android.app.Application
import com.zikrmate.app.core.scheduler.PrayerEngine
import com.zikrmate.app.core.scheduler.PrayerRescheduleWorker
import com.zikrmate.app.core.util.PrayerLog

/**
 * Application entry point — bootstraps the prayer engine without requiring UI navigation.
 */
class ZikrMateApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PrayerLog.engineBoot()
        PrayerRescheduleWorker.schedule(this)
        PrayerEngine.bootstrap(this)
    }
}
