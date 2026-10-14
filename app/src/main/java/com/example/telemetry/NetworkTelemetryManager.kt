package com.example.telemetry

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.WifiManager
import android.os.Build
import com.example.model.NetworkSpeedInfo
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale

class NetworkTelemetryManager(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private var lastRxBytes = TrafficStats.getTotalRxBytes()
    private var lastTxBytes = TrafficStats.getTotalTxBytes()
    private var lastTimestamp = System.currentTimeMillis()

    fun captureNetworkInfo(): NetworkSpeedInfo {
        val now = System.currentTimeMillis()
        val timeDeltaMs = (now - lastTimestamp).coerceAtLeast(100L)
        val timeDeltaSec = timeDeltaMs / 1000.0

        val currentRx = TrafficStats.getTotalRxBytes()
        val currentTx = TrafficStats.getTotalTxBytes()

        val rxDiff = if (lastRxBytes > 0 && currentRx >= lastRxBytes) currentRx - lastRxBytes else 0L
        val txDiff = if (lastTxBytes > 0 && currentTx >= lastTxBytes) currentTx - lastTxBytes else 0L

        val downSpeedBps = (rxDiff / timeDeltaSec).toLong().coerceAtLeast(0L)
        val upSpeedBps = (txDiff / timeDeltaSec).toLong().coerceAtLeast(0L)

        lastRxBytes = currentRx
        lastTxBytes = currentTx
        lastTimestamp = now

        var isWifi = false
        var isCellular = false
        var isConnected = false
        var networkName = "Offline"

        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)

        if (capabilities != null) {
            if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                isConnected = true
            }
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                isWifi = true
                networkName = "Wi-Fi"
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                isCellular = true
                networkName = "Mobile Data"
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                networkName = "Ethernet"
            }
        }

        var wifiFrequency = 0
        var wifiLinkSpeed = 0
        var wifiRssi = 0
        var wifiSignalLevel = 0

        if (isWifi && wifiManager != null) {
            try {
                @Suppress("DEPRECATION")
                val wifiInfo = wifiManager.connectionInfo
                if (wifiInfo != null) {
                    wifiFrequency = wifiInfo.frequency
                    wifiLinkSpeed = wifiInfo.linkSpeed
                    wifiRssi = wifiInfo.rssi
                    wifiSignalLevel = WifiManager.calculateSignalLevel(wifiRssi, 5)
                    val ssid = wifiInfo.ssid?.replace("\"", "") ?: ""
                    if (ssid.isNotEmpty() && ssid != "<unknown ssid>") {
                        networkName = ssid
                    }
                }
            } catch (_: Exception) {
            }
        }

        val ipAddress = getLocalIpAddress()
        val mobileRx = TrafficStats.getMobileRxBytes()
        val mobileTx = TrafficStats.getMobileTxBytes()

        return NetworkSpeedInfo(
            downSpeedBps = downSpeedBps,
            upSpeedBps = upSpeedBps,
            formattedDownSpeed = formatSpeed(downSpeedBps),
            formattedUpSpeed = formatSpeed(upSpeedBps),
            isWifi = isWifi,
            isCellular = isCellular,
            isConnected = isConnected,
            networkName = networkName,
            wifiFrequency = wifiFrequency,
            wifiLinkSpeed = wifiLinkSpeed,
            wifiSignalLevel = wifiSignalLevel,
            wifiRssi = wifiRssi,
            ipAddress = ipAddress,
            totalRxBytes = currentRx,
            totalTxBytes = currentTx,
            mobileRxBytes = mobileRx,
            mobileTxBytes = mobileTx
        )
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addresses = intf.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "0.0.0.0"
                    }
                }
            }
        } catch (_: Exception) {
        }
        return "127.0.0.1"
    }

    companion object {
        fun formatSpeed(bytesPerSec: Long): String {
            if (bytesPerSec <= 0) return "0 B/s"
            val kb = bytesPerSec / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0

            return when {
                gb >= 1.0 -> String.format(Locale.US, "%.2f GB/s", gb)
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB/s", mb)
                kb >= 1.0 -> String.format(Locale.US, "%.1f KB/s", kb)
                else -> "$bytesPerSec B/s"
            }
        }

        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0

            return when {
                gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
                kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
                else -> "$bytes B"
            }
        }
    }
}
