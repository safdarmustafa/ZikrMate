package com.falahpro.app.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Waits for last known location (so emulator mock location is used when set).
 */
@SuppressLint("MissingPermission")
suspend fun getLastKnownLocationOrAwait(context: Context): Pair<Double, Double>? {

    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    val task = fusedClient.lastLocation

    if (task.isComplete) {
        val location = task.result
        return location?.let { Pair(it.latitude, it.longitude) }
    }

    return suspendCancellableCoroutine { cont ->

        task.addOnCompleteListener {

            if (cont.isActive) {
                val location = task.result
                cont.resume(location?.let { Pair(it.latitude, it.longitude) })
            }
        }
    }
}
