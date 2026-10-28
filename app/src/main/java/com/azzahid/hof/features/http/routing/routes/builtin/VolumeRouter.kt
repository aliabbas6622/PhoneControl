package com.azzahid.hof.features.http.routing.routes.builtin

import android.content.Context
import android.media.AudioManager
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val STREAMS = mapOf(
    "media" to AudioManager.STREAM_MUSIC,
    "ring" to AudioManager.STREAM_RING,
    "alarm" to AudioManager.STREAM_ALARM,
    "notification" to AudioManager.STREAM_NOTIFICATION,
    "system" to AudioManager.STREAM_SYSTEM
)

internal fun io.ktor.server.routing.Route.addVolumeRoute(route: Route) {
    route(route.path, {
        description = route.description
    }) {
        get {
            call.respond(Success(data = readVolumes(call.application.androidContext)))
        }

        post {
            val context = call.application.androidContext
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            val body = runCatching { call.receiveText() }.getOrDefault("")
            val json = runCatching {
                Json.parseToJsonElement(body).jsonObject
            }.getOrNull()

            val streamKey = json?.get("stream")?.jsonPrimitive?.contentOrNull
                ?: call.request.queryParameters["stream"]
            val volume = json?.get("volume")?.jsonPrimitive?.intOrNull
                ?: call.request.queryParameters["volume"]?.toIntOrNull()

            val stream = STREAMS[streamKey]
            if (stream == null || volume == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    Failure(
                        error = "Provide 'stream' (${STREAMS.keys.joinToString(", ")}) and 'volume'"
                    )
                )
                return@post
            }

            val max = audioManager.getStreamMaxVolume(stream)
            val clamped = volume.coerceIn(0, max)
            audioManager.setStreamVolume(stream, clamped, 0)

            call.respond(
                Success(
                    data = mapOf(
                        "stream" to streamKey,
                        "volume" to clamped,
                        "max" to max,
                        "streams" to readVolumes(context)
                    )
                )
            )
        }
    }
}

private fun readVolumes(context: Context): Map<String, Any> {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return STREAMS.entries.associate { (name, stream) ->
        name to mapOf(
            "current" to audioManager.getStreamVolume(stream),
            "max" to audioManager.getStreamMaxVolume(stream)
        )
    }
}
