package com.zikrmate.app.prayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zikrmate.app.core.prayer.PrayerRepository
import com.zikrmate.app.core.util.PrayerConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime

data class PrayerUiState(
    val prayerTimes: Map<String, LocalTime> = emptyMap(),
    val sunriseTime: LocalTime? = null,
    val cityName: String = "—",
    val currentTime: LocalTime = LocalTime.now(),
    val prayerStates: Map<String, Boolean> = PrayerConstants.PRAYER_NAMES.associateWith { false },
    val nextPrayerName: String = "—",
    val remainingSeconds: Int = 0,
    val completedCount: Int = 0,
    val hasCache: Boolean = false
)

/**
 * Long-lived display ViewModel. Loads cache once at creation; ticks the clock in the background.
 * Screens observe [uiState] only — no reload on navigation.
 */
class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PrayerRepository.getInstance(application)

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    private val _permissionRequestLaunched = MutableStateFlow(false)
    val permissionRequestLaunched: StateFlow<Boolean> = _permissionRequestLaunched.asStateFlow()

    init {
        viewModelScope.launch {
            loadDisplayCacheOnce()
        }
        viewModelScope.launch {
            observePrayerCompletionStates()
        }
        viewModelScope.launch {
            runClock()
        }
    }

    fun markPermissionRequestLaunched() {
        _permissionRequestLaunched.value = true
    }

    fun setPrayerCompleted(prayer: String, completed: Boolean) {
        val updated = _uiState.value.prayerStates.toMutableMap().apply {
            this[prayer] = completed
        }
        val current = _uiState.value
        _uiState.value = buildUiState(
            prayerTimes = current.prayerTimes,
            sunriseTime = current.sunriseTime,
            cityName = current.cityName,
            currentTime = current.currentTime,
            prayerStates = updated,
            hasCache = current.hasCache
        )
    }

    private suspend fun loadDisplayCacheOnce() {
        if (_uiState.value.hasCache) return

        val cached = repository.getTodayCachedTimes()
        val partial = if (cached != null) null else repository.getTodayCache()
        val times = cached
            ?: partial?.times?.filterKeys { PrayerConstants.PRAYER_NAMES.contains(it) }
            ?: emptyMap()
        val sunrise = cached?.get("Sunrise") ?: partial?.times?.get("Sunrise")
        val city = repository.getCachedCityName() ?: partial?.cityName ?: "—"

        if (times.isNotEmpty() || city != "—") {
            val now = LocalTime.now()
            _uiState.value = buildUiState(
                prayerTimes = times,
                sunriseTime = sunrise,
                cityName = city,
                currentTime = now,
                prayerStates = _uiState.value.prayerStates,
                hasCache = times.isNotEmpty()
            )
        }
    }

    private suspend fun observePrayerCompletionStates() {
        val names = PrayerConstants.PRAYER_NAMES
        val flows = names.map { prayer ->
            com.zikrmate.app.data.DataStoreManager.getPrayerState(getApplication(), prayer)
        }
        combine(flows) { values ->
            names.zip(values.map { it as Boolean }).toMap()
        }.collect { states ->
            val current = _uiState.value
            _uiState.value = buildUiState(
                prayerTimes = current.prayerTimes,
                sunriseTime = current.sunriseTime,
                cityName = current.cityName,
                currentTime = current.currentTime,
                prayerStates = states,
                hasCache = current.hasCache
            )
        }
    }

    private suspend fun runClock() {
        while (true) {
            val now = LocalTime.now()
            val current = _uiState.value
            _uiState.value = buildUiState(
                prayerTimes = current.prayerTimes,
                sunriseTime = current.sunriseTime,
                cityName = current.cityName,
                currentTime = now,
                prayerStates = current.prayerStates,
                hasCache = current.hasCache
            )
            delay(1_000)
        }
    }

    private fun buildUiState(
        prayerTimes: Map<String, LocalTime>,
        sunriseTime: LocalTime?,
        cityName: String,
        currentTime: LocalTime,
        prayerStates: Map<String, Boolean>,
        hasCache: Boolean
    ): PrayerUiState {
        val nextName = if (prayerTimes.isNotEmpty()) {
            prayerTimes.keys.firstOrNull { name ->
                prayerTimes[name]?.let { currentTime.isBefore(it) } == true
            } ?: "Fajr"
        } else {
            "—"
        }
        val nextTime = prayerTimes[nextName] ?: currentTime
        val nowSec = currentTime.toSecondOfDay()
        val nextSec = nextTime.toSecondOfDay()
        val remaining = if (nextSec > nowSec) nextSec - nowSec
        else (24 * 3600 - nowSec) + nextSec

        return PrayerUiState(
            prayerTimes = prayerTimes,
            sunriseTime = sunriseTime,
            cityName = cityName,
            currentTime = currentTime,
            prayerStates = prayerStates,
            nextPrayerName = nextName,
            remainingSeconds = remaining,
            completedCount = prayerStates.values.count { it },
            hasCache = hasCache
        )
    }
}
