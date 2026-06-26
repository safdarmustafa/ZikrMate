package com.zikrmate.app.prayer

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import java.util.Calendar

object PrayerTimeHelper {

    fun getTodayPrayerTimes(
        latitude: Double,
        longitude: Double
    ): PrayerTimes {

        val coordinates = Coordinates(latitude, longitude)

        val calendar = Calendar.getInstance()

        val dateComponents = DateComponents(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val params = CalculationMethod.KARACHI.parameters

        return PrayerTimes(coordinates, dateComponents, params)
    }
}
