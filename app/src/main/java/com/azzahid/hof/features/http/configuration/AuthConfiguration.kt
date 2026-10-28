package com.azzahid.hof.features.http.configuration

import com.azzahid.hof.domain.model.AccessPassword
import com.azzahid.hof.domain.model.AccessPasswordError
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.features.http.utils.ChangePasswordHtmlBuilder
import com.azzahid.hof.features.http.utils.LoginHtmlBuilder
import io.ktor.http.ContentType
import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLParameter
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.origin
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.uri
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.security.MessageDigest

internal const val AUTH_COOKIE_NAME = "hof_session"
internal const val DASHBOARD_PATH = "/api/dashboard"
private const val LOGIN_PATH = "/login"
private const val ACCOUNT_PATH = "/account"
private const val SESSION_MAX_AGE_SECONDS = 60 * 60 * 24 * 30
private val PUBLIC_PATHS = setOf("/", LOGIN_PATH, "/logout", "/favicon.ico")

internal fun Application.configureAuthentication(accessTokenProvider: suspend () -> String) {
    install(XForwardedHeaders)

    intercept(ApplicationCallPipeline.Plugins) {
        val call = call
        if (call.request.uri.substringBefore('?') in PUBLIC_PATHS) return@intercept

        val expected = runCatching { accessTokenProvider() }.getOrDefault("")
        if (expected.isNotEmpty() && call.isAuthenticated(expected)) return@intercept

        if (wantsHtml(call)) {
            call.respondRedirect("$LOGIN_PATH?next=${call.request.uri.encodeURLParameter()}")
        } else {
            call.respond(HttpStatusCode.Unauthorized, Failure("Authentication required"))
        }
        finish()
    }
}

internal fun Route.addPublicAuthRoutes(
    accessTokenProvider: suspend () -> String,
    accessTokenUpdater: suspend (String) -> Unit
) {
    get("/") {
        call.respondRedirect(DASHBOARD_PATH)
    }

    get("/favicon.ico") {
        call.respond(HttpStatusCode.NoContent)
    }

    get(LOGIN_PATH) {
        val expected = runCatching { accessTokenProvider() }.getOrDefault("")
        val next = safeNextPath(call.request.queryParameters["next"])
        if (expected.isNotEmpty() && call.isAuthenticated(expected)) {
            call.respondRedirect(next)
            return@get
        }
        call.response.header(HttpHeaders.CacheControl, "no-store")
        call.respondText(
            LoginHtmlBuilder.build(next, call.request.queryParameters["error"] != null),
            ContentType.Text.Html
        )
    }

    post(LOGIN_PATH) {
        val params = call.receiveFormParametersOrEmpty()
        val expected = runCatching { accessTokenProvider() }.getOrDefault("")
        val supplied = params["token"].orEmpty()
        val next = safeNextPath(params["next"])
        if (expected.isNotEmpty() && constantTimeEquals(supplied, expected)) {
            call.response.cookies.append(sessionCookie(expected, call.isSecureRequest()))
            call.respondRedirect(next)
        } else {
            call.respondRedirect("$LOGIN_PATH?error=1&next=${next.encodeURLParameter()}")
        }
    }

    get("/logout") {
        call.response.cookies.append(sessionCookie("", call.isSecureRequest(), maxAgeSeconds = 0))
        call.respondRedirect(LOGIN_PATH)
    }

    get(ACCOUNT_PATH) {
        call.response.header(HttpHeaders.CacheControl, "no-store")
        call.respondText(ChangePasswordHtmlBuilder.build(), ContentType.Text.Html)
    }

    post(ACCOUNT_PATH) {
        val params = call.receiveFormParametersOrEmpty()
        val password = AccessPassword.normalize(params["password"].orEmpty())
        val confirm = AccessPassword.normalize(params["confirm"].orEmpty())
        val validationError = AccessPassword.validate(password)
        val errorMessage = when {
            validationError != null -> validationError.toMessage()
            password != confirm -> "Passwords don't match"
            else -> null
        }
        if (errorMessage != null) {
            call.response.header(HttpHeaders.CacheControl, "no-store")
            call.respondText(
                ChangePasswordHtmlBuilder.build(errorMessage),
                ContentType.Text.Html,
                HttpStatusCode.BadRequest
            )
            return@post
        }
        accessTokenUpdater(password)
        call.response.cookies.append(sessionCookie("", call.isSecureRequest(), maxAgeSeconds = 0))
        call.respondRedirect(LOGIN_PATH)
    }
}

private fun AccessPasswordError.toMessage(): String = when (this) {
    AccessPasswordError.TOO_SHORT -> "Use at least ${AccessPassword.MIN_LENGTH} characters"
    AccessPasswordError.TOO_LONG -> "Use at most ${AccessPassword.MAX_LENGTH} characters"
    AccessPasswordError.INVALID_CHARACTERS -> "Control characters aren't allowed"
}

private suspend fun ApplicationCall.receiveFormParametersOrEmpty(): Map<String, String> =
    runCatching { receiveParameters().entries().associate { it.key to it.value.first() } }
        .getOrDefault(emptyMap())

private fun ApplicationCall.isAuthenticated(expected: String): Boolean {
    val supplied = request.cookies[AUTH_COOKIE_NAME] ?: bearerToken()
    return supplied != null && constantTimeEquals(supplied, expected)
}

private fun ApplicationCall.bearerToken(): String? =
    request.headers[HttpHeaders.Authorization]
        ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
        ?.substring(7)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

private fun sessionCookie(
    value: String,
    secure: Boolean,
    maxAgeSeconds: Int = SESSION_MAX_AGE_SECONDS
): Cookie = Cookie(
    name = AUTH_COOKIE_NAME,
    value = value,
    maxAge = maxAgeSeconds,
    path = "/",
    secure = secure,
    httpOnly = true,
    extensions = mapOf("SameSite" to "Lax")
)

private fun ApplicationCall.isSecureRequest(): Boolean = request.origin.scheme == "https"

private fun wantsHtml(call: ApplicationCall): Boolean =
    call.request.uri.startsWith(DASHBOARD_PATH) ||
        call.request.headers[HttpHeaders.Accept]?.contains("text/html", ignoreCase = true) == true

internal fun safeNextPath(raw: String?): String {
    val candidate = raw?.trim()
    if (candidate.isNullOrEmpty()) return DASHBOARD_PATH
    val isValid = candidate.startsWith("/") &&
        !candidate.startsWith("//") &&
        !candidate.contains('\\') &&
        candidate.none { it == '\r' || it == '\n' || it == '\u0000' }
    return if (isValid) candidate else DASHBOARD_PATH
}

internal fun constantTimeEquals(supplied: String, expected: String): Boolean =
    MessageDigest.isEqual(supplied.toByteArray(), expected.toByteArray())
