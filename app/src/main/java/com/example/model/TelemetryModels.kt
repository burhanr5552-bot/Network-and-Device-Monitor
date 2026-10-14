package com.example.model

data class NetworkSpeedInfo(
    val downSpeedBps: Long = 0L,
    val upSpeedBps: Long = 0L,
    val formattedDownSpeed: String = "0 B/s",
    val formattedUpSpeed: String = "0 B/s",
    val isWifi: Boolean = false,
    val isCellular: Boolean = false,
    val isConnected: Boolean = false,
    val networkName: String = "Disconnected",
    val wifiFrequency: Int = 0,
    val wifiLinkSpeed: Int = 0,
    val wifiSignalLevel: Int = 0,
    val wifiRssi: Int = 0,
    val ipAddress: String = "0.0.0.0",
    val totalRxBytes: Long = 0L,
    val totalTxBytes: Long = 0L,
    val mobileRxBytes: Long = 0L,
    val mobileTxBytes: Long = 0L
)

data class SimSignalInfo(
    val simState: String = "Checking...",
    val carrierName: String = "Unknown Carrier",
    val networkType: String = "Searching",
    val signalLevel: Int = 0, // 0 to 4
    val signalDbm: Int = -999,
    val isDataRoaming: Boolean = false,
    val dataActivity: String = "None",
    val simSlotsCount: Int = 1
)

data class BatteryInfo(
    val level: Int = 0,
    val temperatureC: Float = 0f,
    val temperatureF: Float = 32f,
    val isCharging: Boolean = false,
    val chargePlug: String = "Battery",
    val status: String = "Unknown",
    val health: String = "Good",
    val voltageMv: Int = 0,
    val technology: String = "Li-ion",
    // Real-time battery measurement properties
    val currentNowMa: Int = 0,          // Real-time instant current in mA (+ charging, - discharging)
    val currentAvgMa: Int = 0,          // Average current in mA
    val chargeCounterMah: Long = 0L,    // Remaining capacity in mAh
    val powerWatts: Float = 0f,         // Real-time power draw/input in Watts (V * I)
    val chargingSpeedCategory: String = "Idle" // e.g. "Fast Charging (18W)", "Discharging (-1.8W)"
)

data class MonitoringSettings(
    val isServiceRunning: Boolean = false,
    val showDataNotification: Boolean = true,       // Separate Data Upload/Download Notification
    val showChargingNotification: Boolean = true,   // Separate Battery & Charging Notification
    val showSignalNotification: Boolean = true,     // Separate SIM & Signal Notification
    val temperatureAlertEnabled: Boolean = true,
    val temperatureThresholdC: Float = 42.0f,
    val batteryLowAlertEnabled: Boolean = true,
    val batteryLowThreshold: Int = 20,
    val batteryFullAlertEnabled: Boolean = true,
    val useFahrenheit: Boolean = false,
    val refreshIntervalMs: Long = 1000L
)

data class SpeedSample(
    val timestamp: Long,
    val downSpeedBps: Long,
    val upSpeedBps: Long
)

data class TempSample(
    val timestamp: Long,
    val tempC: Float
)

data class BatteryCurrentSample(
    val timestamp: Long,
    val currentMa: Int,
    val powerWatts: Float
)
