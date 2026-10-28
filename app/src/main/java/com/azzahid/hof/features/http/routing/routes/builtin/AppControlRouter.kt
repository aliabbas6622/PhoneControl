package com.azzahid.hof.features.http.routing.routes.builtin

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

internal fun io.ktor.server.routing.Route.addAppLaunchRoute(route: Route) {
    post(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext
        val packageName = call.request.queryParameters["package"]

        if (packageName.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = "Missing required query parameter: package")
            )
            return@post
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: findLauncherIntent(context, packageName)
        if (launchIntent == null) {
            call.respond(
                HttpStatusCode.NotFound,
                Failure(error = "App not found or has no launchable activity: $packageName")
            )
            return@post
        }

        try {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            withOverlayWindow(context) {
                context.startActivity(launchIntent)
                delay(300)
            }
            call.respond(
                Success(
                    data = mapOf(
                        "launched" to packageName,
                        "overlayGranted" to Settings.canDrawOverlays(context)
                    )
                )
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(error = "Failed to launch app: ${e.message}")
            )
        }
    }
}

internal fun io.ktor.server.routing.Route.addAppStopRoute(route: Route) {
    post(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext
        val packageName = call.request.queryParameters["package"]

        if (packageName.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = "Missing required query parameter: package")
            )
            return@post
        }

        if (packageName == context.packageName) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = "Refusing to stop the server app itself")
            )
            return@post
        }
        if (packageName == homePackageName(context)) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = "Cannot stop the active launcher")
            )
            return@post
        }

        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val overlayGranted = Settings.canDrawOverlays(context)
            // Foreground apps survive killBackgroundProcesses, so collapse to home first.
            withOverlayWindow(context) {
                context.startActivity(
                    Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_HOME)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                delay(600)
                activityManager.killBackgroundProcesses(packageName)
            }
            val note = if (overlayGranted) {
                "Sent to the background and its background process was killed"
            } else {
                "Kill requested, but Android blocks background app switching without the " +
                    "'Display over other apps' permission; enable it to stop apps that are on screen"
            }
            call.respond(
                Success(
                    data = mapOf(
                        "stopped" to packageName,
                        "overlayGranted" to overlayGranted,
                        "note" to note
                    )
                )
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(error = "Failed to stop app: ${e.message}")
            )
        }
    }
}

/**
 * Android blocks app-switching from the background unless the caller has a visible window.
 * While the block runs, a 1x1 transparent overlay marks this UID as having one.
 */
private suspend fun <T> withOverlayWindow(context: Context, block: suspend () -> T): T {
    if (!Settings.canDrawOverlays(context)) return block()

    return withContext(Dispatchers.Main.immediate) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = View(context)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            1,
            1,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        var added = false
        try {
            windowManager.addView(view, params)
            added = true
        } catch (e: Exception) {
            Log.w("AppControlRouter", "Overlay window could not be shown", e)
        }
        try {
            block()
        } finally {
            if (added) {
                try {
                    windowManager.removeView(view)
                } catch (e: Exception) {
                    Log.w("AppControlRouter", "Overlay window could not be removed", e)
                }
            }
        }
    }
}

private fun homePackageName(context: Context): String? {
    @Suppress("DEPRECATION")
    return context.packageManager
        .resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)
        ?.activityInfo?.packageName
}

private fun findLauncherIntent(context: Context, packageName: String): Intent? {
    val packageManager = context.packageManager
    @Suppress("DEPRECATION")
    val activity = packageManager
        .queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(packageName),
            0
        )
        .firstOrNull()
        ?.activityInfo
        ?: packageManager
            .queryIntentActivities(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                    .setPackage(packageName),
                0
            )
            .firstOrNull()
            ?.activityInfo
        ?: return null

    return Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .setClassName(activity.packageName, activity.name)
}
