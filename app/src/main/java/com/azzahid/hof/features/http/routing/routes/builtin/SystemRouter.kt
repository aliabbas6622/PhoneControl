package com.azzahid.hof.features.http.routing.routes.builtin

import android.os.Build
import android.os.SystemClock
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.get
import io.ktor.server.response.respond
import java.util.Locale
import java.util.TimeZone

internal fun io.ktor.server.routing.Route.addSystemRoute(route: Route) {
    get(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext
        val uptime = SystemClock.elapsedRealtime()

        val packageInfo = runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }.getOrNull()

        val data = mapOf(
            "uptimeMillis" to uptime,
            "uptimeFormatted" to formatDuration(uptime),
            "bootTime" to System.currentTimeMillis() - uptime,
            "cpuCores" to Runtime.getRuntime().availableProcessors(),
            "abi" to Build.SUPPORTED_ABIS.firstOrNull().orEmpty(),
            "supportedAbis" to Build.SUPPORTED_ABIS.toList(),
            "kernelVersion" to System.getProperty("os.version").orEmpty(),
            "securityPatch" to Build.VERSION.SECURITY_PATCH,
            "locale" to Locale.getDefault().toLanguageTag(),
            "timeZone" to TimeZone.getDefault().id,
            "appVersion" to (packageInfo?.versionName ?: "unknown"),
            "threadCount" to Thread.activeCount(),
            "maxMemory" to Runtime.getRuntime().maxMemory(),
            "freeMemory" to Runtime.getRuntime().freeMemory()
        )

        call.respond(Success(data = data))
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val days = totalSeconds / 86_400
    val hours = (totalSeconds % 86_400) / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (days > 0) append("${days}d ")
        if (days > 0 || hours > 0) append("${hours}h ")
        if (days > 0 || hours > 0 || minutes > 0) append("${minutes}m ")
        append("${seconds}s")
    }.trim()
}
