package com.example.service

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat
import java.util.Locale

object NotificationIconGenerator {

    private const val ICON_SIZE = 96

    /**
     * Creates a dynamic status bar icon showing the measured network speed value (e.g., "4.2M", "350K", "0K").
     */
    fun createSpeedIcon(bytesPerSec: Long): IconCompat {
        val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val kb = bytesPerSec / 1024.0
        val mb = kb / 1024.0

        val (valueText, unitText) = when {
            mb >= 10.0 -> String.format(Locale.US, "%.0f", mb) to "M"
            mb >= 1.0 -> String.format(Locale.US, "%.1f", mb) to "M"
            kb >= 100.0 -> String.format(Locale.US, "%.0f", kb) to "K"
            kb >= 1.0 -> String.format(Locale.US, "%.0f", kb) to "K"
            else -> "0" to "K"
        }

        // Draw small arrow indicator at the top
        val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val arrowPath = Path().apply {
            moveTo(ICON_SIZE / 2f, 26f)
            lineTo(ICON_SIZE / 2f - 12f, 12f)
            lineTo(ICON_SIZE / 2f - 4f, 12f)
            lineTo(ICON_SIZE / 2f - 4f, 2f)
            lineTo(ICON_SIZE / 2f + 4f, 2f)
            lineTo(ICON_SIZE / 2f + 4f, 12f)
            lineTo(ICON_SIZE / 2f + 12f, 12f)
            close()
        }
        canvas.drawPath(arrowPath, arrowPaint)

        // Draw numerical speed value
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = if (valueText.length > 2) 38f else 46f
        }

        val bounds = Rect()
        textPaint.getTextBounds(valueText, 0, valueText.length, bounds)
        val textY = 66f
        canvas.drawText(valueText, ICON_SIZE / 2f, textY, textPaint)

        // Draw unit label at bottom (K or M)
        val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = 24f
        }
        canvas.drawText(unitText, ICON_SIZE / 2f, 92f, unitPaint)

        return IconCompat.createWithBitmap(bitmap)
    }

    /**
     * Creates a dynamic status bar icon showing the measured battery percentage (e.g. "85" or "+85" if charging).
     */
    fun createBatteryIcon(level: Int, isCharging: Boolean): IconCompat {
        val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        // Draw circular gauge track
        val rectF = RectF(10f, 10f, ICON_SIZE - 10f, ICON_SIZE - 10f)
        val sweepAngle = (level.coerceIn(0, 100) / 100f) * 360f
        canvas.drawArc(rectF, -90f, sweepAngle, false, strokePaint)

        if (isCharging) {
            // Draw a small lightning bolt in the upper right
            val boltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            val boltPath = Path().apply {
                moveTo(ICON_SIZE - 18f, 6f)
                lineTo(ICON_SIZE - 28f, 22f)
                lineTo(ICON_SIZE - 22f, 22f)
                lineTo(ICON_SIZE - 26f, 34f)
                lineTo(ICON_SIZE - 14f, 18f)
                lineTo(ICON_SIZE - 20f, 18f)
                close()
            }
            canvas.drawPath(boltPath, boltPaint)
        }

        // Draw percentage number inside the gauge
        val text = level.coerceIn(0, 100).toString()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = when (text.length) {
                3 -> 36f  // "100"
                else -> 46f // "85"
            }
        }

        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        val textY = (ICON_SIZE / 2f) + (bounds.height() / 2f) - 2f
        canvas.drawText(text, ICON_SIZE / 2f, textY, textPaint)

        return IconCompat.createWithBitmap(bitmap)
    }

    /**
     * Creates a dynamic status bar icon showing the measured SIM signal strength bars and level (e.g. 1-4 bars).
     */
    fun createSignalIcon(level: Int): IconCompat {
        val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        // Draw 4 vertical signal bars
        val barWidth = 14f
        val gap = 6f
        val startX = 10f
        val heights = floatArrayOf(24f, 44f, 64f, 84f)

        for (i in 0..3) {
            val left = startX + i * (barWidth + gap)
            val top = ICON_SIZE - 6f - heights[i]
            val right = left + barWidth
            val bottom = ICON_SIZE - 6f
            val rect = RectF(left, top, right, bottom)

            if (i < level.coerceIn(0, 4)) {
                canvas.drawRoundRect(rect, 3f, 3f, activePaint)
            } else {
                canvas.drawRoundRect(rect, 3f, 3f, inactivePaint)
            }
        }

        return IconCompat.createWithBitmap(bitmap)
    }
}
