package com.azzahid.hof.features.http.routing.routes.builtin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

internal fun io.ktor.server.routing.Route.addLocationRoute(route: Route) {
    get(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext

        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            !context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            call.respond(
                HttpStatusCode.Forbidden,
                Failure(error = "Location permission required. Grant location access in device settings.")
            )
            return@get
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val location = requestLocation(context, locationManager)
        if (location == null) {
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                Failure(error = "Unable to determine location. Make sure location services are enabled.")
            )
            return@get
        }

        val data = mapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "altitude" to location.altitude,
            "accuracy" to location.accuracy.toDouble(),
            "provider" to location.provider,
            "speed" to location.speed.toDouble(),
            "bearing" to location.bearing.toDouble(),
            "time" to location.time,
            "isFromLastKnown" to (location.time < System.currentTimeMillis() - 60_000)
        )

        call.respond(Success(data = data))
    }
}

@SuppressLint("MissingPermission")
private suspend fun requestLocation(
    context: Context,
    locationManager: LocationManager
): Location? {
    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER
    ).filter { provider ->
        runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
    }

    providers.forEach { provider ->
        runCatching { locationManager.getLastKnownLocation(provider) }
            .getOrNull()
            ?.let { lastKnown ->
                if (lastKnown.time > System.currentTimeMillis() - 5 * 60_000) {
                    return lastKnown
                }
            }
    }

    val provider = providers.firstOrNull() ?: return null

    return withTimeoutOrNull(10_000) {
        suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }

                @Deprecated("Required by platform")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
                override fun onProviderEnabled(provider: String) = Unit
                override fun onProviderDisabled(provider: String) = Unit
            }

            runCatching {
                locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            }.onFailure {
                if (continuation.isActive) continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                runCatching { locationManager.removeUpdates(listener) }
            }
        }
    }
}
