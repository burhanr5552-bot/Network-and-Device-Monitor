package com.example.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.NetPulseApplication
import com.example.data.SettingsStore
import com.example.data.TelemetryRepository
import com.example.model.BatteryInfo
import com.example.model.MonitoringSettings
import com.example.model.NetworkSpeedInfo
import com.example.model.SimSignalInfo
import com.example.telemetry.BatteryTelemetryManager
import com.example.telemetry.NetworkTelemetryManager
import com.example.telemetry.SimTelemetryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class MonitorForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var loopJob: Job? = null

    private lateinit var networkTelemetry: NetworkTelemetryManager
    private lateinit var simTelemetry: SimTelemetryManager
    private lateinit var batteryTelemetry: BatteryTelemetryManager
    private lateinit var settingsStore: SettingsStore
    private lateinit var notificationManager: NotificationManager

    private var lastHighTempAlertTime = 0L
    private var lastBatteryLowAlertTime = 0L
    private var lastBatteryFullAlertTime = 0L

    override fun onCreate() {
        super.onCreate()
        networkTelemetry = NetworkTelemetryManager(this)
        simTelemetry = SimTelemetryManager(this)
        batteryTelemetry = BatteryTelemetryManager(this)
        settingsStore = SettingsStore(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        TelemetryRepository.setServiceActive(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_STOP -> {
                stopMonitoring()
                return START_NOT_STICKY
            }
            ACTION_START, ACTION_UPDATE_SETTINGS -> {
                startMonitoringLoop()
            }
        }

        return START_STICKY
    }

    private fun startMonitoringLoop() {
        loopJob?.cancel()

        // Immediate initial sample and startForeground with primary separate notification (Data Speed)
        val initialNetwork = networkTelemetry.captureNetworkInfo()
        val initialSim = simTelemetry.captureSimInfo()
        val initialBattery = batteryTelemetry.captureBatteryInfo()
        val currentSettings = settingsStore.settings.value

        TelemetryRepository.updateTelemetry(initialNetwork, initialSim, initialBattery)

        // Bind Foreground Service to primary Data notification
        val dataNotification = buildDataSpeedNotification(initialNetwork)
        startForeground(NetPulseApplication.NOTIFICATION_ID_SPEED, dataNotification)

        // Post the separate Charging and Signal notifications
        if (currentSettings.showChargingNotification) {
            notificationManager.notify(
                NetPulseApplication.NOTIFICATION_ID_BATTERY,
                buildChargingBatteryNotification(initialBattery, currentSettings)
            )
        }
        if (currentSettings.showSignalNotification) {
            notificationManager.notify(
                NetPulseApplication.NOTIFICATION_ID_SIM,
                buildSimSignalNotification(initialSim)
            )
        }

        loopJob = serviceScope.launch {
            while (isActive) {
                val settings = settingsStore.settings.value
                val network = networkTelemetry.captureNetworkInfo()
                val sim = simTelemetry.captureSimInfo()
                val battery = batteryTelemetry.captureBatteryInfo()

                TelemetryRepository.updateTelemetry(network, sim, battery)

                updateSeparateNotifications(network, sim, battery, settings)
                checkAlerts(battery, settings)

                delay(settings.refreshIntervalMs.coerceAtLeast(500L))
            }
        }
    }

    private fun updateSeparateNotifications(
        network: NetworkSpeedInfo,
        sim: SimSignalInfo,
        battery: BatteryInfo,
        settings: MonitoringSettings
    ) {
        // 1. Separate Data Speed Notification
        if (settings.showDataNotification) {
            val dataNotification = buildDataSpeedNotification(network)
            notificationManager.notify(NetPulseApplication.NOTIFICATION_ID_SPEED, dataNotification)
        } else {
            // If data is toggled off, keep minimal placeholder so foreground service doesn't crash
            val minimal = buildDataSpeedNotification(network)
            notificationManager.notify(NetPulseApplication.NOTIFICATION_ID_SPEED, minimal)
        }

        // 2. Separate Charging & Real-Time Battery Measuring Notification
        if (settings.showChargingNotification) {
            val chargingNotification = buildChargingBatteryNotification(battery, settings)
            notificationManager.notify(NetPulseApplication.NOTIFICATION_ID_BATTERY, chargingNotification)
        } else {
            notificationManager.cancel(NetPulseApplication.NOTIFICATION_ID_BATTERY)
        }

        // 3. Separate SIM Signal Strength Notification
        if (settings.showSignalNotification) {
            val simNotification = buildSimSignalNotification(sim)
            notificationManager.notify(NetPulseApplication.NOTIFICATION_ID_SIM, simNotification)
        } else {
            notificationManager.cancel(NetPulseApplication.NOTIFICATION_ID_SIM)
        }
    }

    private fun buildDataSpeedNotification(network: NetworkSpeedInfo): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            1,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            10,
            Intent(this, MonitorForegroundService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Data: ↓ ${network.formattedDownSpeed}  ↑ ${network.formattedUpSpeed}"
        val text = "${network.networkName} • Total Rx: ${NetworkTelemetryManager.formatBytes(network.totalRxBytes)}"

        val bigText = StringBuilder()
            .append("🌐 Network: ${network.networkName} (${if (network.isWifi) "Wi-Fi ${network.wifiLinkSpeed}Mbps" else if (network.isCellular) "Mobile Data" else "Offline"})\n")
            .append("⬇ Download: ${network.formattedDownSpeed}  ⬆ Upload: ${network.formattedUpSpeed}\n")
            .append("📊 Total Session Rx: ${NetworkTelemetryManager.formatBytes(network.totalRxBytes)} | Tx: ${NetworkTelemetryManager.formatBytes(network.totalTxBytes)}\n")
            .append("📡 IP: ${network.ipAddress}")
            .toString()

        val speedIcon = try {
            NotificationIconGenerator.createSpeedIcon(network.downSpeedBps)
        } catch (_: Exception) {
            androidx.core.graphics.drawable.IconCompat.createWithResource(this, android.R.drawable.stat_sys_download)
        }

        return NotificationCompat.Builder(this, NetPulseApplication.CHANNEL_SPEED)
            .setSmallIcon(speedIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopIntent)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun buildChargingBatteryNotification(
        battery: BatteryInfo,
        settings: MonitoringSettings
    ): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            2,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val tempStr = if (settings.useFahrenheit) {
            String.format(Locale.US, "%.1f°F", battery.temperatureF)
        } else {
            String.format(Locale.US, "%.1f°C", battery.temperatureC)
        }

        val sign = if (battery.currentNowMa > 0) "+" else ""
        val title = "Battery: ${battery.level}%  •  ${battery.status} (${sign}${battery.currentNowMa} mA)"
        val text = "${battery.chargingSpeedCategory} • ${String.format(Locale.US, "%.1fW", battery.powerWatts)} • $tempStr"

        val bigText = StringBuilder()
            .append("⚡ Status: ${battery.status} via ${battery.chargePlug}\n")
            .append("🔋 Level: ${battery.level}%  |  Temp: $tempStr\n")
            .append("📈 Live Current: ${sign}${battery.currentNowMa} mA  (Avg: ${battery.currentAvgMa} mA)\n")
            .append("⚡ Real-Time Power: ${String.format(Locale.US, "%.2f Watts", battery.powerWatts)} (${battery.chargingSpeedCategory})\n")
            .append("🔌 Voltage: ${battery.voltageMv} mV  |  Capacity: ~${battery.chargeCounterMah} mAh\n")
            .append("🩺 Health: ${battery.health}  |  Tech: ${battery.technology}")
            .toString()

        val batteryIcon = try {
            NotificationIconGenerator.createBatteryIcon(battery.level, battery.isCharging)
        } catch (_: Exception) {
            androidx.core.graphics.drawable.IconCompat.createWithResource(this, android.R.drawable.ic_lock_idle_charging)
        }

        return NotificationCompat.Builder(this, NetPulseApplication.CHANNEL_BATTERY)
            .setSmallIcon(batteryIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun buildSimSignalNotification(sim: SimSignalInfo): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            3,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dbmStr = if (sim.signalDbm != -999) " (${sim.signalDbm} dBm)" else ""
        val title = "SIM Signal: ${sim.carrierName} [${sim.signalLevel}/4 bars]"
        val text = "Network: ${sim.networkType}$dbmStr • ${sim.dataActivity}"

        val bigText = StringBuilder()
            .append("📶 Carrier: ${sim.carrierName}\n")
            .append("📡 Network Type: ${sim.networkType}\n")
            .append("📊 Signal Strength: ${sim.signalLevel}/4 bars$dbmStr\n")
            .append("🔄 Data Activity: ${sim.dataActivity}  |  Roaming: ${if (sim.isDataRoaming) "Yes" else "No"}\n")
            .append("💳 SIM State: ${sim.simState}")
            .toString()

        val signalIcon = try {
            NotificationIconGenerator.createSignalIcon(sim.signalLevel)
        } catch (_: Exception) {
            androidx.core.graphics.drawable.IconCompat.createWithResource(this, android.R.drawable.stat_sys_phone_call)
        }

        return NotificationCompat.Builder(this, NetPulseApplication.CHANNEL_SIM)
            .setSmallIcon(signalIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun checkAlerts(battery: BatteryInfo, settings: MonitoringSettings) {
        val now = System.currentTimeMillis()
        val alertCooldown = 5 * 60 * 1000L // 5 minutes cooldown between repeated alerts

        // High Temperature Alert
        if (settings.temperatureAlertEnabled && battery.temperatureC >= settings.temperatureThresholdC) {
            if (now - lastHighTempAlertTime > alertCooldown) {
                lastHighTempAlertTime = now
                val tempStr = if (settings.useFahrenheit) {
                    String.format(Locale.US, "%.1f°F", battery.temperatureF)
                } else {
                    String.format(Locale.US, "%.1f°C", battery.temperatureC)
                }
                fireHighPriorityAlert(
                    title = "🔥 High Temperature Warning!",
                    text = "Battery reached $tempStr (Threshold: ${settings.temperatureThresholdC.toInt()}°C). Disconnect heavy load or charger to cool down."
                )
            }
        }

        // Battery Low Alert
        if (settings.batteryLowAlertEnabled && battery.level <= settings.batteryLowThreshold && !battery.isCharging) {
            if (now - lastBatteryLowAlertTime > alertCooldown) {
                lastBatteryLowAlertTime = now
                fireHighPriorityAlert(
                    title = "🪫 Battery Low (${battery.level}%)",
                    text = "Battery is down to ${battery.level}%. Please connect to a power source."
                )
            }
        }

        // Battery Full Alert
        if (settings.batteryFullAlertEnabled && battery.level >= 100 && battery.isCharging) {
            if (now - lastBatteryFullAlertTime > alertCooldown) {
                lastBatteryFullAlertTime = now
                fireHighPriorityAlert(
                    title = "⚡ Battery Fully Charged",
                    text = "Battery is at 100%. Unplug charger to preserve cell health."
                )
            }
        }
    }

    private fun fireHighPriorityAlert(title: String, text: String) {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alert = NotificationCompat.Builder(this, NetPulseApplication.CHANNEL_ALERTS)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NetPulseApplication.NOTIFICATION_ID_ALERT, alert)
    }

    private fun stopMonitoring() {
        loopJob?.cancel()
        TelemetryRepository.setServiceActive(false)
        settingsStore.updateSettings { it.copy(isServiceRunning = false) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        notificationManager.cancel(NetPulseApplication.NOTIFICATION_ID_SPEED)
        notificationManager.cancel(NetPulseApplication.NOTIFICATION_ID_SIM)
        notificationManager.cancel(NetPulseApplication.NOTIFICATION_ID_BATTERY)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        loopJob?.cancel()
        serviceScope.cancel()
        TelemetryRepository.setServiceActive(false)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.example.netpulse.START_MONITORING"
        const val ACTION_STOP = "com.example.netpulse.STOP_MONITORING"
        const val ACTION_UPDATE_SETTINGS = "com.example.netpulse.UPDATE_SETTINGS"
    }
}
