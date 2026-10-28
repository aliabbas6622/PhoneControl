package com.azzahid.hof.features.http.routing.routes.builtin

import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond

internal fun io.ktor.server.routing.Route.addEchoRoute(route: Route) {
    route(route.path, {
        description = route.description
    }) {
        get {
            call.respond(Success(data = echoData(call)))
        }
        post {
            val body = call.receiveText()
            call.respond(Success(data = echoData(call) + ("body" to body)))
        }
    }
}

private fun echoData(call: ApplicationCall): Map<String, Any> {
    val headers = call.request.headers.entries().associate { (name, values) ->
        val value = if (name.equals(HttpHeaders.Authorization, ignoreCase = true) ||
            name.equals(HttpHeaders.Cookie, ignoreCase = true) ||
            name.contains("token", ignoreCase = true) ||
            name.contains("secret", ignoreCase = true)
        ) {
            "[redacted]"
        } else {
            values.joinToString(", ")
        }
        name to value
    }

    val query = call.request.queryParameters.entries().associate { entry ->
        entry.key to entry.value.joinToString(", ")
    }

    val remoteAddress = call.request.local.remoteHost

    return mapOf(
        "method" to call.request.httpMethod.value,
        "path" to call.request.path(),
        "query" to query,
        "headers" to headers,
        "remoteHost" to remoteAddress
    )
}
