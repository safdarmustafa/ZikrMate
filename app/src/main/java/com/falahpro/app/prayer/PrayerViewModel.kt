package com.falahpro.app.prayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.falahpro.app.core.prayer.PrayerRepository
import com.falahpro.app.core.util.PrayerConstants
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
            observeDisplayData()
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

    /**
     * Observes the repository's cached times + city name and keeps the UI in sync.
     * Reacts to location changes (which recalculate times) without needing a manual reload.
     */
    private suspend fun observeDisplayData() {
        repository.observeDisplayData().collect { display ->
            val times = display.times.filterKeys { PrayerConstants.PRAYER_NAMES.contains(it) }
            val current = _uiState.value
            _uiState.value = buildUiState(
                prayerTimes = times,
                sunriseTime = display.sunrise,
                cityName = display.cityName,
                currentTime = current.currentTime,
                prayerStates = current.prayerStates,
                hasCache = times.isNotEmpty()
            )
        }
    }

    private suspend fun observePrayerCompletionStates() {
        val names = PrayerConstants.PRAYER_NAMES
        val flows = names.map { prayer ->
            com.falahpro.app.data.DataStoreManager.getPrayerState(getApplication(), prayer)
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
