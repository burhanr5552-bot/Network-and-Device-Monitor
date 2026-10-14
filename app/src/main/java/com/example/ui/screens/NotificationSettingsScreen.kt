package com.example.ui.screens

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.NetPulseApplication
import com.example.model.MonitoringSettings
import com.example.ui.components.GlowCard
import com.example.ui.theme.AmberNeon
import com.example.ui.theme.CoralNeon
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NotificationSettingsScreen(
    settings: MonitoringSettings,
    isServiceActive: Boolean,
    onToggleService: () -> Unit,
    onUpdateSettings: ((MonitoringSettings) -> MonitoringSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Service Switch Card
        GlowCard(
            accentGlowColor = if (isServiceActive) EmeraldNeon else DarkBorder,
            modifier = Modifier.testTag("notification_master_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notifications",
                            tint = if (isServiceActive) EmeraldNeon else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Persistent Monitoring Service",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = if (isServiceActive) "3 separate notifications active" else "Service is stopped",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isServiceActive) EmeraldNeon else TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isServiceActive,
                        onCheckedChange = { onToggleService() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldNeon,
                            checkedTrackColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("master_service_switch")
                    )
                }
            }
        }

        // Separate Notifications Configuration Card
        GlowCard(
            accentGlowColor = CyanNeon,
            modifier = Modifier.testTag("separate_notifications_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SEPARATE NOTIFICATIONS IN STATUS BAR",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Each metric runs as its own separate notification and dynamically renders the live measured value (speed number, battery %, and signal bars) directly inside the status bar icon.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notification 1: Data Speed
                NotificationToggleRow(
                    icon = Icons.Default.Speed,
                    iconTint = CyanNeon,
                    title = "1. Data Speed Notification",
                    subtitle = "Real-time upload & download speeds (MB/s) and session usage",
                    checked = settings.showDataNotification,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(showDataNotification = checked) }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Notification 2: Charging & Real-Time Battery
                NotificationToggleRow(
                    icon = Icons.Default.BatteryChargingFull,
                    iconTint = AmberNeon,
                    title = "2. Charging & Battery Notification",
                    subtitle = "Live power (Watts), instant current (mA), status & temperature",
                    checked = settings.showChargingNotification,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(showChargingNotification = checked) }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Notification 3: SIM Signal Strength
                NotificationToggleRow(
                    icon = Icons.Default.CellTower,
                    iconTint = EmeraldNeon,
                    title = "3. SIM Signal Notification",
                    subtitle = "Carrier name, 4-bar signal level, dBm strength & 5G/4G tech",
                    checked = settings.showSignalNotification,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(showSignalNotification = checked) }
                    }
                )
            }
        }

        // Hardware Alerts & Safety Thresholds Card
        GlowCard(
            accentGlowColor = CoralNeon,
            modifier = Modifier.testTag("hardware_alerts_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "HARDWARE & THERMAL ALERTS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotificationToggleRow(
                    icon = Icons.Default.Thermostat,
                    iconTint = CoralNeon,
                    title = "High Temperature Warning",
                    subtitle = "Sound/vibrate alert if battery exceeds ${settings.temperatureThresholdC.toInt()}°C",
                    checked = settings.temperatureAlertEnabled,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(temperatureAlertEnabled = checked) }
                    }
                )

                if (settings.temperatureAlertEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = settings.temperatureThresholdC,
                        onValueChange = { onUpdateSettings { s -> s.copy(temperatureThresholdC = it) } },
                        valueRange = 36f..52f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = CoralNeon,
                            activeTrackColor = CoralNeon
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                NotificationToggleRow(
                    icon = Icons.Default.BatteryChargingFull,
                    iconTint = EmeraldNeon,
                    title = "Battery Fully Charged (100%)",
                    subtitle = "Alerts when fully charged to prevent overcharging",
                    checked = settings.batteryFullAlertEnabled,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(batteryFullAlertEnabled = checked) }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                NotificationToggleRow(
                    icon = Icons.Default.BatteryAlert,
                    iconTint = CoralNeon,
                    title = "Low Battery Alert (≤ 20%)",
                    subtitle = "Alerts when battery level drops below 20%",
                    checked = settings.batteryLowAlertEnabled,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(batteryLowAlertEnabled = checked) }
                    }
                )
            }
        }

        // Measurement Preferences & Refresh Interval
        GlowCard(
            accentGlowColor = DarkBorder,
            modifier = Modifier.testTag("preferences_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PREFERENCES & REFRESH INTERVAL",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Temperature Unit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !settings.useFahrenheit,
                            onClick = { onUpdateSettings { it.copy(useFahrenheit = false) } },
                            label = { Text("°C Celsius") }
                        )
                        FilterChip(
                            selected = settings.useFahrenheit,
                            onClick = { onUpdateSettings { it.copy(useFahrenheit = true) } },
                            label = { Text("°F Fahrenheit") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Update Interval",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(500L to "0.5s", 1000L to "1s", 2000L to "2s").forEach { (interval, label) ->
                            FilterChip(
                                selected = settings.refreshIntervalMs == interval,
                                onClick = { onUpdateSettings { it.copy(refreshIntervalMs = interval) } },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }

        // Test Separate Notifications Action
        OutlinedButton(
            onClick = {
                sendTestSeparateNotifications(context)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("send_test_notification_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CyanNeon
            )
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "Test Separate Notifications",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Preview 3 Separate Notifications", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun NotificationToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = EmeraldNeon,
                checkedTrackColor = DarkSurfaceVariant
            )
        )
    }
}

private fun sendTestSeparateNotifications(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val contentIntent = PendingIntent.getActivity(
        context,
        99,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 1. Separate Data Notification with measured speed in status bar icon
    val speedIcon = try {
        com.example.service.NotificationIconGenerator.createSpeedIcon(12L * 1024L * 1024L)
    } catch (_: Exception) {
        androidx.core.graphics.drawable.IconCompat.createWithResource(context, android.R.drawable.stat_sys_download)
    }
    val dataNotification = NotificationCompat.Builder(context, NetPulseApplication.CHANNEL_SPEED)
        .setSmallIcon(speedIcon)
        .setContentTitle("Data: ↓ 12.4 MB/s  ↑ 1.8 MB/s")
        .setContentText("Wi-Fi: Office 5GHz • Total Rx: 412 MB")
        .setContentIntent(contentIntent)
        .setAutoCancel(true)
        .build()
    notificationManager.notify(NetPulseApplication.NOTIFICATION_ID_SPEED, dataNotification)

    // 2. Separate Charging & Battery Measuring Notification with percentage in status bar icon
    val batteryIcon = try {
        com.example.service.NotificationIconGenerator.createBatteryIcon(85, true)
    } catch (_: Exception) {
        androidx.core.graphics.drawable.IconCompat.createWithResource(context, android.R.drawable.stat_sys_upload_done)
    }
    val chargingNotification = NotificationCompat.Builder(context, NetPulseApplication.CHANNEL_BATTERY)
        .setSmallIcon(batteryIcon)
        .setContentTitle("Battery: 85% • Charging (+1850 mA)")
        .setContentText("Rapid Charging (7.4W) • 32.8°C • 4080 mV • ~3825 mAh")
        .setContentIntent(contentIntent)
        .setAutoCancel(true)
        .build()
    notificationManager.notify(NetPulseApplication.NOTIFICATION_ID_BATTERY, chargingNotification)

    // 3. Separate SIM Signal Notification with 4-bars in status bar icon
    val signalIcon = try {
        com.example.service.NotificationIconGenerator.createSignalIcon(4)
    } catch (_: Exception) {
        androidx.core.graphics.drawable.IconCompat.createWithResource(context, android.R.drawable.stat_sys_phone_call)
    }
    val simNotification = NotificationCompat.Builder(context, NetPulseApplication.CHANNEL_SIM)
        .setSmallIcon(signalIcon)
        .setContentTitle("SIM Signal: Google Fi [4/4 bars]")
        .setContentText("5G NR • -79 dBm • Roaming: No • Active")
        .setContentIntent(contentIntent)
        .setAutoCancel(true)
        .build()
    notificationManager.notify(NetPulseApplication.NOTIFICATION_ID_SIM, simNotification)
}
