package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    accentGlowColor: Color = Color.Transparent,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val borderBrush = if (accentGlowColor != Color.Transparent) {
        Brush.linearGradient(
            colors = listOf(
                accentGlowColor.copy(alpha = 0.6f),
                DarkBorder,
                DarkBorder.copy(alpha = 0.3f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                DarkBorder.copy(alpha = 0.8f),
                DarkBorder.copy(alpha = 0.4f)
            )
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        color = DarkSurfaceCard,
        border = BorderStroke(1.dp, borderBrush),
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkSurfaceVariant.copy(alpha = 0.5f),
                            DarkSurfaceCard
                        )
                    )
                )
                .padding(16.dp)
        ) {
            content()
        }
    }
}
