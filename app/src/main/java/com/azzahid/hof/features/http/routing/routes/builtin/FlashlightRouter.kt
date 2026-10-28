package com.azzahid.hof.features.http.routing.routes.builtin

import android.Manifest
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import java.util.concurrent.atomic.AtomicBoolean

private val torchEnabled = AtomicBoolean(false)

internal fun io.ktor.server.routing.Route.addFlashlightRoute(route: Route) {
    post(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext

        if (!context.hasPermission(Manifest.permission.CAMERA)) {
            call.respond(
                HttpStatusCode.Forbidden,
                Failure(error = "Camera permission required to control the flashlight")
            )
            return@post
        }

        val requested = call.request.queryParameters["enable"]?.toBooleanStrictOrNull()
        val target = requested ?: !torchEnabled.get()

        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                    characteristics.get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_BACK
            } ?: cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }

            if (cameraId == null) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    Failure(error = "Device has no flashlight")
                )
                return@post
            }

            cameraManager.setTorchMode(cameraId, target)
            torchEnabled.set(target)
            call.respond(Success(data = mapOf("enabled" to target)))
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(error = "Failed to control flashlight: ${e.message}")
            )
        }
    }
}
