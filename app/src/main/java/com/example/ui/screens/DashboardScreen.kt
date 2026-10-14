package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryInfo
import com.example.model.MonitoringSettings
import com.example.model.NetworkSpeedInfo
import com.example.model.SimSignalInfo
import com.example.model.SpeedSample
import com.example.telemetry.NetworkTelemetryManager
import com.example.ui.components.GlowCard
import com.example.ui.components.LiveSpeedGraph
import com.example.ui.components.SignalBarIndicator
import com.example.ui.components.SpeedometerDial
import com.example.ui.theme.AmberNeon
import com.example.ui.theme.CoralNeon
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun DashboardScreen(
    networkInfo: NetworkSpeedInfo,
    simInfo: SimSignalInfo,
    batteryInfo: BatteryInfo,
    settings: MonitoringSettings,
    isServiceActive: Boolean,
    speedHistory: List<SpeedSample>,
    onToggleService: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onNavigateToBattery: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Control Card
        GlowCard(
            accentGlowColor = if (isServiceActive) EmeraldNeon else DarkBorder,
            modifier = Modifier.testTag("service_status_card")
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
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = CircleShape,
                            color = if (isServiceActive) EmeraldNeon else CoralNeon
                        ) {}
                        Column {
                            Text(
                                text = if (isServiceActive) "MONITOR ACTIVE" else "MONITOR IDLE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (isServiceActive) EmeraldNeon else TextSecondary
                            )
                            Text(
                                text = if (isServiceActive) "Live status bar notifications active" else "Start foreground notification service",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = onToggleService,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isServiceActive) CoralNeon else EmeraldNeon,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("toggle_service_button")
                    ) {
                        Icon(
                            imageVector = if (isServiceActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isServiceActive) "Stop Service" else "Start Service",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isServiceActive) "Stop" else "Start",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live Speedometer & Network Throughput Card
        GlowCard(
            accentGlowColor = CyanNeon,
            modifier = Modifier.testTag("network_speed_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (networkInfo.isWifi) Icons.Default.Wifi else Icons.Default.CellTower,
                            contentDescription = "Active Network",
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = networkInfo.networkName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Text(
                            text = if (networkInfo.isConnected) "ONLINE" else "OFFLINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (networkInfo.isConnected) EmeraldNeon else CoralNeon,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dual Speed Readouts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Download Speed
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Download",
                                tint = CyanNeon,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "DOWNLOAD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = networkInfo.formattedDownSpeed,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 22.sp
                            ),
                            color = CyanNeon
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(44.dp)
                            .background(DarkBorder)
                    )

                    // Upload Speed
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Upload",
                                tint = EmeraldNeon,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "UPLOAD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = networkInfo.formattedUpSpeed,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 22.sp
                            ),
                            color = EmeraldNeon
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Real-time Canvas Graph
                LiveSpeedGraph(samples = speedHistory)

                Spacer(modifier = Modifier.height(12.dp))

                // Session Data Counter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Rx: ${NetworkTelemetryManager.formatBytes(networkInfo.totalRxBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Total Tx: ${NetworkTelemetryManager.formatBytes(networkInfo.totalTxBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // SIM Signal & Carrier Summary Card
        GlowCard(
            accentGlowColor = EmeraldNeon,
            modifier = Modifier
                .clickable { onNavigateToNetwork() }
                .testTag("sim_signal_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SIM & CELLULAR SIGNAL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = TextSecondary
                    )
                    Text(
                        text = "Details →",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldNeon
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                SignalBarIndicator(simInfo = simInfo)
            }
        }

        // Battery Level & Temperature Summary Card
        GlowCard(
            accentGlowColor = AmberNeon,
            modifier = Modifier
                .clickable { onNavigateToBattery() }
                .testTag("battery_temp_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BATTERY & THERMAL STATUS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = TextSecondary
                    )
                    Text(
                        text = "Details →",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberNeon
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Battery info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = DarkSurfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (batteryInfo.isCharging) Icons.Default.Bolt else Icons.Default.Speed,
                                    contentDescription = "Battery",
                                    tint = if (batteryInfo.isCharging) AmberNeon else EmeraldNeon,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "${batteryInfo.level}%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextPrimary
                            )
                            val sign = if (batteryInfo.currentNowMa > 0) "+" else ""
                            Text(
                                text = "${batteryInfo.status} • ${sign}${batteryInfo.currentNowMa} mA (${String.format(Locale.US, "%.1fW", batteryInfo.powerWatts)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (batteryInfo.isCharging) EmeraldNeon else TextSecondary
                            )
                        }
                    }

                    // Temperature info
                    val tempStr = if (settings.useFahrenheit) {
                        String.format(Locale.US, "%.1f°F", batteryInfo.temperatureF)
                    } else {
                        String.format(Locale.US, "%.1f°C", batteryInfo.temperatureC)
                    }

                    val tempColor = when {
                        batteryInfo.temperatureC >= settings.temperatureThresholdC -> CoralNeon
                        batteryInfo.temperatureC >= 40f -> AmberNeon
                        else -> EmeraldNeon
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = "Temperature",
                                tint = tempColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = tempStr,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = tempColor
                            )
                        }
                        Text(
                            text = if (batteryInfo.temperatureC >= settings.temperatureThresholdC) "WARNING HOT" else "Healthy Range",
                            style = MaterialTheme.typography.labelSmall,
                            color = tempColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Quick Tools & Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier
                    .weight(1f)
                    .testTag("refresh_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Refresh")
            }

            OutlinedButton(
                onClick = onNavigateToNotifications,
                modifier = Modifier
                    .weight(1f)
                    .testTag("notification_settings_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CyanNeon
                )
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alerts",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Alerts")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
