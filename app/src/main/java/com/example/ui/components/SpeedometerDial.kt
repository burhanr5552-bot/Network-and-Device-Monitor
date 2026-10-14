package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerDial(
    speedFormatted: String,
    speedBps: Long,
    label: String,
    accentColor: Color = CyanNeon,
    modifier: Modifier = Modifier
) {
    // Benchmark 100 Mbps = 12.5 MB/s
    val maxBps = 100L * 1024L * 1024L / 8L
    val targetFraction = (speedBps.toFloat() / maxBps.toFloat()).coerceIn(0.02f, 1f)

    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 600),
        label = "speedDial"
    )

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            val startAngle = 135f
            val sweepAngleTotal = 270f

            // Background track
            drawArc(
                color = DarkBorder,
                startAngle = startAngle,
                sweepAngle = sweepAngleTotal,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Arc with glowing gradient
            val sweepAngleActive = sweepAngleTotal * animatedFraction
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.5f),
                        accentColor,
                        accentColor
                    )
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngleActive,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Indicator tick at the active point
            val currentAngleRad = Math.toRadians((startAngle + sweepAngleActive).toDouble())
            val radius = diameter / 2
            val center = Offset(size.width / 2, size.height / 2)
            val tickEnd = Offset(
                x = (center.x + radius * cos(currentAngleRad)).toFloat(),
                y = (center.y + radius * sin(currentAngleRad)).toFloat()
            )

            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = tickEnd
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = speedFormatted,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
