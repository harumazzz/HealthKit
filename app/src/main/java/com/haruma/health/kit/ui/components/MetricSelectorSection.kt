package com.haruma.health.kit.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haruma.health.kit.R
import com.haruma.health.kit.ui.detail.DetailMetric
import com.haruma.health.kit.ui.detail.DetailTimeRange
import com.haruma.health.kit.ui.theme.MetricHeartRate
import com.haruma.health.kit.ui.theme.MetricSleep
import com.haruma.health.kit.ui.theme.RingMove
import com.haruma.health.kit.ui.theme.RingSteps

@Composable
fun MetricSelectorSection(
    timeRange: DetailTimeRange,
    selectedMetric: DetailMetric,
    onTimeRangeSelected: (DetailTimeRange) -> Unit,
    onMetricSelected: (DetailMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_trends),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        val days7Label = stringResource(R.string.time_range_7d)
        val days30Label = stringResource(R.string.time_range_30d)

        SegmentedTabBar(
            items = listOf(DetailTimeRange.PAST_7_DAYS, DetailTimeRange.PAST_30_DAYS),
            selectedItem = timeRange,
            onItemSelected = onTimeRangeSelected,
            itemLabel = { range ->
                when (range) {
                    DetailTimeRange.PAST_7_DAYS -> days7Label
                    DetailTimeRange.PAST_30_DAYS -> days30Label
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricChipItem(
                selected = selectedMetric == DetailMetric.STEPS,
                label = stringResource(R.string.steps),
                icon = Icons.Default.DirectionsWalk,
                color = RingSteps,
                onClick = { onMetricSelected(DetailMetric.STEPS) }
            )
            MetricChipItem(
                selected = selectedMetric == DetailMetric.CALORIES,
                label = stringResource(R.string.calories_burned),
                icon = Icons.Default.LocalFireDepartment,
                color = RingMove,
                onClick = { onMetricSelected(DetailMetric.CALORIES) }
            )
            MetricChipItem(
                selected = selectedMetric == DetailMetric.SLEEP,
                label = stringResource(R.string.sleep),
                icon = Icons.Default.Bedtime,
                color = MetricSleep,
                onClick = { onMetricSelected(DetailMetric.SLEEP) }
            )
            MetricChipItem(
                selected = selectedMetric == DetailMetric.HEART_RATE,
                label = stringResource(R.string.heart_rate),
                icon = Icons.Default.Favorite,
                color = MetricHeartRate,
                onClick = { onMetricSelected(DetailMetric.HEART_RATE) }
            )
        }
    }
}

@Composable
fun MetricChipItem(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = androidx.compose.foundation.shape.CircleShape,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color,
            selectedLeadingIconColor = color
        ),
        modifier = modifier
    )
}
