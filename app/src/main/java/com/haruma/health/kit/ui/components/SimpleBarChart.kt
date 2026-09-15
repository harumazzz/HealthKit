package com.haruma.health.kit.ui.components

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruma.health.kit.ui.detail.ChartBarData
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Composable
fun SimpleBarChart(
    bars: List<ChartBarData>,
    selectedBarIndex: Int?,
    onBarSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    goalValue: Double? = null,
    barColor: Color = MaterialTheme.colorScheme.primary,
    peakBarColor: Color = MaterialTheme.colorScheme.tertiary,
    selectedBarColor: Color = MaterialTheme.colorScheme.secondary,
    goalLineColor: Color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
) {
    if (bars.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No chart data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(bars) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    val density = LocalDensity.current
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val labelColorArgb = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val goalTextColorArgb = goalLineColor.toArgb()

    val labelPaint = remember(density, labelColorArgb) {
        Paint().apply {
            color = labelColorArgb
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    val goalPaint = remember(density, goalTextColorArgb) {
        Paint().apply {
            color = goalTextColorArgb
            textSize = with(density) { 9.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
    }

    val selectedBar = selectedBarIndex?.let { bars.getOrNull(it) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (selectedBar != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedBar.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedBar.isPeak) {
                            Text(
                                text = "Highest of this period",
                                style = MaterialTheme.typography.labelSmall,
                                color = peakBarColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = selectedBar.formattedValue,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(bars) {
                        detectTapGestures { offset ->
                            val slotWidth = size.width / bars.size
                            val touchedIndex = (offset.x / slotWidth).toInt().coerceIn(0, bars.lastIndex)
                            if (touchedIndex == selectedBarIndex) {
                                onBarSelected(null)
                            } else {
                                onBarSelected(touchedIndex)
                            }
                        }
                    }
                    .pointerInput(bars) {
                        detectHorizontalDragGestures { change, _ ->
                            val slotWidth = size.width / bars.size
                            val touchedIndex = (change.position.x / slotWidth).toInt().coerceIn(0, bars.lastIndex)
                            onBarSelected(touchedIndex)
                        }
                    }
            ) {
                val horizontalPadding = 8.dp.toPx()
                val bottomLabelHeight = 24.dp.toPx()
                val topPadding = 16.dp.toPx()
                val chartHeight = size.height - bottomLabelHeight - topPadding
                val chartBottom = size.height - bottomLabelHeight

                val maxBarValue = bars.maxOfOrNull { it.value } ?: 0.0
                val maxValue = max(maxOf(maxBarValue, goalValue ?: 0.0) * 1.15, 1.0)

                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                drawLine(
                    color = gridColor,
                    start = Offset(0f, chartBottom),
                    end = Offset(size.width, chartBottom),
                    strokeWidth = 1.dp.toPx()
                )

                drawLine(
                    color = gridColor.copy(alpha = 0.25f),
                    start = Offset(0f, chartBottom - (chartHeight * 0.5f)),
                    end = Offset(size.width, chartBottom - (chartHeight * 0.5f)),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashedEffect
                )

                if (goalValue != null && goalValue > 0.0) {
                    val goalRatio = (goalValue / maxValue).toFloat().coerceIn(0f, 1f)
                    val goalY = chartBottom - (chartHeight * goalRatio)

                    drawLine(
                        color = goalLineColor,
                        start = Offset(0f, goalY),
                        end = Offset(size.width, goalY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dashedEffect
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        "Goal",
                        size.width - 6.dp.toPx(),
                        goalY - 4.dp.toPx(),
                        goalPaint
                    )
                }

                val slotWidth = size.width / bars.size
                val barWidth = when {
                    bars.size <= 7 -> (slotWidth * 0.55f).coerceAtMost(32.dp.toPx())
                    else -> (slotWidth * 0.70f).coerceAtLeast(3.dp.toPx())
                }

                val showLabelInterval = when {
                    bars.size <= 7 -> 1
                    bars.size <= 14 -> 2
                    else -> 5
                }

                bars.forEachIndexed { index, bar ->
                    val centerX = (index * slotWidth) + (slotWidth / 2f)
                    val isSelected = index == selectedBarIndex
                    val animatedRatio = (bar.value / maxValue).toFloat().coerceIn(0f, 1f) * animationProgress.value

                    val barHeight = max(chartHeight * animatedRatio, if (bar.value > 0.0) 4.dp.toPx() else 2.dp.toPx())
                    val barTop = chartBottom - barHeight
                    val barLeft = centerX - (barWidth / 2f)

                    val color = when {
                        isSelected -> selectedBarColor
                        bar.isPeak -> peakBarColor
                        else -> barColor.copy(alpha = 0.88f)
                    }

                    if (isSelected) {
                        drawRoundRect(
                            color = selectedBarColor.copy(alpha = 0.18f),
                            topLeft = Offset(index * slotWidth, topPadding),
                            size = Size(slotWidth, chartHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(barLeft, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )

                    if (index % showLabelInterval == 0 || index == bars.lastIndex) {
                        drawContext.canvas.nativeCanvas.drawText(
                            bar.label,
                            centerX,
                            size.height - 4.dp.toPx(),
                            labelPaint
                        )
                    }
                }
            }
        }
    }
}
