package com.example.tasbihcounter.qibla

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.tasbihcounter.R
import com.example.tasbihcounter.location.getUserLocation
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(onBack: () -> Unit) {

    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    if (!hasPermission) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF241612), Color(0xFF140C09))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("Location permission required", color = Color.White)
        }
    } else {
        PremiumCompass()
    }
}

@SuppressLint("MissingPermission")
@Composable
fun PremiumCompass() {

    val context = LocalContext.current
    val sensorManager =
        context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager

    var azimuth by remember { mutableFloatStateOf(0f) }
    var qiblaBearing by remember { mutableFloatStateOf(0f) }
    var hasLocation by remember { mutableStateOf(false) }

    val kaabaLat = 21.4225
    val kaabaLng = 39.8262

    LaunchedEffect(Unit) {
        // Use the same fused-location helper as the prayer screen so Qibla matches your real city (or emulator mock).
        val loc = getUserLocation(context)
        if (loc != null) {
            val (lat, lng) = loc
            qiblaBearing = calculateQiblaDirection(
                lat,
                lng,
                kaabaLat,
                kaabaLng
            )
            hasLocation = true
        } else {
            // Safe fallback: approximate Qibla from India (Delhi-ish) so the compass is still usable.
            val fallbackLat = 28.6139
            val fallbackLng = 77.2090
            qiblaBearing = calculateQiblaDirection(
                fallbackLat,
                fallbackLng,
                kaabaLat,
                kaabaLng
            )
            hasLocation = false
        }
    }

    DisposableEffect(Unit) {

        val accelerometer: Sensor? =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer: Sensor? =
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)

        val listener = object : SensorEventListener {

            override fun onSensorChanged(event: SensorEvent) {

                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        gravity[0] = event.values[0]
                        gravity[1] = event.values[1]
                        gravity[2] = event.values[2]
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        geomagnetic[0] = event.values[0]
                        geomagnetic[1] = event.values[1]
                        geomagnetic[2] = event.values[2]
                    }
                }

                val rotationMatrix = FloatArray(9)
                val orientation = FloatArray(3)

                if (SensorManager.getRotationMatrix(
                        rotationMatrix,
                        null,
                        gravity,
                        geomagnetic
                    )
                ) {
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val azimuthRad = orientation[0]
                    val azimuthDeg =
                        Math.toDegrees(azimuthRad.toDouble()).toFloat()
                    azimuth = (azimuthDeg + 360) % 360
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometer?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        magnetometer?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val animatedCompassRotation by animateFloatAsState(
        targetValue = -azimuth,
        animationSpec = tween(200),
        label = ""
    )

    val kaabaRotation by animateFloatAsState(
        targetValue = qiblaBearing - azimuth,
        animationSpec = tween(200),
        label = ""
    )

    val angleToQibla =
        normalizeAngle(qiblaBearing - azimuth)

    val isAligned = abs(angleToQibla) < 5

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF241612), Color(0xFF140C09))
                )
            )
            .padding(16.dp)
    ) {

        Text(
            text = "Qibla Direction",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE2C07A)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(320.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(320.dp)
                    .graphicsLayer { rotationZ = animatedCompassRotation }
            ) {

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF3E2A24), Color(0xFF1A120F))
                        )
                    )
                }

                Box(Modifier.fillMaxSize()) {

                    Text(
                        "N",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 20.dp),
                        color = Color(0xFFE2C07A),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "S",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = (-20).dp),
                        color = Color(0xFFD0C4BC),
                        fontSize = 18.sp
                    )

                    Text(
                        "E",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = (-20).dp),
                        color = Color(0xFFD0C4BC),
                        fontSize = 18.sp
                    )

                    Text(
                        "W",
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = 20.dp),
                        color = Color(0xFFD0C4BC),
                        fontSize = 18.sp
                    )
                }
            }

            if (qiblaBearing != 0f) {
                Image(
                    painter = painterResource(id = R.drawable.kaaba),
                    contentDescription = "Kaaba",
                    modifier = Modifier
                        .size(if (isAligned) 90.dp else 70.dp)
                        .graphicsLayer { rotationZ = kaabaRotation }
                        .offset(y = (-110).dp)
                )
            }
        }

        Text(
            text = if (isAligned)
                "✓ Aligned with Qibla"
            else
                "Rotate ${abs(angleToQibla).toInt()}° ${
                    if (angleToQibla > 0)
                        "clockwise"
                    else
                        "counter-clockwise"
                }",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = if (isAligned)
                Color(0xFF4CAF50)
            else
                Color(0xFFFFB74D)
        )
    }
}

fun calculateQiblaDirection(
    userLat: Double,
    userLng: Double,
    kaabaLat: Double,
    kaabaLng: Double
): Float {

    val lat1 = Math.toRadians(userLat)
    val lat2 = Math.toRadians(kaabaLat)
    val deltaLng = Math.toRadians(kaabaLng - userLng)

    val y = sin(deltaLng) * cos(lat2)
    val x =
        cos(lat1) * sin(lat2) -
                sin(lat1) * cos(lat2) * cos(deltaLng)

    val bearing = Math.toDegrees(atan2(y, x))

    return ((bearing + 360) % 360).toFloat()
}

fun normalizeAngle(angle: Float): Float {
    var a = angle % 360
    if (a > 180) a -= 360
    if (a < -180) a += 360
    return a
}
