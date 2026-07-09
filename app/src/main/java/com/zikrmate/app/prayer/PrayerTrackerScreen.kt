package com.zikrmate.app.prayer

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zikrmate.app.core.notification.PrayerNotificationManager
import com.zikrmate.app.core.scheduler.PrayerEngine
import com.zikrmate.app.core.util.PrayerConstants
import com.zikrmate.app.data.AzanMode
import com.zikrmate.app.data.DataStoreManager
import com.zikrmate.app.ZikrMate
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

/**
 * Activity-scoped visual effects that keep running while the user is on other tabs,
 * so background and pulse animations never restart from their initial values on return.
 */
@Stable
class PrayerVisualEffectsState {
    var bgOffset by mutableFloatStateOf(0f)
        private set
    var pulseScale by mutableFloatStateOf(0.7f)
        private set

    internal fun setBgOffset(value: Float) {
        bgOffset = value
    }

    internal fun setPulseScale(value: Float) {
        pulseScale = value
    }
}

@Composable
fun rememberPrayerVisualEffects(): PrayerVisualEffectsState {
    val state = remember { PrayerVisualEffectsState() }
    LaunchedEffect(state) {
        coroutineScope {
            launch {
                val anim = Animatable(state.bgOffset)
                while (true) {
                    anim.animateTo(700f, tween(20_000, easing = LinearEasing)) {
                        state.setBgOffset(value)
                    }
                    anim.animateTo(0f, tween(20_000, easing = LinearEasing)) {
                        state.setBgOffset(value)
                    }
                }
            }
            launch {
                val anim = Animatable(state.pulseScale)
                while (true) {
                    anim.animateTo(1.2f, tween(2_000, easing = LinearEasing)) {
                        state.setPulseScale(value)
                    }
                    anim.animateTo(0.7f, tween(2_000, easing = LinearEasing)) {
                        state.setPulseScale(value)
                    }
                }
            }
        }
    }
    return state
}

@Composable
fun PremiumBackground(
    bgOffset: Float,
    content: @Composable () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF2B1C17), Color(0xFF140C09)),
                    center = Offset(bgOffset, bgOffset),
                    radius = 1200f
                )
            )
    ) {
        content()
    }
}

/**
 * Passive observer of [PrayerViewModel.uiState].
 * The ViewModel is activity-scoped so tab switches do not recreate state or restart loaders.
 */
@Composable
fun PrayerTrackerScreen(
    viewModel: PrayerViewModel,
    visualEffects: PrayerVisualEffectsState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val permissionLaunched by viewModel.permissionRequestLaunched.collectAsState()

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

    if (!hasLocationPermission && !permissionLaunched) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            viewModel.markPermissionRequestLaunched()
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Refresh prayer times/city from live GPS whenever we have permission (initial or just granted).
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            PrayerEngine.syncLocationIfPermitted(context)
        }
    }

    val prayers = PrayerConstants.PRAYER_NAMES
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")

    val displayHours = uiState.remainingSeconds / 3600
    val displayMinutes = (uiState.remainingSeconds % 3600) / 60
    val displaySecs = uiState.remainingSeconds % 60

    val progress = uiState.completedCount / 5f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "progress"
    )

    val azanMode by DataStoreManager
        .getAzanMode(context)
        .collectAsState(initial = AzanMode.SILENT)

    val scrollState = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }

    PremiumBackground(bgOffset = visualEffects.bgOffset) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrayerReliabilityBanner()
                Spacer(Modifier.height(12.dp))

                val pulse = visualEffects.pulseScale

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
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
                                    listOf(Color(0xFF3E2A24), Color(0xFF1A120F))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            uiState.currentTime.format(formatter),
                            fontSize = 20.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Light
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.cityName,
                        fontSize = 14.sp,
                        color = Color.White.copy(0.7f)
                    )

                    if (!hasLocationPermission) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Allow location for more accurate times",
                            fontSize = 11.sp,
                            color = Color.White.copy(0.45f)
                        )
                    }

                    uiState.sunriseTime?.let { sunrise ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Sunrise ${sunrise.format(formatter)}",
                            fontSize = 11.sp,
                            color = Color.White.copy(0.45f)
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Next Prayer",
                        fontSize = 12.sp,
                        color = Color.White.copy(0.5f)
                    )
                    Spacer(Modifier.height(6.dp))

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
                                PrayerNotificationManager.getInstance(context)
                                    .updateChannelsForMode(nextMode)
                                PrayerEngine.rescheduleAll(context, reason = "azan_mode_changed")
                            }
                        }
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        uiState.nextPrayerName.uppercase(),
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
                        color = Color.White.copy(0.7f)
                    )
                }

                Column {
                    prayers.chunked(2).forEach { rowPrayers ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowPrayers.forEach { prayer ->
                                val isCompleted = uiState.prayerStates[prayer] == true
                                val time = uiState.prayerTimes[prayer]

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
                                            val newValue = !(uiState.prayerStates[prayer] ?: false)
                                            viewModel.setPrayerCompleted(prayer, newValue)
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
                                                    if (isCompleted) Color(0xFFE2C07A)
                                                    else Color.White.copy(0.25f),
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

@Composable
fun rememberPrayerViewModel(): PrayerViewModel {
    val activity = LocalContext.current as ZikrMate
    return viewModel(viewModelStoreOwner = activity)
}
