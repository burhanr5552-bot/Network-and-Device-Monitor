package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryCurrentSample
import com.example.ui.theme.AmberNeon
import com.example.ui.theme.CoralNeon
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.abs

@Composable
fun LiveCurrentGraph(
    samples: List<BatteryCurrentSample>,
    modifier: Modifier = Modifier
) {
    if (samples.isEmpty()) return

    val currentNow = samples.lastOrNull()?.currentMa ?: 0
    val powerNow = samples.lastOrNull()?.powerWatts ?: 0f

    val isCharging = currentNow >= 0
    val lineColor = if (isCharging) EmeraldNeon else AmberNeon

    val maxAbsCurrent = samples.maxOfOrNull { abs(it.currentMa) }?.coerceAtLeast(500) ?: 1000

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isCharging) "Charging Current Flow" else "Discharge Current Flow",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Text(
                text = "${if (currentNow >= 0) "+" else ""}$currentNow mA (${String.format(Locale.US, "%.2f W", powerNow)})",
                style = MaterialTheme.typography.labelSmall,
                color = lineColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw background horizontal grid
                val gridLines = 2
                for (i in 0..gridLines) {
                    val y = height * (i.toFloat() / gridLines)
                    drawLine(
                        color = DarkBorder.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (samples.size < 2) return@Canvas

                val stepX = width / (samples.size - 1).coerceAtLeast(1)
                val linePath = Path()
                val fillPath = Path()

                samples.forEachIndexed { index, sample ->
                    val x = index * stepX
                    // Normalized between 0 and 1 using max absolute current
                    val normalized = (abs(sample.currentMa).toFloat() / maxAbsCurrent.toFloat()).coerceIn(0.05f, 1f)
                    val y = height - (normalized * (height - 12f)) - 6f

                    if (index == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo(width, height)
                fillPath.close()

                // Gradient fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Stroke line
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}
