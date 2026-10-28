package com.azzahid.hof.features.http.routing.routes.builtin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout

internal fun io.ktor.server.routing.Route.addCameraRoute(route: Route) {
    post(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext

        if (!call.requirePermission(
                context,
                Manifest.permission.CAMERA,
                "Camera permission required. Grant CAMERA permission in device settings."
            )
        ) {
            return@post
        }

        val facing = when (call.request.queryParameters["facing"]?.lowercase()) {
            "front" -> CameraCharacteristics.LENS_FACING_FRONT
            else -> CameraCharacteristics.LENS_FACING_BACK
        }

        try {
            val jpeg = capturePhoto(context, facing)
            call.respondBytes(jpeg, ContentType.Image.JPEG)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            call.respond(
                HttpStatusCode.GatewayTimeout,
                Failure(error = "Camera capture timed out")
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(error = "Camera capture failed: ${e.message}")
            )
        }
    }
}

@SuppressLint("MissingPermission")
private suspend fun capturePhoto(context: Context, facing: Int): ByteArray {
    val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    val cameraId = manager.cameraIdList.firstOrNull { id ->
        manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == facing
    } ?: throw IllegalStateException("No camera available for the requested facing mode")

    val characteristics = manager.getCameraCharacteristics(cameraId)
    val configurations = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        ?: throw IllegalStateException("Camera configuration is unavailable")
    val outputSizes = configurations.getOutputSizes(ImageFormat.JPEG)
        ?: throw IllegalStateException("Camera does not support JPEG capture")
    val size = outputSizes
        .filter { it.width <= 1920 && it.height <= 1080 }
        .maxByOrNull { it.width * it.height }
        ?: outputSizes.maxByOrNull { it.width * it.height }
        ?: throw IllegalStateException("Camera has no JPEG output size")

    val thread = HandlerThread("http-on-fire-camera").apply { start() }
    val handler = Handler(thread.looper)
    val reader = ImageReader.newInstance(size.width, size.height, ImageFormat.JPEG, 2)
    val result = CompletableDeferred<ByteArray>()

    var device: CameraDevice? = null
    var session: CameraCaptureSession? = null

    fun cleanup() {
        runCatching { session?.close() }
        runCatching { device?.close() }
        runCatching { reader.close() }
        thread.quitSafely()
    }

    reader.setOnImageAvailableListener({ imageReader ->
        val image = imageReader.acquireLatestImage() ?: return@setOnImageAvailableListener
        try {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
            result.complete(bytes)
        } catch (e: Exception) {
            result.completeExceptionally(e)
        } finally {
            image.close()
        }
    }, handler)

    val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            device = camera

            try {
                val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                    addTarget(reader.surface)
                    set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
                }.build()

                camera.createCaptureSession(
                    listOf(reader.surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(captureSession: CameraCaptureSession) {
                            session = captureSession
                            try {
                                captureSession.capture(
                                    request,
                                    object : CameraCaptureSession.CaptureCallback() {
                                        override fun onCaptureFailed(
                                            session: CameraCaptureSession,
                                            request: CaptureRequest,
                                            failure: CaptureFailure
                                        ) {
                                            result.completeExceptionally(
                                                IllegalStateException(
                                                    "Capture failed with reason ${failure.reason}"
                                                )
                                            )
                                        }
                                    },
                                    handler
                                )
                            } catch (e: Exception) {
                                result.completeExceptionally(e)
                            }
                        }

                        override fun onConfigureFailed(captureSession: CameraCaptureSession) {
                            result.completeExceptionally(
                                IllegalStateException("Failed to configure camera capture session")
                            )
                        }
                    },
                    handler
                )
            } catch (e: Exception) {
                result.completeExceptionally(e)
            }
        }

        override fun onDisconnected(camera: CameraDevice) {
            result.completeExceptionally(IllegalStateException("Camera disconnected"))
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            result.completeExceptionally(IllegalStateException("Camera error: $error"))
            camera.close()
        }
    }

    try {
        manager.openCamera(cameraId, stateCallback, handler)
        return withTimeout(15_000) { result.await() }
    } finally {
        cleanup()
    }
}
