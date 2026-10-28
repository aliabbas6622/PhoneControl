package com.azzahid.hof.features.http.routing.routes.builtin

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

internal fun io.ktor.server.routing.Route.addTtsRoute(route: Route) {
    post(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext
        val text = call.receiveText().trim()

        if (text.isEmpty()) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = "Request body must contain the text to speak")
            )
            return@post
        }

        if (text.length > TextToSpeech.getMaxSpeechInputLength()) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(
                    error = "Text is too long (max ${TextToSpeech.getMaxSpeechInputLength()} characters)"
                )
            )
            return@post
        }

        val spoken = withContext(Dispatchers.Main) {
            speakBlocking(context, text)
        }

        if (spoken) {
            call.respond(Success(data = mapOf("spoken" to text)))
        } else {
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(error = "Text-to-speech engine is unavailable or failed to initialize")
            )
        }
    }
}

private suspend fun speakBlocking(
    context: android.content.Context,
    text: String
): Boolean {
    val initialized = CompletableDeferred<Boolean>()

    val tts = TextToSpeech(context) { status ->
        initialized.complete(status == TextToSpeech.SUCCESS)
    }

    val ready = withTimeoutOrNull(5_000) { initialized.await() } ?: false
    if (!ready) {
        tts.shutdown()
        return false
    }

    tts.language = Locale.getDefault()
    val utteranceId = "http-on-fire-${System.currentTimeMillis()}"
    val finished = CompletableDeferred<Boolean>()

    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit
        override fun onDone(utteranceId: String?) {
            finished.complete(true)
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            finished.complete(false)
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            finished.complete(false)
        }
    })

    val queued = tts.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), utteranceId)
    val completed = if (queued == TextToSpeech.SUCCESS) {
        withTimeoutOrNull(text.length * 150L + 30_000L) { finished.await() } ?: false
    } else {
        false
    }

    tts.stop()
    tts.shutdown()
    return completed
}
