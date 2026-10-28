package com.azzahid.hof.features.http.routing.routes.builtin

import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.services.AndroidQRCodeService
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes

internal fun io.ktor.server.routing.Route.addQrCodeRoute(route: Route) {
    get(route.path, {
        description = route.description
    }) {
        val data = call.request.queryParameters["data"]
            ?: call.request.queryParameters["text"]

        if (data.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = "Missing required query parameter: data")
            )
            return@get
        }

        val scale = call.request.queryParameters["scale"]?.toIntOrNull() ?: 8
        val bitmap = AndroidQRCodeService().generateQrBitmap(data, scale)

        if (bitmap == null) {
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(error = "Failed to generate QR code")
            )
            return@get
        }

        val png = java.io.ByteArrayOutputStream().use { stream ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
            bitmap.recycle()
            stream.toByteArray()
        }

        call.response.headers.append(
            io.ktor.http.HttpHeaders.CacheControl,
            "no-store, no-cache, must-revalidate"
        )
        call.respondBytes(png, ContentType.Image.PNG)
    }
}
