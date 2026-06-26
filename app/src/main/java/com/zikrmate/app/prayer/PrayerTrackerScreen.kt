package com.zikrmate.app.prayer

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.zikrmate.app.data.AzanMode
import com.zikrmate.app.data.DataStoreManager
import com.zikrmate.app.location.getUserLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@Composable
fun PremiumBackground(content: @Composable () -> Unit) {

    val infiniteTransition = rememberInfiniteTransition(label = "")

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 700f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF2B1C17), Color(0xFF140C09)),
                    center = Offset(offset, offset),
                    radius = 1200f
                )
            )
    ) {
        content()
    }
}

@Composable
fun PrayerTrackerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ----- LOCATION PERMISSION -----
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasLocationPermission =
            perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val viewModel: PrayerViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PrayerViewModel(context.applicationContext as Application) as T
            }
        }
    )

    // ----- FETCH PRAYER TIMES (only once we have permission) -----
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val location = getUserLocation(context)
            if (location != null) {
                viewModel.updatePrayerTimes(location.first, location.second)
            } else {
                // Fallback only when location unavailable (e.g. no GPS, emulator without mock)
                // Using Patna, Bihar so Indian users get a sensible default
                viewModel.updatePrayerTimes(25.5941, 85.1376)
            }
        }
    }

    val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

    var prayerStates by remember {
        mutableStateOf(prayers.associateWith { false })
    }

    val times = viewModel.prayerTimes

    LaunchedEffect(times) {
        if (times.isNotEmpty()) {
            times.forEach { (prayerName: String, prayerTime: LocalTime) ->
                val now = LocalDateTime.now()

                val prayerDateTime = now
                    .withHour(prayerTime.hour)
                    .withMinute(prayerTime.minute)
                    .withSecond(0)

                val delayMillis =
                    Duration.between(now, prayerDateTime).toMillis()

                if (delayMillis > 0) {

                    val request =
                        OneTimeWorkRequestBuilder<AzanWorker>()
                            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                            .setInputData(
                                workDataOf("prayer" to prayerName)
                            )
                            .build()

                    WorkManager
                        .getInstance(context)
                        .enqueueUniqueWork(
                            "azan_$prayerName",
                            ExistingWorkPolicy.REPLACE,
                            request
                        )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        prayers.forEach { prayer: String ->
            val saved = DataStoreManager
                .getPrayerState(context, prayer)
                .first()

            prayerStates = prayerStates.toMutableMap().apply {
                this[prayer] = saved
            }
        }
    }

    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = LocalTime.now()
        }
    }

    val formatter = DateTimeFormatter.ofPattern("hh:mm a")

    val nextPrayer = if (times.isNotEmpty()) {
        when {
            currentTime.isBefore(times["Fajr"]!!) -> "Fajr"
            currentTime.isBefore(times["Dhuhr"]!!) -> "Dhuhr"
            currentTime.isBefore(times["Asr"]!!) -> "Asr"
            currentTime.isBefore(times["Maghrib"]!!) -> "Maghrib"
            currentTime.isBefore(times["Isha"]!!) -> "Isha"
            else -> "Fajr"
        }
    } else {
        "Fajr"
    }

    val nextTime = times[nextPrayer] ?: LocalTime.now()

    val nowInSeconds = currentTime.toSecondOfDay()
    val nextInSeconds = nextTime.toSecondOfDay()

    val remainingSeconds =
        if (nextInSeconds > nowInSeconds)
            nextInSeconds - nowInSeconds
        else
            (24 * 3600 - nowInSeconds) + nextInSeconds

    val animatedSeconds by animateIntAsState(
        targetValue = remainingSeconds,
        animationSpec = tween(500),
        label = ""
    )

    val displayHours = animatedSeconds / 3600
    val displayMinutes = (animatedSeconds % 3600) / 60
    val displaySecs = animatedSeconds % 60

    val completedCount = prayerStates.values.count { completed: Boolean -> completed }
    val progress = completedCount / 5f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = ""
    )

    // If we don't have permission yet, show a friendly message instead of crashing
    if (!hasLocationPermission) {
        PremiumBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Allow location to show accurate prayer times.",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
        return
    }

    PremiumBackground {

        Box(
            Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val pulse by rememberInfiniteTransition(label = "")
                    .animateFloat(
                        initialValue = 0.7f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = ""
                    )

                Box(contentAlignment = Alignment.Center) {

                    CircularProgressIndicator(
                        progress = animatedProgress,
                        strokeWidth = 6.dp,
                        color = Color(0xFFE2C07A),
                        trackColor = Color.White.copy(0.08f),
                        modifier = Modifier.size(190.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size((160 * pulse).dp)
                            .background(
                                Color(0xFFE2C07A).copy(alpha = 0.05f),
                                CircleShape
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .shadow(12.dp, CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF3E2A24),
                                        Color(0xFF1A120F)
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            currentTime.format(formatter),
                            fontSize = 20.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Light
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(
                        text = viewModel.cityName,
                        fontSize = 14.sp,
                        color = Color.White.copy(0.7f)
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Next Prayer",
                        fontSize = 12.sp,
                        color = Color.White.copy(0.5f)
                    )

                    Spacer(Modifier.height(6.dp))

                    val azanMode by DataStoreManager
                        .getAzanMode(context)
                        .collectAsState(initial = AzanMode.FULL_SOUND)

                    Text(
                        text = when (azanMode) {
                            AzanMode.FULL_SOUND -> "🔊 Full Azan"
                            AzanMode.NOTIFICATION_ONLY -> "🔔 Notification Only"
                            AzanMode.SILENT -> "🔕 Silent"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFFE2C07A),
                        modifier = Modifier.clickable {
                            scope.launch {
                                val nextMode = when (azanMode) {
                                    AzanMode.SILENT -> AzanMode.FULL_SOUND
                                    AzanMode.FULL_SOUND -> AzanMode.NOTIFICATION_ONLY
                                    AzanMode.NOTIFICATION_ONLY -> AzanMode.SILENT
                                }
                                DataStoreManager.saveAzanMode(context, nextMode)
                            }
                        }
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        nextPrayer.uppercase(),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2C07A)
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "in %02dh %02dm %02ds".format(
                            displayHours,
                            displayMinutes,
                            displaySecs
                        ),
                        fontSize = 14.sp,
                        color = Color.White.copy(0.7f),
                        modifier = Modifier.animateContentSize()
                    )
                }

                Column {
                    prayers.chunked(2).forEach { rowPrayers: List<String> ->

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            rowPrayers.forEach { prayer: String ->

                                val isCompleted = prayerStates[prayer] == true
                                val time = times[prayer]

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(6.dp, RoundedCornerShape(20.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF2A1C17),
                                                    Color(0xFF2A1C17),
                                                    Color.White.copy(alpha = 0.05f)
                                                )
                                            ),
                                            RoundedCornerShape(20.dp)
                                        )
                                        .clickable {
                                            val newValue = !isCompleted
                                            prayerStates =
                                                prayerStates.toMutableMap().apply {
                                                    this[prayer] = newValue
                                                }

                                            scope.launch {
                                                DataStoreManager.savePrayerState(
                                                    context,
                                                    prayer,
                                                    newValue
                                                )
                                            }
                                        }
                                        .padding(20.dp)
                                ) {

                                    Column {

                                        Text(
                                            prayer,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )

                                        Spacer(Modifier.height(4.dp))

                                        Text(
                                            time?.format(formatter) ?: "--:--",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(0.6f)
                                        )

                                        Spacer(Modifier.height(14.dp))

                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(
                                                    if (isCompleted)
                                                        Color(0xFFE2C07A)
                                                    else
                                                        Color.White.copy(0.25f),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }

                            if (rowPrayers.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        Spacer(Modifier.height(18.dp))
                    }
                }
            }
        }
    }
}
