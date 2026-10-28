package com.azzahid.hof.features.http.routing.routes.builtin

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.post
import io.ktor.server.response.respond

private const val DEFAULT_DURATION = 500L
private const val MAX_DURATION = 10_000L

internal fun io.ktor.server.routing.Route.addVibrateRoute(route: Route) {
    post(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext
        val duration = (call.request.queryParameters["duration"]?.toLongOrNull()
            ?: DEFAULT_DURATION).coerceIn(1L, MAX_DURATION)

        val vibrator = getVibrator(context)
        if (vibrator == null || !vibrator.hasVibrator()) {
            call.respond(
                io.ktor.http.HttpStatusCode.InternalServerError,
                com.azzahid.hof.domain.model.Failure(error = "Device has no vibrator")
            )
            return@post
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }

        call.respond(Success(data = mapOf("duration" to duration)))
    }
}

private fun getVibrator(context: Context): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
