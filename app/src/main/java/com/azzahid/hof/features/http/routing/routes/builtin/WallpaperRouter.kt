package com.azzahid.hof.features.http.routing.routes.builtin

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.io.IOException

private const val MAX_WALLPAPER_BYTES = 30L * 1024 * 1024

internal fun io.ktor.server.routing.Route.addWallpaperRoute(route: Route) {
    post(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext
        val target = call.request.queryParameters["target"]?.lowercase() ?: "both"

        if (target !in setOf("home", "lock", "both")) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = "target must be one of: home, lock, both")
            )
            return@post
        }

        val length = call.request.headers[io.ktor.http.HttpHeaders.ContentLength]?.toLongOrNull()
        if (length != null && length > MAX_WALLPAPER_BYTES) {
            call.respond(
                HttpStatusCode.PayloadTooLarge,
                Failure(error = "Image is larger than ${formatBytes(MAX_WALLPAPER_BYTES)}")
            )
            return@post
        }

        val bytes = call.receive<ByteArray>()
        if (bytes.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, Failure(error = "Request body is empty"))
            return@post
        }

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (bitmap == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = "Body is not a decodable image")
            )
            return@post
        }

        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            if (target == "home" || target == "both") {
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
            }
            if (target == "lock" || target == "both") {
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            }
            call.respond(
                Success(
                    data = mapOf(
                        "target" to target,
                        "width" to bitmap.width,
                        "height" to bitmap.height
                    )
                )
            )
        } catch (e: SecurityException) {
            call.respond(
                HttpStatusCode.Forbidden,
                Failure(error = "Wallpaper permission denied: ${e.message}")
            )
        } catch (e: IOException) {
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(error = "Failed to set wallpaper: ${e.message}")
            )
        } finally {
            bitmap.recycle()
        }
    }
}
