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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.model.SpeedSample
import com.example.telemetry.NetworkTelemetryManager
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.TextSecondary

@Composable
fun LiveSpeedGraph(
    samples: List<SpeedSample>,
    modifier: Modifier = Modifier
) {
    val maxDownload = samples.maxOfOrNull { it.downSpeedBps }?.coerceAtLeast(1024L * 1024L) ?: (1024L * 1024L)
    val maxUpload = samples.maxOfOrNull { it.upSpeedBps }?.coerceAtLeast(512L * 1024L) ?: (512L * 1024L)
    val peakOverall = maxOf(maxDownload, maxUpload)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Download legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = CyanNeon
                    ) {}
                    Text(
                        text = "Download",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                // Upload legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = EmeraldNeon
                    ) {}
                    Text(
                        text = "Upload",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "Peak: ${NetworkTelemetryManager.formatSpeed(peakOverall)}",
                style = MaterialTheme.typography.labelSmall,
                color = CyanNeon,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw background grid lines (3 horizontal lines)
                val gridLines = 3
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

                // 1. Download Path & Gradient Fill
                val dlPath = Path()
                val dlFillPath = Path()

                samples.forEachIndexed { index, sample ->
                    val x = index * stepX
                    val normalizedY = (sample.downSpeedBps.toFloat() / peakOverall.toFloat()).coerceIn(0f, 1f)
                    val y = height - (normalizedY * (height - 8f)) - 4f

                    if (index == 0) {
                        dlPath.moveTo(x, y)
                        dlFillPath.moveTo(x, height)
                        dlFillPath.lineTo(x, y)
                    } else {
                        dlPath.lineTo(x, y)
                        dlFillPath.lineTo(x, y)
                    }
                }
                dlFillPath.lineTo(width, height)
                dlFillPath.close()

                // Draw download fill
                drawPath(
                    path = dlFillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CyanNeon.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw download line
                drawPath(
                    path = dlPath,
                    color = CyanNeon,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // 2. Upload Path & Gradient Fill
                val ulPath = Path()
                val ulFillPath = Path()

                samples.forEachIndexed { index, sample ->
                    val x = index * stepX
                    val normalizedY = (sample.upSpeedBps.toFloat() / peakOverall.toFloat()).coerceIn(0f, 1f)
                    val y = height - (normalizedY * (height - 8f)) - 4f

                    if (index == 0) {
                        ulPath.moveTo(x, y)
                        ulFillPath.moveTo(x, height)
                        ulFillPath.lineTo(x, y)
                    } else {
                        ulPath.lineTo(x, y)
                        ulFillPath.lineTo(x, y)
                    }
                }
                ulFillPath.lineTo(width, height)
                ulFillPath.close()

                // Draw upload fill
                drawPath(
                    path = ulFillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            EmeraldNeon.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw upload line
                drawPath(
                    path = ulPath,
                    color = EmeraldNeon,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}
