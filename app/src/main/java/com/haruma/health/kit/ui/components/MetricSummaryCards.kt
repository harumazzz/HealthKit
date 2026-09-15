package com.haruma.health.kit.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haruma.health.kit.R
import com.haruma.health.kit.ui.detail.DetailMetric
import com.haruma.health.kit.ui.detail.SummaryStatistics
import com.haruma.health.kit.ui.theme.RingSteps
import java.time.format.DateTimeFormatter

@Composable
fun MetricSummaryCards(
    stats: SummaryStatistics,
    selectedMetric: DetailMetric,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatItemCard(
                title = stringResource(R.string.stat_daily_avg),
                value = stats.formattedDailyAverage,
                subtitle = null,
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
            StatItemCard(
                title = stringResource(R.string.stat_peak_day),
                value = stats.formattedPeakValue,
                subtitle = stats.peakDate?.format(DateTimeFormatter.ofPattern("MMM d")),
                accentColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatItemCard(
                title = if (selectedMetric == DetailMetric.HEART_RATE) {
                    stringResource(R.string.stat_daily_avg)
                } else {
                    stringResource(R.string.stat_total)
                },
                value = stats.formattedTotalValue,
                subtitle = null,
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )

            val goalText = if (stats.formattedGoalValue != null) "${stats.goalMetPercentage}%" else "N/A"
            val goalSubText = if (stats.formattedGoalValue != null) stringResource(R.string.stat_goal_met) else null

            StatItemCard(
                title = stringResource(R.string.goal_achievement_rate),
                value = goalText,
                subtitle = goalSubText,
                accentColor = if (stats.goalMetPercentage >= 70) RingSteps else accentColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatItemCard(
    title: String,
    value: String,
    subtitle: String?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
