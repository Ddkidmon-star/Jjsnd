package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BreathPattern
import com.example.model.BreathStage
import com.example.model.BreatheState
import com.example.ui.theme.LocalHorizonColors
import kotlin.math.min

@Composable
fun BreatheScreen(
    breatheState: BreatheState,
    isHudVisible: Boolean,
    onSelectPattern: (BreathPattern) -> Unit,
    onToggleBreathing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHorizonColors.current
    val haptics = LocalHapticFeedback.current

    val targetScale = when (breatheState.stage) {
        BreathStage.READY -> 0.56f
        BreathStage.INHALE -> 0.48f + (0.52f * breatheState.stageProgress)
        BreathStage.HOLD_TOP -> 1.0f
        BreathStage.EXHALE -> 1.0f - (0.52f * breatheState.stageProgress)
        BreathStage.HOLD_BOTTOM -> 0.48f
    }

    val orbScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing),
        label = "breath_orb_scale"
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val isLandscape = maxWidth > maxHeight
        val minDim = min(maxWidth.value, maxHeight.value)
        val orbMaxSize = (minDim * if (isLandscape) 0.60f else 0.72f).coerceIn(210f, 340f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            AnimatedVisibility(
                visible = isHudVisible,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(180))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(bottom = if (isLandscape) 12.dp else 24.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surface.copy(alpha = 0.8f))
                        .border(1.dp, colors.mutedText.copy(alpha = 0.16f), RoundedCornerShape(24.dp))
                        .padding(4.dp)
                ) {
                    BreathPattern.entries.forEach { pattern ->
                        val selected = breatheState.pattern == pattern
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selected) colors.accent else colors.surface.copy(alpha = 0f)
                                )
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectPattern(pattern)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("breath_pattern_${pattern.name.lowercase()}")
                        ) {
                            Text(
                                text = pattern.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) colors.background else colors.mutedText
                            )
                        }
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(orbMaxSize.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = size.minDimension / 2f - 8.dp.toPx()
                    val currentRadius = maxRadius * orbScale

                    // Outer cadence boundary ring
                    drawCircle(
                        color = colors.mutedText.copy(alpha = 0.18f),
                        radius = maxRadius,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Soft aura halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colors.accent.copy(alpha = 0.42f),
                                colors.accentSecondary.copy(alpha = 0.16f),
                                colors.background.copy(alpha = 0f)
                            ),
                            center = center,
                            radius = currentRadius * 1.18f
                        ),
                        radius = currentRadius * 1.18f,
                        center = center
                    )

                    // Translucent inner petals / rings
                    drawCircle(
                        color = colors.accent.copy(alpha = 0.18f),
                        radius = currentRadius * 0.92f,
                        center = center
                    )
                    drawCircle(
                        color = colors.accent.copy(alpha = 0.85f),
                        radius = currentRadius * 0.68f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!breatheState.isActive) {
                        Icon(
                            imageVector = Icons.Rounded.Air,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = breatheState.stage.prompt,
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (breatheState.isActive) {
                            "${breatheState.stageRemainingSeconds}s  ·  Cycle ${breatheState.completedCycles + 1}"
                        } else {
                            breatheState.pattern.ratioLabel
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.accent,
                        letterSpacing = 1.4.sp
                    )
                }
            }

            AnimatedVisibility(
                visible = isHudVisible,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(180))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = if (isLandscape) 12.dp else 24.dp)
                ) {
                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleBreathing()
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.background
                        ),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("breathe_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (breatheState.isActive) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (breatheState.isActive) "End Session" else "Begin Breathing",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
