package com.azzahid.hof.features.http.routing.routes.builtin

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.get
import io.ktor.server.response.respond
import kotlinx.coroutines.delay

private val SENSOR_TYPES = listOf(
    "accelerometer" to Sensor.TYPE_ACCELEROMETER,
    "gyroscope" to Sensor.TYPE_GYROSCOPE,
    "magnetometer" to Sensor.TYPE_MAGNETIC_FIELD,
    "light" to Sensor.TYPE_LIGHT,
    "proximity" to Sensor.TYPE_PROXIMITY,
    "pressure" to Sensor.TYPE_PRESSURE,
    "humidity" to Sensor.TYPE_RELATIVE_HUMIDITY,
    "ambientTemperature" to Sensor.TYPE_AMBIENT_TEMPERATURE,
    "gravity" to Sensor.TYPE_GRAVITY,
    "linearAcceleration" to Sensor.TYPE_LINEAR_ACCELERATION,
    "rotationVector" to Sensor.TYPE_ROTATION_VECTOR
)

internal fun io.ktor.server.routing.Route.addSensorsRoute(route: Route) {
    get(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val available = mutableMapOf<String, Sensor>()
        val readings = mutableMapOf<String, Map<String, Any>>()
        val registered = mutableListOf<Pair<Sensor, SensorEventListener>>()

        try {
            for ((name, type) in SENSOR_TYPES) {
                val sensor = sensorManager.getDefaultSensor(type) ?: continue
                available[name] = sensor

                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        readings[name] = mapOf(
                            "values" to event.values.map { it.toDouble() },
                            "accuracy" to event.accuracy,
                            "timestamp" to event.timestamp
                        )
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                }

                val registeredOk = runCatching {
                    sensorManager.registerListener(
                        listener,
                        sensor,
                        SensorManager.SENSOR_DELAY_FASTEST
                    )
                }.getOrDefault(false)

                if (registeredOk) {
                    registered.add(sensor to listener)
                }
            }

            delay(800)
        } finally {
            registered.forEach { (sensor, listener) ->
                runCatching { sensorManager.unregisterListener(listener, sensor) }
            }
        }

        val sensors = available.mapValues { (name, sensor) ->
            val reading = readings[name]
            mapOf(
                "name" to sensor.name,
                "vendor" to sensor.vendor,
                "values" to (reading?.get("values") ?: emptyList<Double>()),
                "accuracy" to (reading?.get("accuracy") ?: 0),
                "timestamp" to (reading?.get("timestamp") ?: 0L),
                "power" to sensor.power,
                "maxRange" to sensor.maximumRange,
                "resolution" to sensor.resolution
            )
        }

        call.respond(Success(data = mapOf("count" to sensors.size, "sensors" to sensors)))
    }
}
