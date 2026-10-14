package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryCurrentSample
import com.example.model.BatteryInfo
import com.example.model.MonitoringSettings
import com.example.model.TempSample
import com.example.ui.components.BatteryGauge
import com.example.ui.components.GlowCard
import com.example.ui.components.LiveCurrentGraph
import com.example.ui.components.ThermalGauge
import com.example.ui.theme.AmberNeon
import com.example.ui.theme.CoralNeon
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun BatteryThermalScreen(
    batteryInfo: BatteryInfo,
    settings: MonitoringSettings,
    tempHistory: List<TempSample>,
    currentHistory: List<BatteryCurrentSample>,
    onUpdateSettings: ((MonitoringSettings) -> MonitoringSettings) -> Unit,
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
        // Real-Time Battery Measuring & Power Telemetry Card
        GlowCard(
            accentGlowColor = if (batteryInfo.isCharging) EmeraldNeon else AmberNeon,
            modifier = Modifier.testTag("battery_realtime_measuring_card")
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
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Real-Time Measuring",
                            tint = if (batteryInfo.isCharging) EmeraldNeon else AmberNeon,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "REAL-TIME BATTERY MEASURING",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = TextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Text(
                            text = batteryInfo.chargingSpeedCategory,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = if (batteryInfo.isCharging) EmeraldNeon else AmberNeon,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time Current (mA) and Power (Watts)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val sign = if (batteryInfo.currentNowMa > 0) "+" else ""
                    val powerColor = if (batteryInfo.isCharging) EmeraldNeon else AmberNeon

                    Column {
                        Text(
                            text = "INSTANT CURRENT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${sign}${batteryInfo.currentNowMa} mA",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 26.sp
                            ),
                            color = powerColor
                        )
                        Text(
                            text = "Avg: ${batteryInfo.currentAvgMa} mA",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "REAL-TIME POWER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.2f W", batteryInfo.powerWatts),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 26.sp
                            ),
                            color = powerColor
                        )
                        Text(
                            text = "Capacity: ~${batteryInfo.chargeCounterMah} mAh",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Current Waveform Canvas Graph
                LiveCurrentGraph(samples = currentHistory)
            }
        }

        // Dual Real-Time Telemetry Gauges (Battery % and Thermal Arc)
        GlowCard(
            accentGlowColor = AmberNeon,
            modifier = Modifier.testTag("battery_thermal_gauges_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "CHARGE LEVEL & THERMAL METER",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BatteryGauge(battery = batteryInfo)

                    ThermalGauge(
                        tempC = batteryInfo.temperatureC,
                        tempF = batteryInfo.temperatureF,
                        useFahrenheit = settings.useFahrenheit,
                        thresholdC = settings.temperatureThresholdC
                    )
                }
            }
        }

        // Hardware & Battery Health Card
        GlowCard(
            accentGlowColor = EmeraldNeon,
            modifier = Modifier.testTag("hardware_health_card")
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
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = "Battery Health",
                            tint = EmeraldNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Battery Health & Specs",
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
                            text = batteryInfo.health.uppercase(Locale.US),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (batteryInfo.health == "Good") EmeraldNeon else CoralNeon,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BatteryMetricItem(
                        icon = Icons.Default.Power,
                        title = "POWER SOURCE",
                        value = batteryInfo.chargePlug,
                        modifier = Modifier.weight(1f)
                    )
                    BatteryMetricItem(
                        icon = Icons.Default.ElectricBolt,
                        title = "VOLTAGE",
                        value = "${batteryInfo.voltageMv} mV (${String.format(Locale.US, "%.2f V", batteryInfo.voltageMv / 1000f)})",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BatteryMetricItem(
                        icon = Icons.Default.Shield,
                        title = "TECHNOLOGY",
                        value = batteryInfo.technology,
                        modifier = Modifier.weight(1f)
                    )
                    BatteryMetricItem(
                        icon = Icons.Default.BatteryAlert,
                        title = "STATUS",
                        value = batteryInfo.status,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Thermal Monitoring & Warning Threshold Card
        GlowCard(
            accentGlowColor = CoralNeon,
            modifier = Modifier.testTag("thermal_threshold_card")
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
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = "Thermal Alert",
                            tint = CoralNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Thermal Safety Alert",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                    }

                    Switch(
                        checked = settings.temperatureAlertEnabled,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(temperatureAlertEnabled = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldNeon,
                            checkedTrackColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("thermal_alert_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Triggers a high-priority heads-up notification if battery temperature exceeds the safe threshold.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Threshold Trigger:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${settings.temperatureThresholdC.toInt()}°C",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = CoralNeon
                    )
                }

                Slider(
                    value = settings.temperatureThresholdC,
                    onValueChange = { newThreshold ->
                        onUpdateSettings { it.copy(temperatureThresholdC = newThreshold) }
                    },
                    valueRange = 36f..52f,
                    steps = 15,
                    colors = SliderDefaults.colors(
                        thumbColor = CoralNeon,
                        activeTrackColor = CoralNeon,
                        inactiveTrackColor = DarkBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("temp_threshold_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("36°C (Conservative)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("52°C (High)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }

        // Temperature History Sparkline Card
        if (tempHistory.size >= 2) {
            GlowCard(
                accentGlowColor = CyanNeon,
                modifier = Modifier.testTag("temp_history_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "THERMAL TREND (RECENT SAMPLES)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height

                            val minT = (tempHistory.minOfOrNull { it.tempC } ?: 20f).coerceAtLeast(15f)
                            val maxT = (tempHistory.maxOfOrNull { it.tempC } ?: 45f).coerceAtLeast(minT + 2f)

                            val stepX = width / (tempHistory.size - 1).coerceAtLeast(1)
                            val path = Path()

                            tempHistory.forEachIndexed { i, sample ->
                                val x = i * stepX
                                val normalized = ((sample.tempC - minT) / (maxT - minT)).coerceIn(0f, 1f)
                                val y = height - (normalized * (height - 10f)) - 5f
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }

                            drawPath(
                                path = path,
                                color = AmberNeon,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun BatteryMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                ),
                color = TextPrimary
            )
        }
    }
}
