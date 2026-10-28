package com.azzahid.hof.features.http.routing.routes.builtin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond

internal fun io.ktor.server.routing.Route.addClipboardRoute(route: Route) {
    route(route.path, {
        description = route.description
    }) {
        get {
            val context = call.application.androidContext
            call.respond(Success(data = mapOf("text" to readClipboard(context))))
        }

        post {
            val context = call.application.androidContext
            val text = call.readClipboardText()

            if (text == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    com.azzahid.hof.domain.model.Failure(
                        error = "Clipboard text is required in the request body or 'text' query parameter"
                    )
                )
                return@post
            }

            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("HTTP on Fire", text))
            call.respond(Success(data = mapOf("text" to text)))
        }
    }
}

private fun readClipboard(context: Context): String {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    if (!clipboard.hasPrimaryClip()) return ""
    val clip = clipboard.primaryClip ?: return ""
    if (clip.itemCount == 0) return ""
    return clip.getItemAt(0).coerceToText(context).toString()
}

private suspend fun ApplicationCall.readClipboardText(): String? {
    val queryText = request.queryParameters["text"]
    if (!queryText.isNullOrEmpty()) return queryText

    if (request.contentLength() == 0L) return null

    val body = receiveText()
    return body.ifEmpty { null }
}

private fun io.ktor.server.request.ApplicationRequest.contentLength(): Long? =
    headers[io.ktor.http.HttpHeaders.ContentLength]?.toLongOrNull()
