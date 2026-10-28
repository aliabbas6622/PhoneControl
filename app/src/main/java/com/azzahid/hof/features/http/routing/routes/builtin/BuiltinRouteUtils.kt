package com.azzahid.hof.features.http.routing.routes.builtin

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.azzahid.hof.domain.model.Failure
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

internal fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

internal suspend fun ApplicationCall.requirePermission(
    context: Context,
    permission: String,
    error: String
): Boolean {
    if (context.hasPermission(permission)) return true
    respond(HttpStatusCode.Forbidden, Failure(error = error))
    return false
}

internal fun formatBytes(bytes: Long): String = when {
    bytes >= 1_073_741_824 -> "%.1f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576 -> "%.0f MB".format(bytes / 1_048_576.0)
    else -> "%.0f KB".format(bytes / 1024.0)
}
