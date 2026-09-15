package com.haruma.health.kit.ui.settings.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "starRatingFillAnimation"
    )

    val activeStarColor = Color(0xFFFFB800)
    val inactiveStarColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    val feedbackHint = when (selectedRating) {
        1 -> stringResource(R.string.rate_us_hint_1)
        2 -> stringResource(R.string.rate_us_hint_2)
        3 -> stringResource(R.string.rate_us_hint_3)
        4 -> stringResource(R.string.rate_us_hint_4)
        5 -> stringResource(R.string.rate_us_hint_5)
        else -> stringResource(R.string.rate_us_dialog_desc)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.88f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = activeStarColor,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.rate_us_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedContent(
                    targetState = feedbackHint,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                    },
                    label = "hintTransition"
                ) { hint ->
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { index ->
                        val fillRatio = (animatedRatingProgress - (index - 1)).coerceIn(0f, 1f)
                        val isSelected = index <= selectedRating

                        val starColor by animateColorAsState(
                            targetValue = if (fillRatio > 0.4f) activeStarColor else inactiveStarColor,
                            animationSpec = tween(durationMillis = 200),
                            label = "starColorAnimation"
                        )

                        val scaleFactor by animateFloatAsState(
                            targetValue = if (isSelected) 1.25f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
                            label = "starScaleAnimation"
                        )

                        val interactionSource = remember { MutableInteractionSource() }

                        Icon(
                            imageVector = if (fillRatio > 0.4f) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = starColor,
                            modifier = Modifier
                                .scale(scaleFactor)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    selectedRating = index
                                }
                                .padding(4.dp)
                                .size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (selectedRating > 0) {
                            onRatingSubmitted(selectedRating)
                        }
                    },
                    enabled = selectedRating > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.rate_us_submit),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
