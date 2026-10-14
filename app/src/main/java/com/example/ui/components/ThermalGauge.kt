package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
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
import com.example.ui.theme.AmberNeon
import com.example.ui.theme.CoralNeon
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun ThermalGauge(
    tempC: Float,
    tempF: Float,
    useFahrenheit: Boolean,
    thresholdC: Float = 42f,
    modifier: Modifier = Modifier
) {
    // Normal temp range 20°C to 55°C
    val minTemp = 20f
    val maxTemp = 55f
    val normalizedFraction = ((tempC - minTemp) / (maxTemp - minTemp)).coerceIn(0.05f, 1f)

    val animatedFraction by animateFloatAsState(
        targetValue = normalizedFraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "tempFraction"
    )

    val (thermalColor, thermalStatus) = when {
        tempC >= thresholdC -> CoralNeon to "Critical Warm"
        tempC >= 40f -> AmberNeon to "Warm"
        tempC >= 32f -> EmeraldNeon to "Normal"
        else -> CyanNeon to "Cool"
    }

    val displayTemp = if (useFahrenheit) {
        String.format(Locale.US, "%.1f°F", tempF)
    } else {
        String.format(Locale.US, "%.1f°C", tempC)
    }

    Box(
        modifier = modifier.size(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            val startAngle = 135f
            val sweepTotal = 270f

            // Background arc track
            drawArc(
                color = DarkBorder,
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            val activeSweep = sweepTotal * animatedFraction
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        CyanNeon,
                        EmeraldNeon,
                        AmberNeon,
                        CoralNeon
                    )
                ),
                startAngle = startAngle,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = "Temperature",
                    tint = thermalColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = displayTemp,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 24.sp
                    ),
                    color = TextPrimary
                )
            }
            Text(
                text = thermalStatus,
                style = MaterialTheme.typography.labelSmall,
                color = thermalColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Alert at ${thresholdC.toInt()}°C",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextSecondary
            )
        }
    }
}
