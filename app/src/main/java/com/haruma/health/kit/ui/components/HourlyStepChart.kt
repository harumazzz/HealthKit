package com.haruma.health.kit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun HourlyStepChart(
    hourlySteps: List<Long>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFFF26859),
    gridLineColor: Color = Color(0xFF2A2E38),
    textColor: Color = Color(0xFF8E95A5)
) {
    val textMeasurer = rememberTextMeasurer()
    val maxStepInHour = (hourlySteps.maxOrNull() ?: 0L).coerceAtLeast(1000L)
    val maxY = (((maxStepInHour + 499) / 500) * 500).coerceAtLeast(1000L)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 8.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height

                val leftMargin = 0f
                val rightMargin = 45.dp.toPx()
                val bottomMargin = 25.dp.toPx()
                val topMargin = 10.dp.toPx()

                val chartWidth = width - leftMargin - rightMargin
                val chartHeight = height - topMargin - bottomMargin

                val yStepsCount = 2
                for (i in 0..yStepsCount) {
                    val yVal = maxY * (yStepsCount - i) / yStepsCount
                    val yPos = topMargin + (chartHeight * i / yStepsCount)

                    drawLine(
                        color = gridLineColor,
                        start = Offset(leftMargin, yPos),
                        end = Offset(leftMargin + chartWidth, yPos),
                        strokeWidth = 1.dp.toPx()
                    )

                    val formattedY = String.format(Locale.US, "%,d", yVal)
                    val textLayout = textMeasurer.measure(
                        text = formattedY,
                        style = TextStyle(color = textColor, fontSize = 11.sp)
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            x = leftMargin + chartWidth + 6.dp.toPx(),
                            y = yPos - textLayout.size.height / 2f
                        )
                    )
                }

                val hours = 24
                val barSpacing = 4.dp.toPx()
                val totalSpacing = barSpacing * (hours - 1)
                val barWidth = ((chartWidth - totalSpacing) / hours).coerceAtLeast(2.dp.toPx())

                for (h in 0 until hours) {
                    val stepCount = hourlySteps.getOrElse(h) { 0L }
                    val barHeight = if (stepCount > 0) {
                        ((stepCount.toFloat() / maxY.toFloat()) * chartHeight).coerceAtLeast(4.dp.toPx())
                    } else {
                        2.dp.toPx()
                    }

                    val xPos = leftMargin + h * (barWidth + barSpacing)
                    val yPos = topMargin + chartHeight - barHeight

                    val currentBarColor = if (stepCount > 0) barColor else barColor.copy(alpha = 0.25f)

                    drawRoundRect(
                        color = currentBarColor,
                        topLeft = Offset(xPos, yPos),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }

                val xTicks = listOf(0, 4, 8, 12, 16, 20, 24)
                for (tick in xTicks) {
                    val labelText = tick.toString()
                    val textLayout = textMeasurer.measure(
                        text = labelText,
                        style = TextStyle(color = textColor, fontSize = 11.sp)
                    )
                    val tickFraction = tick / 24f
                    val xPos = leftMargin + tickFraction * chartWidth - textLayout.size.width / 2f
                    val yPos = topMargin + chartHeight + 6.dp.toPx()

                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(xPos.coerceIn(leftMargin, width - textLayout.size.width.toFloat()), yPos)
                    )
                }
            }
        }
    }
}
