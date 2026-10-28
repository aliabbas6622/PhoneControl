package com.azzahid.hof.features.http.routing.routes.builtin

import android.content.Context
import android.net.wifi.WifiManager
import com.azzahid.hof.data.repository.AndroidNetworkRepository
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.get
import io.ktor.server.response.respond

internal fun io.ktor.server.routing.Route.addWifiInfoRoute(route: Route) {
    get(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager

        @Suppress("DEPRECATION")
        val info = wifiManager.connectionInfo

        val ssid = info?.ssid?.trim('"')
            ?.takeUnless { it.isEmpty() || it == "<unknown ssid>" }
            ?: "Unknown"

        val frequency = info?.frequency ?: 0
        val frequencyUnits = when {
            frequency >= 5000 -> "MHz (5 GHz)"
            frequency > 0 -> "MHz (2.4 GHz)"
            else -> "MHz"
        }

        val data = mapOf(
            "ssid" to ssid,
            "bssid" to (info?.bssid ?: ""),
            "ip" to AndroidNetworkRepository().getLocalIpAddress(),
            "rssi" to (info?.rssi ?: 0),
            "linkSpeed" to (info?.linkSpeed ?: 0),
            "linkSpeedUnits" to "Mbps",
            "frequency" to frequency,
            "frequencyUnits" to frequencyUnits,
            "isWifiEnabled" to wifiManager.isWifiEnabled
        )

        call.respond(Success(data = data))
    }
}
