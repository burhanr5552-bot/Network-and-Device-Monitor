package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.MonitoringSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("netpulse_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<MonitoringSettings> = _settings.asStateFlow()

    private fun loadSettings(): MonitoringSettings {
        return MonitoringSettings(
            isServiceRunning = prefs.getBoolean(KEY_SERVICE_RUNNING, false),
            showDataNotification = prefs.getBoolean(KEY_SHOW_DATA, true),
            showChargingNotification = prefs.getBoolean(KEY_SHOW_CHARGING, true),
            showSignalNotification = prefs.getBoolean(KEY_SHOW_SIGNAL, true),
            temperatureAlertEnabled = prefs.getBoolean(KEY_TEMP_ALERT, true),
            temperatureThresholdC = prefs.getFloat(KEY_TEMP_THRESHOLD, 42.0f),
            batteryLowAlertEnabled = prefs.getBoolean(KEY_BATTERY_LOW_ALERT, true),
            batteryLowThreshold = prefs.getInt(KEY_BATTERY_LOW_THRESHOLD, 20),
            batteryFullAlertEnabled = prefs.getBoolean(KEY_BATTERY_FULL_ALERT, true),
            useFahrenheit = prefs.getBoolean(KEY_USE_FAHRENHEIT, false),
            refreshIntervalMs = prefs.getLong(KEY_REFRESH_INTERVAL, 1000L)
        )
    }

    fun updateSettings(transform: (MonitoringSettings) -> MonitoringSettings) {
        val current = _settings.value
        val updated = transform(current)
        _settings.value = updated

        prefs.edit()
            .putBoolean(KEY_SERVICE_RUNNING, updated.isServiceRunning)
            .putBoolean(KEY_SHOW_DATA, updated.showDataNotification)
            .putBoolean(KEY_SHOW_CHARGING, updated.showChargingNotification)
            .putBoolean(KEY_SHOW_SIGNAL, updated.showSignalNotification)
            .putBoolean(KEY_TEMP_ALERT, updated.temperatureAlertEnabled)
            .putFloat(KEY_TEMP_THRESHOLD, updated.temperatureThresholdC)
            .putBoolean(KEY_BATTERY_LOW_ALERT, updated.batteryLowAlertEnabled)
            .putInt(KEY_BATTERY_LOW_THRESHOLD, updated.batteryLowThreshold)
            .putBoolean(KEY_BATTERY_FULL_ALERT, updated.batteryFullAlertEnabled)
            .putBoolean(KEY_USE_FAHRENHEIT, updated.useFahrenheit)
            .putLong(KEY_REFRESH_INTERVAL, updated.refreshIntervalMs)
            .apply()
    }

    companion object {
        private const val KEY_SERVICE_RUNNING = "key_service_running"
        private const val KEY_SHOW_DATA = "key_show_data"
        private const val KEY_SHOW_CHARGING = "key_show_charging"
        private const val KEY_SHOW_SIGNAL = "key_show_signal"
        private const val KEY_TEMP_ALERT = "key_temp_alert"
        private const val KEY_TEMP_THRESHOLD = "key_temp_threshold"
        private const val KEY_BATTERY_LOW_ALERT = "key_battery_low_alert"
        private const val KEY_BATTERY_LOW_THRESHOLD = "key_battery_low_threshold"
        private const val KEY_BATTERY_FULL_ALERT = "key_battery_full_alert"
        private const val KEY_USE_FAHRENHEIT = "key_use_fahrenheit"
        private const val KEY_REFRESH_INTERVAL = "key_refresh_interval"
    }
}
