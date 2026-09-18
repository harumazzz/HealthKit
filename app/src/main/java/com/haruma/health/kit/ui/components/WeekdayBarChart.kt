package com.haruma.health.kit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.haruma.health.kit.ui.detail.DayAverage
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore

@Composable
fun WeekdayBarChart(
    dayAverages: List<DayAverage>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    if (dayAverages.all { it.value == 0.0 }) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Not enough data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    val peakValue = dayAverages.maxOfOrNull { it.value } ?: 0.0

    LaunchedEffect(dayAverages) {
        modelProducer.runTransaction {
            columnModel { series(dayAverages.map { it.value.toFloat() }) }
        }
    }

    val accentFill = Fill(accentColor.copy(alpha = 0.75f))
    val peakFill = Fill(MaterialTheme.colorScheme.tertiary)
    val barShape = RoundedCornerShape(40)

    val columnProvider = remember(dayAverages, accentFill, peakFill) {
        object : ColumnCartesianLayer.ColumnProvider {
            override fun getColumn(
                entry: ColumnCartesianLayerModel.Entry,
                extraStore: ExtraStore,
            ): LineComponent {
                val index = entry.x.toInt()
                val isPeak = dayAverages.getOrNull(index)?.value == peakValue && peakValue > 0.0
                return LineComponent(
                    fill = if (isPeak) peakFill else accentFill,
                    thickness = 22.dp,
                    shape = barShape,
                )
            }

            override fun getWidestSeriesColumn(
                seriesKey: Any,
                seriesIndex: Int,
                extraStore: ExtraStore,
            ): LineComponent = LineComponent(
                fill = accentFill,
                thickness = 22.dp,
                shape = barShape,
            )
        }
    }

    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val labelStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp,
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Avg by Day of Week",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(columnProvider = columnProvider),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = { _, value, _ ->
                        val index = value.toInt()
                        val rawLabel = dayAverages.getOrNull(index)?.dayLabel
                        val result = if (!rawLabel.isNullOrBlank()) rawLabel else (index + 1).toString()
                        result.ifBlank { (index + 1).toString() }
                    },
                    itemPlacer = remember {
                        HorizontalAxis.ItemPlacer.aligned(spacing = { 1 })
                    },
                    line = rememberLineComponent(fill = Fill(outlineColor)),
                    tick = rememberLineComponent(fill = Fill.Transparent),
                    guideline = rememberLineComponent(fill = Fill.Transparent),
                    label = rememberTextComponent(style = labelStyle),
                ),
                getXStep = { _, _, _ -> 1.0 },
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(bottom = 4.dp),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            zoomState = rememberVicoZoomState(zoomEnabled = false),
        )
    }
}
