package com.haruma.health.kit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruma.health.kit.ui.detail.ChartBarData
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import java.time.format.DateTimeFormatter

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

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(bars) {
        modelProducer.runTransaction {
            columnSeries {
                series(bars.map { it.value.toFloat() })
            }
        }
    }

    val selectedBar = selectedBarIndex?.let { bars.getOrNull(it) }

    val barFill = Fill(barColor.copy(alpha = 0.88f))
    val peakBarFill = Fill(peakBarColor)
    val selectedBarFill = Fill(selectedBarColor)
    val thicknessValue = if (bars.size <= 7) 20.dp else 10.dp
    val barShape = RoundedCornerShape(40)

    val columnProvider = remember(bars, selectedBarIndex, barFill, peakBarFill, selectedBarFill) {
        object : ColumnCartesianLayer.ColumnProvider {
            override fun getColumn(
                entry: ColumnCartesianLayerModel.Entry,
                extraStore: ExtraStore,
            ): LineComponent {
                val index = entry.x.toInt()
                val bar = bars.getOrNull(index)
                val fill = when {
                    index == selectedBarIndex -> selectedBarFill
                    bar?.isPeak == true -> peakBarFill
                    else -> barFill
                }
                return LineComponent(
                    fill = fill,
                    thickness = thicknessValue,
                    shape = barShape,
                )
            }

            override fun getWidestSeriesColumn(
                seriesKey: Any,
                seriesIndex: Int,
                extraStore: ExtraStore,
            ): LineComponent = LineComponent(
                fill = barFill,
                thickness = thicknessValue,
                shape = barShape,
            )
        }
    }

    val showLabelInterval = when {
        bars.size <= 7 -> 1
        bars.size <= 14 -> 2
        else -> 5
    }

    val goalLineDecoration = if (goalValue != null && goalValue > 0.0) {
        listOf(
            HorizontalLine(
                y = { goalValue },
                line = LineComponent(
                    fill = Fill(goalLineColor),
                    thickness = 1.5.dp,
                ),
                labelComponent = TextComponent(
                    textStyle = TextStyle(
                        color = goalLineColor,
                        fontSize = 9.sp,
                    ),
                    padding = Insets(horizontal = 4.dp, vertical = 2.dp),
                ),
                label = { "Goal" },
            )
        )
    } else emptyList()

    Column(modifier = modifier.fillMaxWidth()) {
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
                        color = selectedBarColor
                    )
                }
            }
        }

        val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        val labelStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
        )

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(columnProvider = columnProvider),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = { _, value, _ ->
                        val index = value.toInt()
                        if (index in bars.indices && bars[index].label.isNotBlank()) {
                            bars[index].label
                        } else {
                            (index + 1).toString()
                        }
                    },
                    itemPlacer = remember(showLabelInterval) {
                        HorizontalAxis.ItemPlacer.aligned(spacing = { showLabelInterval })
                    },
                    line = rememberLineComponent(fill = Fill(outlineColor)),
                    tick = rememberLineComponent(fill = Fill.Transparent),
                    guideline = rememberLineComponent(fill = Fill.Transparent),
                    label = rememberTextComponent(style = labelStyle),
                ),
                decorations = goalLineDecoration,
                getXStep = { 1.0 },
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            zoomState = rememberVicoZoomState(zoomEnabled = false),
        )
    }
}
