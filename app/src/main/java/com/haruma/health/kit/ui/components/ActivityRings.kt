package com.haruma.health.kit.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haruma.health.kit.R
import com.haruma.health.kit.ui.theme.RingMove
import com.haruma.health.kit.ui.theme.RingMoveBackground
import com.haruma.health.kit.ui.theme.RingSteps
import com.haruma.health.kit.ui.theme.RingStepsBackground
import com.haruma.health.kit.ui.theme.RingWater
import com.haruma.health.kit.ui.theme.RingWaterBackground
import java.util.Locale

@Composable
fun ActivityRings(
    calories: Double,
    caloriesGoal: Double,
    steps: Long,
    stepsGoal: Long,
    water: Int,
    waterGoal: Int,
    modifier: Modifier = Modifier
) {
    val caloriesProgress = if (caloriesGoal > 0) (calories / caloriesGoal).toFloat().coerceIn(0f, 1f) else 0f
    val stepsProgress = if (stepsGoal > 0) (steps.toFloat() / stepsGoal.toFloat()).coerceIn(0f, 1f) else 0f
    val waterProgress = if (waterGoal > 0) (water.toFloat() / waterGoal.toFloat()).coerceIn(0f, 1f) else 0f

    val animatedCaloriesProgress by animateFloatAsState(
        targetValue = caloriesProgress,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "caloriesProgress"
    )
    val animatedStepsProgress by animateFloatAsState(
        targetValue = stepsProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "stepsProgress"
    )
    val animatedWaterProgress by animateFloatAsState(
        targetValue = waterProgress,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "waterProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.activity_summary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(190.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val spacing = 5.dp.toPx()

                    val outerRadius = (size.minDimension - strokeWidth) / 2f
                    val outerTopLeft = Offset((size.width - outerRadius * 2) / 2f, (size.height - outerRadius * 2) / 2f)
                    val outerSize = Size(outerRadius * 2, outerRadius * 2)

                    drawArc(
                        color = RingMoveBackground,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = outerTopLeft,
                        size = outerSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    if (animatedCaloriesProgress > 0f) {
                        drawArc(
                            color = RingMove,
                            startAngle = -90f,
                            sweepAngle = animatedCaloriesProgress * 360f,
                            useCenter = false,
                            topLeft = outerTopLeft,
                            size = outerSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    val middleRadius = outerRadius - strokeWidth - spacing
                    val middleTopLeft = Offset((size.width - middleRadius * 2) / 2f, (size.height - middleRadius * 2) / 2f)
                    val middleSize = Size(middleRadius * 2, middleRadius * 2)

                    drawArc(
                        color = RingStepsBackground,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = middleTopLeft,
                        size = middleSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    if (animatedStepsProgress > 0f) {
                        drawArc(
                            color = RingSteps,
                            startAngle = -90f,
                            sweepAngle = animatedStepsProgress * 360f,
                            useCenter = false,
                            topLeft = middleTopLeft,
                            size = middleSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    val innerRadius = middleRadius - strokeWidth - spacing
                    val innerTopLeft = Offset((size.width - innerRadius * 2) / 2f, (size.height - innerRadius * 2) / 2f)
                    val innerSize = Size(innerRadius * 2, innerRadius * 2)

                    drawArc(
                        color = RingWaterBackground,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = innerTopLeft,
                        size = innerSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    if (animatedWaterProgress > 0f) {
                        drawArc(
                            color = RingWater,
                            startAngle = -90f,
                            sweepAngle = animatedWaterProgress * 360f,
                            useCenter = false,
                            topLeft = innerTopLeft,
                            size = innerSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                RingLegendItem(
                    color = RingMove,
                    label = stringResource(R.string.calories_burned),
                    value = "${calories.toInt()}",
                    target = "/${caloriesGoal.toInt()} ${stringResource(R.string.unit_kcal)}"
                )
                RingLegendItem(
                    color = RingSteps,
                    label = stringResource(R.string.steps),
                    value = String.format(Locale.US, "%,d", steps),
                    target = "/${String.format(Locale.US, "%,d", stepsGoal)}"
                )
                RingLegendItem(
                    color = RingWater,
                    label = stringResource(R.string.water),
                    value = String.format(Locale.US, "%,d", water),
                    target = "/${String.format(Locale.US, "%,d", waterGoal)} ${stringResource(R.string.unit_ml)}"
                )
            }
        }
    }
}

@Composable
private fun RingLegendItem(
    color: Color,
    label: String,
    value: String,
    target: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = color
            ) {}
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = target,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
