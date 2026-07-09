package com.zikrmate.app.core.prayer

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import com.zikrmate.app.core.util.PrayerConstants
import com.zikrmate.app.core.util.PrayerLog
import com.zikrmate.app.util.toLocalTimeSafe
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.time.ExperimentalTime

/**
 * Computes prayer times for a given date and location.
 * Stateless — caching is handled by [PrayerRepository].
 */
@OptIn(ExperimentalTime::class)
object PrayerCalculator {

    fun calculateForDate(
        latitude: Double,
        longitude: Double,
        date: LocalDate
    ): Map<String, LocalTime> {
        val coordinates = Coordinates(latitude, longitude)
        val dateComponents = DateComponents(date.year, date.monthValue, date.dayOfMonth)
        // Karachi method with Hanafi madhab (Asr when shadow = 2x object length).
        val params = CalculationMethod.KARACHI.parameters.copy(madhab = Madhab.HANAFI)
        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

        val times = mapOf(
            "Fajr" to prayerTimes.fajr.toLocalTimeSafe(),
            "Sunrise" to prayerTimes.sunrise.toLocalTimeSafe(),
            "Dhuhr" to prayerTimes.dhuhr.toLocalTimeSafe(),
            "Asr" to prayerTimes.asr.toLocalTimeSafe(),
            "Maghrib" to prayerTimes.maghrib.toLocalTimeSafe(),
            "Isha" to prayerTimes.isha.toLocalTimeSafe()
        )

        times.forEach { (name, time) ->
            PrayerLog.prayerCalculated(name, time.toString(), date.toString())
        }

        return times
    }

    fun toDateTime(date: LocalDate, time: LocalTime): LocalDateTime =
        LocalDateTime.of(date, time)

    fun findNextPrayer(times: Map<String, LocalTime>, now: LocalTime = LocalTime.now()): Pair<String, LocalTime>? {
        for (name in PrayerConstants.PRAYER_NAMES) {
            val time = times[name] ?: continue
            if (now.isBefore(time)) return name to time
        }
        val fajr = times["Fajr"] ?: return null
        return "Fajr" to fajr
    }
}
