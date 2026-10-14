package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NetworkSpeedInfo
import com.example.model.SimSignalInfo
import com.example.telemetry.NetworkTelemetryManager
import com.example.ui.BandwidthResult
import com.example.ui.components.GlowCard
import com.example.ui.components.SignalBarIndicator
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
fun NetworkSimScreen(
    networkInfo: NetworkSpeedInfo,
    simInfo: SimSignalInfo,
    isBandwidthTesting: Boolean,
    bandwidthResult: BandwidthResult?,
    onRunBandwidthTest: () -> Unit,
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
        // Wi-Fi Hardware & Connection Card
        GlowCard(
            accentGlowColor = if (networkInfo.isWifi) CyanNeon else DarkBorder,
            modifier = Modifier.testTag("wifi_details_card")
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
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = DarkSurfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = "Wi-Fi",
                                    tint = if (networkInfo.isWifi) CyanNeon else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Wi-Fi Network",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = if (networkInfo.isWifi) networkInfo.networkName else "Not Connected",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (networkInfo.isWifi) CyanNeon else TextSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Text(
                            text = if (networkInfo.isWifi) "CONNECTED" else "STANDBY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (networkInfo.isWifi) CyanNeon else TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Wi-Fi specs grid
                val freqLabel = when {
                    networkInfo.wifiFrequency in 2400..2500 -> "2.4 GHz Band"
                    networkInfo.wifiFrequency in 4900..5900 -> "5 GHz High-Speed"
                    networkInfo.wifiFrequency > 5900 -> "6 GHz Wi-Fi 6E"
                    else -> "N/A"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "LINK SPEED",
                        value = if (networkInfo.wifiLinkSpeed > 0) "${networkInfo.wifiLinkSpeed} Mbps" else "N/A",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "FREQUENCY",
                        value = freqLabel,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "SIGNAL RSSI",
                        value = if (networkInfo.wifiRssi != 0) "${networkInfo.wifiRssi} dBm" else "N/A",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "LOCAL IP",
                        value = networkInfo.ipAddress,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // SIM & Cellular Telemetry Card
        GlowCard(
            accentGlowColor = EmeraldNeon,
            modifier = Modifier.testTag("cellular_details_card")
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
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = DarkSurfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SimCard,
                                    contentDescription = "SIM Card",
                                    tint = EmeraldNeon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "SIM & Cellular Carrier",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = simInfo.carrierName,
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldNeon
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Text(
                            text = simInfo.simState.uppercase(Locale.US),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (simInfo.simState == "Ready") EmeraldNeon else AmberNeon,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SignalBarIndicator(simInfo = simInfo)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "NETWORK TECH",
                        value = simInfo.networkType,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "DATA ROAMING",
                        value = if (simInfo.isDataRoaming) "Active (Roaming)" else "Disabled (Home)",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "MOBILE RX",
                        value = NetworkTelemetryManager.formatBytes(networkInfo.mobileRxBytes),
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "MOBILE TX",
                        value = NetworkTelemetryManager.formatBytes(networkInfo.mobileTxBytes),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live Speed & Latency Test Tool
        GlowCard(
            accentGlowColor = AmberNeon,
            modifier = Modifier.testTag("speed_test_card")
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
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = "Speed Test",
                            tint = AmberNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Bandwidth & Latency Benchmark",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Runs a live throughput download stream and HTTP ping roundtrip test.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (bandwidthResult != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "PING",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "${bandwidthResult.pingMs} ms",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (bandwidthResult.pingMs < 60) EmeraldNeon else AmberNeon
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "DOWNLOAD",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f Mbps", bandwidthResult.downloadMbps),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = CyanNeon
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "UPLOAD",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f Mbps", bandwidthResult.uploadMbps),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = EmeraldNeon
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = onRunBandwidthTest,
                    enabled = !isBandwidthTesting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberNeon,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("run_speed_test_button")
                ) {
                    if (isBandwidthTesting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Measuring Speed...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Test",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (bandwidthResult == null) "Run Speed & Ping Test" else "Test Again",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MetricBox(
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
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
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
