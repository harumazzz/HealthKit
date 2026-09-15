package com.haruma.health.kit.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haruma.health.kit.R
import com.haruma.health.kit.ui.detail.ChartBarData
import com.haruma.health.kit.ui.detail.DetailMetric

@Composable
fun MetricChartCard(
    selectedMetric: DetailMetric,
    chartBars: List<ChartBarData>,
    selectedBarIndex: Int?,
    onBarSelected: (Int?) -> Unit,
    goalValue: Double?,
    formattedGoalValue: String?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedMetric) {
                        DetailMetric.STEPS -> stringResource(R.string.steps)
                        DetailMetric.CALORIES -> stringResource(R.string.calories_burned)
                        DetailMetric.SLEEP -> stringResource(R.string.sleep)
                        DetailMetric.HEART_RATE -> stringResource(R.string.heart_rate)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (formattedGoalValue != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${stringResource(R.string.stat_goal)}: $formattedGoalValue",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SimpleBarChart(
                bars = chartBars,
                selectedBarIndex = selectedBarIndex,
                onBarSelected = onBarSelected,
                goalValue = goalValue,
                barColor = accentColor,
                peakBarColor = MaterialTheme.colorScheme.tertiary,
                selectedBarColor = accentColor.copy(alpha = 1f),
                goalLineColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
            )
        }
    }
}
