package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SimSignalInfo
import com.example.ui.theme.AmberNeon
import com.example.ui.theme.CoralNeon
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SignalBarIndicator(
    simInfo: SimSignalInfo,
    modifier: Modifier = Modifier
) {
    val barColor = when (simInfo.signalLevel) {
        4 -> EmeraldNeon
        3 -> EmeraldNeon.copy(alpha = 0.9f)
        2 -> AmberNeon
        1 -> CoralNeon
        else -> DarkBorder
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Visual 4-bars gauge
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.height(36.dp)
        ) {
            val barHeights = listOf(10.dp, 18.dp, 26.dp, 36.dp)
            barHeights.forEachIndexed { index, height ->
                val isActive = (index + 1) <= simInfo.signalLevel
                Box(
                    modifier = Modifier
                        .width(7.dp)
                        .height(height)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(if (isActive) barColor else DarkBorder)
                )
            }
        }

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = simInfo.carrierName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
                // Network type pill
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DarkSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = simInfo.networkType,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = EmeraldNeon,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${simInfo.signalLevel}/4 bars",
                    style = MaterialTheme.typography.labelSmall,
                    color = barColor,
                    fontWeight = FontWeight.SemiBold
                )
                if (simInfo.signalDbm != -999) {
                    Text(
                        text = "•  ${simInfo.signalDbm} dBm",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "•  ${simInfo.dataActivity}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
