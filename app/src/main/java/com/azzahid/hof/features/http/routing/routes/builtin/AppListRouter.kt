package com.azzahid.hof.features.http.routing.routes.builtin

import android.content.pm.PackageManager
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes

internal fun io.ktor.server.routing.Route.addAppListRoute(route: Route) {
    get(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext
        val packageManager = context.packageManager

        val applications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(0)
        }

        val includeSystem = call.request.queryParameters["includeSystemApps"]?.toBoolean() ?: false
        val search = call.request.queryParameters["q"]?.lowercase()

        val apps = applications
            .asSequence()
            .filter { info ->
                (includeSystem || packageManager.getLaunchIntentForPackage(info.packageName) != null)
            }
            .filter { info ->
                if (search.isNullOrBlank()) {
                    true
                } else {
                    info.packageName.lowercase().contains(search) ||
                        packageManager.getApplicationLabel(info).toString().lowercase().contains(search)
                }
            }
            .map { info ->
                mapOf(
                    "name" to packageManager.getApplicationLabel(info).toString(),
                    "packageName" to info.packageName,
                    "isSystemApp" to ((info.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0)
                )
            }
            .sortedBy { it["name"].toString().lowercase() }
            .toList()

        call.respond(Success(data = mapOf("count" to apps.size, "apps" to apps)))
    }

    get("${route.path}/icon", {
        description = "PNG icon of an installed app by package name"
    }) {
        val context = call.application.androidContext
        val packageName = call.request.queryParameters["package"]

        if (packageName.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = "Missing required query parameter: package")
            )
            return@get
        }

        try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            val bitmap = drawable.toBitmap(width = 144, height = 144)
            val png = java.io.ByteArrayOutputStream().use { stream ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                bitmap.recycle()
                stream.toByteArray()
            }
            call.response.headers.append(
                io.ktor.http.HttpHeaders.CacheControl,
                "public, max-age=3600"
            )
            call.respondBytes(png, ContentType.Image.PNG)
        } catch (e: PackageManager.NameNotFoundException) {
            call.respond(
                HttpStatusCode.NotFound,
                Failure(error = "App not found: $packageName")
            )
        }
    }
}
