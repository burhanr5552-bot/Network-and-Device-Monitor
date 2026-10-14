package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class NetPulseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val overviewChannel = NotificationChannel(
                CHANNEL_OVERVIEW,
                "Live Telemetry Overview",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time combined network, SIM, and battery metrics"
                setShowBadge(false)
            }

            val speedChannel = NotificationChannel(
                CHANNEL_SPEED,
                "Network Speed & Data",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time Wi-Fi & cellular upload and download speeds"
                setShowBadge(false)
            }

            val simChannel = NotificationChannel(
                CHANNEL_SIM,
                "SIM & Signal Strength",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live carrier, SIM status, and signal strength level"
                setShowBadge(false)
            }

            val batteryChannel = NotificationChannel(
                CHANNEL_BATTERY,
                "Battery & Temperature",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows continuous battery percentage and device temperature"
                setShowBadge(false)
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "System & Thermal Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for high battery temperature, low battery, or battery full"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(overviewChannel, speedChannel, simChannel, batteryChannel, alertsChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_OVERVIEW = "channel_overview"
        const val CHANNEL_SPEED = "channel_speed"
        const val CHANNEL_SIM = "channel_sim"
        const val CHANNEL_BATTERY = "channel_battery"
        const val CHANNEL_ALERTS = "channel_alerts"

        const val NOTIFICATION_ID_OVERVIEW = 1001
        const val NOTIFICATION_ID_SPEED = 1002
        const val NOTIFICATION_ID_SIM = 1003
        const val NOTIFICATION_ID_BATTERY = 1004
        const val NOTIFICATION_ID_ALERT = 2001
    }
}
