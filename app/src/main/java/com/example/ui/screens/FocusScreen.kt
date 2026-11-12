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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FocusPhase
import com.example.model.FocusTimerState
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalHorizonColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun FocusScreen(
    focusState: FocusTimerState,
    completedSessions: Int,
    isHudVisible: Boolean,
    onSelectPhase: (FocusPhase) -> Unit,
    onSelectPresetMinutes: (Int) -> Unit,
    onAdjustMinutes: (Int) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHorizonColors.current
    val haptics = LocalHapticFeedback.current

    val animatedProgress by animateFloatAsState(
        targetValue = focusState.progress,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "focus_progress"
    )

    val mins = focusState.remainingSeconds / 60
    val secs = focusState.remainingSeconds % 60
    val countdownLabel = "%02d:%02d".format(mins, secs)

    val endMillis = System.currentTimeMillis() + (focusState.remainingSeconds * 1000L)
    val endTimeLabel = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(endMillis))

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val isLandscape = maxWidth > maxHeight
        val minDim = min(maxWidth.value, maxHeight.value)
        val ringSize = (minDim * if (isLandscape) 0.62f else 0.72f).coerceIn(210f, 340f)

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
                    FocusPhase.entries.forEach { phase ->
                        val selected = focusState.phase == phase
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selected) colors.accent else colors.surface.copy(alpha = 0f)
                                )
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectPhase(phase)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("focus_phase_${phase.name.lowercase()}")
                        ) {
                            Text(
                                text = phase.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) colors.background else colors.mutedText
                            )
                        }
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(ringSize.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 10.dp.toPx()
                    val radius = size.minDimension / 2f - strokeW
                    val center = Offset(size.width / 2f, size.height / 2f)

                    drawCircle(
                        color = colors.surfaceElevated,
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeW)
                    )

                    val sweep = 360f * animatedProgress
                    if (sweep > 0.5f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                0f to colors.accentSecondary,
                                0.5f to colors.accent,
                                1f to colors.accentSecondary
                            ),
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2f, radius * 2f),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )

                        val tipAngle = ((sweep - 90f) * PI / 180.0).toFloat()
                        val tip = Offset(
                            center.x + radius * cos(tipAngle),
                            center.y + radius * sin(tipAngle)
                        )
                        drawCircle(
                            color = colors.accent.copy(alpha = 0.28f),
                            radius = 14.dp.toPx(),
                            center = tip
                        )
                        drawCircle(
                            color = colors.onBackground,
                            radius = 5.dp.toPx(),
                            center = tip
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (focusState.justCompleted) "SESSION COMPLETE" else focusState.phase.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.accent,
                        letterSpacing = 1.6.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = countdownLabel,
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (ringSize * 0.22f).sp,
                            letterSpacing = (-2).sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (focusState.isRunning) "Ends at $endTimeLabel" else "Completed: $completedSessions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.mutedText
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
                    if (!focusState.isRunning && focusState.remainingSeconds == focusState.totalSeconds) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = { onAdjustMinutes(-1) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = colors.surfaceElevated,
                                    contentColor = colors.onBackground
                                ),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("focus_minus_minute")
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease 1 minute")
                            }

                            listOf(15, 25, 45, 60).forEach { preset ->
                                val isCurrent = focusState.totalSeconds == preset * 60
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isCurrent) colors.accent.copy(alpha = 0.2f) else colors.surface
                                        )
                                        .border(
                                            1.dp,
                                            if (isCurrent) colors.accent else colors.mutedText.copy(alpha = 0.18f),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onSelectPresetMinutes(preset) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .testTag("focus_preset_$preset")
                                ) {
                                    Text(
                                        text = "${preset}m",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isCurrent) colors.accent else colors.mutedText
                                    )
                                }
                            }

                            FilledTonalIconButton(
                                onClick = { onAdjustMinutes(1) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = colors.surfaceElevated,
                                    contentColor = colors.onBackground
                                ),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("focus_plus_minute")
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Increase 1 minute")
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onResetTimer()
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = colors.surfaceElevated,
                                contentColor = colors.onBackground
                            ),
                            modifier = Modifier
                                .size(52.dp)
                                .testTag("focus_reset_button")
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Reset focus timer")
                        }

                        Button(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleTimer()
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.background
                            ),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("focus_toggle_button")
                        ) {
                            Icon(
                                imageVector = when {
                                    focusState.isRunning -> Icons.Rounded.Pause
                                    focusState.justCompleted -> Icons.Rounded.CheckCircle
                                    else -> Icons.Rounded.PlayArrow
                                },
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    focusState.isRunning -> "Pause"
                                    focusState.justCompleted -> "Start Again"
                                    focusState.remainingSeconds < focusState.totalSeconds -> "Resume"
                                    else -> "Start Focus"
                                },
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
}
