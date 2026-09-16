package com.example.android_temp_indicator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Icon
import kotlin.math.roundToInt

object TemperatureIconGenerator {

    /**
     * Generates a monochrome Icon representing the temperature for the status bar smallIcon.
     * SystemUI uses the alpha channel to tint the icon in the status bar.
     */
    fun createTemperatureIcon(context: Context, temperature: Double): Icon {
        val roundedTemp = temperature.roundToInt()
        val text = when {
            roundedTemp in -9..99 -> "$roundedTemp°"
            roundedTemp in 100..999 -> "$roundedTemp"
            else -> "--"
        }

        val bitmap = generateTextBitmap(text)
        return Icon.createWithBitmap(bitmap)
    }

    private fun generateTextBitmap(text: String): Bitmap {
        // 96x96 px provides crisp rendering on high-DPI displays (such as Poco F5 / 1080x2400)
        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE // Solid white mask; SystemUI tints transparent vs non-transparent pixels
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        // Adjust text size based on character count to fit inside the 24dp boundary without clipping
        val targetTextSize = when {
            text.length <= 2 -> size * 0.78f // e.g. "9°" or "38"
            text.length == 3 -> size * 0.65f // e.g. "38°"
            else -> size * 0.50f             // e.g. "100" or "-10°"
        }
        paint.textSize = targetTextSize

        // Measure text bounds to accurately align vertically and horizontally
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        val x = size / 2f
        val y = (size / 2f) + (bounds.height() / 2f) - bounds.bottom

        canvas.drawText(text, x, y, paint)

        return bitmap
    }
}
