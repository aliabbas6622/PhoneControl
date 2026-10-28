package com.azzahid.hof.features.http.routing.routes.builtin

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond

internal fun io.ktor.server.routing.Route.addToastRoute(route: Route) {
    post(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext

        val queryText = call.request.queryParameters["text"]
        val text = (queryText ?: runCatching { call.receiveText() }.getOrDefault("")).trim()

        if (text.isEmpty()) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = "Toast text is required in the request body or 'text' query parameter")
            )
            return@post
        }

        val long = call.request.queryParameters["long"]?.toBooleanStrictOrNull() == true
        val duration = if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context.applicationContext, text, duration).show()
        }

        call.respond(Success(data = mapOf("shown" to text, "duration" to if (long) "long" else "short")))
    }
}
