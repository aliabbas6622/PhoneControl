package com.azzahid.hof.features.http.routing.routes.builtin

import android.content.Context
import android.provider.Settings
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

private const val MAX_BRIGHTNESS = 255

internal fun io.ktor.server.routing.Route.addBrightnessRoute(route: Route) {
    route(route.path, {
        description = route.description
    }) {
        get {
            call.respond(Success(data = readBrightness(call.application.androidContext)))
        }

        post {
            val context = call.application.androidContext
            if (!Settings.System.canWrite(context)) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    Failure(
                        error = "Modify system settings permission required. Enable it for HTTP on Fire in Android settings."
                    )
                )
                return@post
            }

            val body = runCatching { call.receiveText() }.getOrDefault("")
            val json = runCatching { Json.parseToJsonElement(body).jsonObject }.getOrNull()

            val requestedMode = json?.get("mode")?.jsonPrimitive?.contentOrNull
                ?: call.request.queryParameters["mode"]
            val requestedBrightness = json?.get("brightness")?.jsonPrimitive?.intOrNull
                ?: call.request.queryParameters["brightness"]?.toIntOrNull()

            if (requestedMode == null && requestedBrightness == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    Failure(error = "Provide 'brightness' (0-$MAX_BRIGHTNESS) or 'mode' (auto/manual)")
                )
                return@post
            }

            try {
                if (requestedMode != null) {
                    val mode = when (requestedMode.lowercase()) {
                        "auto", "automatic" -> Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                        "manual" -> Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                        else -> {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                Failure(error = "Mode must be 'auto' or 'manual'")
                            )
                            return@post
                        }
                    }
                    Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        mode
                    )
                }

                if (requestedBrightness != null) {
                    Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                    )
                    Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        requestedBrightness.coerceIn(1, MAX_BRIGHTNESS)
                    )
                }

                call.respond(Success(data = readBrightness(context)))
            } catch (e: SecurityException) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    Failure(error = "Failed to change brightness: ${e.message}")
                )
            }
        }
    }
}

private fun readBrightness(context: Context): Map<String, Any> {
    val resolver = context.contentResolver
    val mode = Settings.System.getInt(
        resolver,
        Settings.System.SCREEN_BRIGHTNESS_MODE,
        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
    )
    val brightness = Settings.System.getInt(
        resolver,
        Settings.System.SCREEN_BRIGHTNESS,
        MAX_BRIGHTNESS / 2
    )

    return mapOf(
        "brightness" to brightness,
        "maxBrightness" to MAX_BRIGHTNESS,
        "mode" to if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) "auto" else "manual",
        "canWrite" to Settings.System.canWrite(context)
    )
}
