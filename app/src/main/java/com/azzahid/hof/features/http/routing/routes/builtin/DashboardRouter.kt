package com.azzahid.hof.features.http.routing.routes.builtin

import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.features.http.utils.DashboardHtmlBuilder
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.plugins.origin
import io.ktor.server.response.header
import io.ktor.server.response.respondText

internal fun io.ktor.server.routing.Route.addDashboardRoute(route: Route) {
    get(route.path, {
        description = route.description
    }) {
        val origin = call.request.origin
        val isDefaultPort = (origin.scheme == "http" && origin.serverPort == 80) ||
            (origin.scheme == "https" && origin.serverPort == 443)
        val portSuffix = if (isDefaultPort) "" else ":${origin.serverPort}"
        val baseUrl = "${origin.scheme}://${origin.serverHost}$portSuffix"
        val html = DashboardHtmlBuilder.build(baseUrl)
        call.response.header(HttpHeaders.CacheControl, "no-store")
        call.respondText(html, ContentType.Text.Html)
    }
}
