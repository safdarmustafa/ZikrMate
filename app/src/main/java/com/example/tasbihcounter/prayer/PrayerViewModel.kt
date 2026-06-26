package com.example.tasbihcounter.prayer

import android.app.Application
import android.location.Geocoder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.tasbihcounter.util.toLocalTimeSafe
import java.time.LocalTime
import java.util.Locale
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    var prayerTimes by mutableStateOf<Map<String, LocalTime>>(emptyMap())
        private set

    var cityName by mutableStateOf("Detecting location...")
        private set

    init {
        // Don't use a fixed fallback (e.g. Delhi); wait for real location so UI shows "Detecting location..."
    }

    fun updatePrayerTimes(lat: Double, lng: Double) {

        val adhanTimes =
            PrayerTimeHelper.getTodayPrayerTimes(lat, lng)

        prayerTimes = mapOf(
            "Fajr" to adhanTimes.fajr.toLocalTimeSafe(),
            "Dhuhr" to adhanTimes.dhuhr.toLocalTimeSafe(),
            "Asr" to adhanTimes.asr.toLocalTimeSafe(),
            "Maghrib" to adhanTimes.maghrib.toLocalTimeSafe(),
            "Isha" to adhanTimes.isha.toLocalTimeSafe()
        )

        updateCityName(lat, lng)
    }

    private fun updateCityName(lat: Double, lng: Double) {

        try {
            val geocoder = Geocoder(
                getApplication(),
                Locale.getDefault()
            )

            val addresses = geocoder.getFromLocation(lat, lng, 1)

            if (!addresses.isNullOrEmpty()) {

                val locality = addresses[0].locality
                val country = addresses[0].countryName

                cityName = when {
                    locality != null && country != null ->
                        "$locality, $country"

                    locality != null ->
                        locality

                    else ->
                        "Unknown City"
                }
            } else {
                cityName = "Unknown City"
            }

        } catch (e: Exception) {
            cityName = "Unknown City"
        }
    }
}
