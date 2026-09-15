package com.haruma.health.kit.ui.settings.dialogs

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haruma.health.kit.R

@Composable
fun RateUsDialog(
    onRatingSubmitted: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRating by remember { mutableIntStateOf(0) }

    val animatedRatingProgress by animateFloatAsState(
        targetValue = selectedRating.toFloat(),
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "starRatingFillAnimation"
    )

    val activeStarColor = Color(0xFFFFB800)
    val inactiveStarColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.rate_us_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.rate_us_dialog_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { index ->
                        val fillRatio = (animatedRatingProgress - (index - 1)).coerceIn(0f, 1f)

                        val starColor by animateColorAsState(
                            targetValue = if (fillRatio > 0.5f) activeStarColor else inactiveStarColor,
                            animationSpec = tween(durationMillis = 200),
                            label = "starColorAnimation"
                        )

                        val isSelected = index <= selectedRating
                        val scaleFactor by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
                            label = "starScaleAnimation"
                        )

                        val interactionSource = remember { MutableInteractionSource() }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .scale(scaleFactor)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    selectedRating = index
                                }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = if (fillRatio > 0.5f) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = null,
                                tint = starColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedRating > 0) {
                    Text(
                        text = stringResource(R.string.feedback_rating_label, selectedRating),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = stringResource(R.string.rate_us_rating_hint),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedRating > 0) {
                        onRatingSubmitted(selectedRating)
                    }
                },
                enabled = selectedRating > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = stringResource(R.string.rate_us_submit))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
