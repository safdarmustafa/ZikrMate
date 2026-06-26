package com.zikrmate.app.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

private const val LOCATION_TIMEOUT_MS = 15_000L

/**
 * Fetches the device's current location for prayer times / Qibla.
 * 1. Tries last known location first (fast; works with emulator mock location).
 * 2. If null, requests a fresh fix with a 15s timeout so we don't hang.
 *
 * On emulator: set location in Extended Controls (⋮) → Location →
 * e.g. Patna, Bihar: 25.5941, 85.1376 then "Set Location".
 */
@SuppressLint("MissingPermission")
suspend fun getUserLocation(context: Context): Pair<Double, Double>? {

    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    // 1) Try last known / cached location (waits for task so emulator mock location is used)
    val last = getLastKnownLocationOrAwait(context)
    if (last != null) return last

    // 2) Request fresh location with timeout
    return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
        suspendCancellableCoroutine { continuation ->

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                2000
            )
                .setMaxUpdates(1)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location: Location? = result.lastLocation
                    if (location != null) {
                        fusedClient.removeLocationUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(Pair(location.latitude, location.longitude))
                        }
                    }
                }
            }

            fusedClient.requestLocationUpdates(
                locationRequest,
                callback,
                context.mainLooper
            )

            continuation.invokeOnCancellation {
                fusedClient.removeLocationUpdates(callback)
            }
        }
    }
}
