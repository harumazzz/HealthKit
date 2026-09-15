package com.haruma.health.kit.ui.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.content.ContextCompat
import com.haruma.health.kit.R

object HealthWidgetBitmapFactory {

    fun renderWidgetBitmap(
        context: Context,
        steps: Long,
        stepsGoal: Long,
        calories: Double,
        sleepMinutes: Long,
        widthPx: Int = 400,
        heightPx: Int = 400
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isDark = uiMode == Configuration.UI_MODE_NIGHT_YES

        val bgColor = if (isDark) Color.parseColor("#141414") else Color.parseColor("#FFFFFF")
        val trackColor = resolveTrackColor(context, isDark)
        val textColor = if (isDark) Color.WHITE else Color.parseColor("#1C1C1E")
        val progressColor = resolveProgressColor(context, isDark)
        val overflowColor = resolveOverflowColor(context, isDark)

        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }

        val cornerRadius = widthPx * 0.16f
        val backgroundRect = RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat())
        canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint)

        val centerX = widthPx / 2f
        val centerY = heightPx / 2f
        val outerRadius = widthPx * 0.38f
        val strokeWidth = widthPx * 0.065f

        val ringBounds = RectF(
            centerX - outerRadius,
            centerY - outerRadius,
            centerX + outerRadius,
            centerY + outerRadius
        )

        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = trackColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawArc(ringBounds, -90f, 360f, false, trackPaint)

        val rawProgress = if (stepsGoal > 0) steps.toFloat() / stepsGoal.toFloat() else 0f

        if (rawProgress > 0f) {
            val baseSweep = (rawProgress * 360f).coerceAtMost(360f)
            val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = progressColor
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawArc(ringBounds, -90f, baseSweep, false, activePaint)

            if (rawProgress > 1f) {
                val overflowSweep = ((rawProgress - 1f) * 360f).coerceAtMost(360f)
                val overflowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = overflowColor
                    style = Paint.Style.STROKE
                    this.strokeWidth = strokeWidth
                    strokeCap = Paint.Cap.ROUND
                }
                canvas.drawArc(ringBounds, -90f, overflowSweep, false, overflowPaint)
            }
        } else {
            val startDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = progressColor
                style = Paint.Style.FILL
            }
            val dotRadius = strokeWidth / 2f
            canvas.drawCircle(centerX, centerY - outerRadius, dotRadius, startDotPaint)
        }

        val rowHeight = heightPx * 0.15f
        val startY = centerY - (rowHeight * 1.0f)
        val iconSize = (heightPx * 0.085f).toInt()
        val spacing = widthPx * 0.03f

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = heightPx * 0.075f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }

        val stepsText = steps.toString()
        val stepsIcon = ContextCompat.getDrawable(context, R.drawable.ic_widget_steps)
        drawMetricRow(
            canvas = canvas,
            icon = stepsIcon,
            text = stepsText,
            centerX = centerX,
            rowCenterY = startY,
            iconSize = iconSize,
            spacing = spacing,
            textPaint = textPaint
        )

        val caloriesText = calories.toInt().toString()
        val caloriesIcon = ContextCompat.getDrawable(context, R.drawable.ic_widget_calories)
        drawMetricRow(
            canvas = canvas,
            icon = caloriesIcon,
            text = caloriesText,
            centerX = centerX,
            rowCenterY = centerY,
            iconSize = iconSize,
            spacing = spacing,
            textPaint = textPaint
        )

        val sleepText = sleepMinutes.toString()
        val sleepIcon = ContextCompat.getDrawable(context, R.drawable.ic_widget_sleep)
        drawMetricRow(
            canvas = canvas,
            icon = sleepIcon,
            text = sleepText,
            centerX = centerX,
            rowCenterY = startY + (rowHeight * 2.0f),
            iconSize = iconSize,
            spacing = spacing,
            textPaint = textPaint
        )

        return bitmap
    }

    private fun resolveProgressColor(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val resId = if (isDark) {
                android.R.color.system_accent1_300
            } else {
                android.R.color.system_accent1_600
            }
            try {
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Exception) {
            }
        }
        return if (isDark) Color.parseColor("#4CAF50") else Color.parseColor("#2E7D32")
    }

    private fun resolveOverflowColor(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val resId = if (isDark) {
                android.R.color.system_accent1_100
            } else {
                android.R.color.system_accent1_800
            }
            try {
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Exception) {
            }
        }
        return if (isDark) Color.parseColor("#81C784") else Color.parseColor("#1B5E20")
    }

    private fun resolveTrackColor(context: Context, isDark: Boolean): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val resId = if (isDark) {
                android.R.color.system_accent1_900
            } else {
                android.R.color.system_accent1_100
            }
            try {
                val color = ContextCompat.getColor(context, resId)
                if (color != 0) return color
            } catch (_: Exception) {
            }
        }
        return if (isDark) Color.parseColor("#1B382B") else Color.parseColor("#E8F5E9")
    }

    private fun drawMetricRow(
        canvas: Canvas,
        icon: Drawable?,
        text: String,
        centerX: Float,
        rowCenterY: Float,
        iconSize: Int,
        spacing: Float,
        textPaint: Paint
    ) {
        val textWidth = textPaint.measureText(text)
        val totalRowWidth = iconSize + spacing + textWidth
        val rowStartX = centerX - (totalRowWidth / 2f)

        icon?.let {
            val iconLeft = rowStartX.toInt()
            val iconTop = (rowCenterY - (iconSize / 2f)).toInt()
            it.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
            it.draw(canvas)
        }

        val textX = rowStartX + iconSize + spacing
        val textMetrics = textPaint.fontMetrics
        val textY = rowCenterY - ((textMetrics.descent + textMetrics.ascent) / 2f)
        canvas.drawText(text, textX, textY, textPaint)
    }
}
