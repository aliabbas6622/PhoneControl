package com.azzahid.hof.features.http.routing.routes.builtin

import android.media.Ringtone
import android.media.RingtoneManager
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DEFAULT_DURATION = 5_000L
private const val MAX_DURATION = 60_000L

internal fun io.ktor.server.routing.Route.addRingRoute(route: Route) {
    post(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext
        val shouldStop = call.request.queryParameters["stop"]?.toBooleanStrictOrNull() == true ||
            call.request.queryParameters["action"] == "stop"

        if (shouldStop) {
            RingtoneController.stop()
            call.respond(Success(data = mapOf("ringing" to false)))
            return@post
        }

        val duration = (call.request.queryParameters["duration"]?.toLongOrNull()
            ?: DEFAULT_DURATION).coerceIn(1_000L, MAX_DURATION)

        val started = RingtoneController.play(context, duration)
        if (started) {
            call.respond(Success(data = mapOf("ringing" to true, "duration" to duration)))
        } else {
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(error = "No alarm or ringtone available on this device")
            )
        }
    }
}

private object RingtoneController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null
    private var ringtone: Ringtone? = null

    suspend fun play(context: android.content.Context, duration: Long): Boolean =
        withContext(Dispatchers.Main) {
            stopInternal()

            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: return@withContext false

            val sound = RingtoneManager.getRingtone(context.applicationContext, uri)
                ?: return@withContext false

            sound.audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            ringtone = sound
            job = scope.launch {
                sound.play()
                delay(duration)
                stop()
            }
            true
        }

    suspend fun stop() = withContext(Dispatchers.Main) {
        stopInternal()
    }

    private fun stopInternal() {
        job?.cancel()
        job = null
        ringtone?.stop()
        ringtone = null
    }
}
