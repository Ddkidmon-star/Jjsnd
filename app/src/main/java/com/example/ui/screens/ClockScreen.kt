package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClockStyle
import com.example.model.HorizonPreferences
import com.example.ui.ClockSnapshot
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalHorizonColors
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ClockScreen(
    snapshot: ClockSnapshot,
    preferences: HorizonPreferences,
    isHudVisible: Boolean,
    onSelectClockStyle: (ClockStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHorizonColors.current

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val isLandscape = maxWidth > maxHeight
        val minDimDp = min(maxWidth.value, maxHeight.value)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            AnimatedContent(
                targetState = preferences.clockStyle,
                transitionSpec = {
                    fadeIn(tween(260)) togetherWith fadeOut(tween(200))
                },
                label = "clock_style_transition"
            ) { style ->
                when (style) {
                    ClockStyle.SOLAR_RING -> SolarRingClock(
                        snapshot = snapshot,
                        preferences = preferences,
                        dialSizeDp = (minDimDp * if (isLandscape) 0.72f else 0.78f).coerceIn(220f, 380f)
                    )
                    ClockStyle.SPLIT_FLIP -> SplitFlipClock(
                        snapshot = snapshot,
                        preferences = preferences,
                        isLandscape = isLandscape
                    )
                    ClockStyle.EDITORIAL -> EditorialClock(
                        snapshot = snapshot,
                        preferences = preferences,
                        isLandscape = isLandscape
                    )
                }
            }

            AnimatedVisibility(
                visible = isHudVisible,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(180))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = if (isLandscape) 14.dp else 28.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surface.copy(alpha = 0.78f))
                        .border(1.dp, colors.mutedText.copy(alpha = 0.16f), RoundedCornerShape(24.dp))
                        .padding(4.dp)
                ) {
                    ClockStyle.entries.forEach { style ->
                        val selected = preferences.clockStyle == style
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selected) colors.accent else colors.surface.copy(alpha = 0f)
                                )
                                .clickable { onSelectClockStyle(style) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("clock_style_${style.name.lowercase()}")
                        ) {
                            Text(
                                text = style.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) colors.background else colors.mutedText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SolarRingClock(
    snapshot: ClockSnapshot,
    preferences: HorizonPreferences,
    dialSizeDp: Float
) {
    val colors = LocalHorizonColors.current
    val smoothSecond = snapshot.second + (snapshot.millis / 1000f)
    val sweepAngle = (smoothSecond / 60f) * 360f

    val hourVal = if (preferences.is24HourFormat) snapshot.hour24 else snapshot.hour12
    val timeText = "%02d:%02d".format(hourVal, snapshot.minute)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(dialSizeDp.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f - 12.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            // Ambient outer ring
            drawCircle(
                color = colors.mutedText.copy(alpha = 0.12f),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 60 precision dial ticks
            for (tick in 0 until 60) {
                val angleRad = ((tick * 6f - 90f) * PI / 180.0).toFloat()
                val isMajor = tick % 5 == 0
                val tickLen = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
                val innerR = radius - tickLen - 8.dp.toPx()
                val outerR = radius - 8.dp.toPx()
                val isPassed = tick <= snapshot.second
                drawLine(
                    color = when {
                        isMajor && isPassed -> colors.accent
                        isPassed -> colors.accent.copy(alpha = 0.6f)
                        isMajor -> colors.mutedText.copy(alpha = 0.45f)
                        else -> colors.mutedText.copy(alpha = 0.18f)
                    },
                    start = Offset(
                        center.x + innerR * cos(angleRad),
                        center.y + innerR * sin(angleRad)
                    ),
                    end = Offset(
                        center.x + outerR * cos(angleRad),
                        center.y + outerR * sin(angleRad)
                    ),
                    strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            if (preferences.showSeconds) {
                val arcDiameter = radius * 2f
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to colors.accent.copy(alpha = 0.25f),
                        0.5f to colors.accentSecondary,
                        1f to colors.accent
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(arcDiameter, arcDiameter),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                val tipAngle = ((sweepAngle - 90f) * PI / 180.0).toFloat()
                val tipOffset = Offset(
                    center.x + radius * cos(tipAngle),
                    center.y + radius * sin(tipAngle)
                )
                drawCircle(
                    color = colors.accent.copy(alpha = 0.3f),
                    radius = 10.dp.toPx(),
                    center = tipOffset
                )
                drawCircle(
                    color = colors.accent,
                    radius = 4.5.dp.toPx(),
                    center = tipOffset
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${snapshot.dayOfWeek.uppercase()}  ·  ${snapshot.dateLabel.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent,
                letterSpacing = 1.6.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = timeText,
                style = TextStyle(
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (dialSizeDp * 0.23f).sp,
                    letterSpacing = (-2).sp,
                    fontFeatureSettings = "tnum"
                ),
                color = colors.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!preferences.is24HourFormat) {
                    Text(
                        text = snapshot.amPm,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.mutedText
                    )
                }
                if (preferences.showSeconds) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceElevated)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "%02d s".format(snapshot.second),
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                fontFeatureSettings = "tnum"
                            ),
                            color = colors.accent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitFlipClock(
    snapshot: ClockSnapshot,
    preferences: HorizonPreferences,
    isLandscape: Boolean
) {
    val colors = LocalHorizonColors.current
    val hourVal = if (preferences.is24HourFormat) snapshot.hour24 else snapshot.hour12
    val hourStr = "%02d".format(hourVal)
    val minStr = "%02d".format(snapshot.minute)
    val secStr = "%02d".format(snapshot.second)

    val animatedDayProgress by animateFloatAsState(
        targetValue = snapshot.dayProgress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "day_progress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 620.dp)
    ) {
        Text(
            text = "${snapshot.greeting}  ·  ${snapshot.dayOfWeek}, ${snapshot.dateLabel}",
            style = MaterialTheme.typography.titleMedium,
            color = colors.mutedText
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 18.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlipTile(
                value = hourStr,
                badge = if (!preferences.is24HourFormat) snapshot.amPm else "24H",
                isLandscape = isLandscape,
                modifier = Modifier.weight(1f)
            )

            FlipTile(
                value = minStr,
                badge = if (preferences.showSeconds) ":$secStr" else null,
                isLandscape = isLandscape,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Day elapsed horizon bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DAY PROGRESS",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.mutedText
                )
                Text(
                    text = "${(snapshot.dayProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedDayProgress.coerceIn(0.02f, 1f))
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(colors.accentSecondary, colors.accent)
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun FlipTile(
    value: String,
    badge: String?,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalHorizonColors.current
    val cardHeight = if (isLandscape) 170.dp else 196.dp
    val fontSize = if (isLandscape) 92.sp else 84.sp

    Box(
        modifier = modifier
            .height(cardHeight)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(colors.surfaceElevated, colors.surface)
                )
            )
            .border(1.dp, colors.mutedText.copy(alpha = 0.16f), RoundedCornerShape(28.dp))
    ) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (slideInVertically(tween(240)) { -it / 4 } + fadeIn(tween(220))) togetherWith
                    (slideOutVertically(tween(180)) { it / 4 } + fadeOut(tween(160)))
            },
            modifier = Modifier.align(Alignment.Center),
            label = "flip_digit"
        ) { digit ->
            Text(
                text = digit,
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    letterSpacing = (-3).sp,
                    fontFeatureSettings = "tnum"
                ),
                color = colors.onBackground
            )
        }

        // Classic split-flap hinge hairline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.Center)
                .background(colors.background.copy(alpha = 0.65f))
        )

        if (badge != null) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            )
        }
    }
}

@Composable
private fun EditorialClock(
    snapshot: ClockSnapshot,
    preferences: HorizonPreferences,
    isLandscape: Boolean
) {
    val colors = LocalHorizonColors.current
    val hourVal = if (preferences.is24HourFormat) snapshot.hour24 else snapshot.hour12
    val hourStr = "%02d".format(hourVal)
    val minStr = "%02d".format(snapshot.minute)

    if (isLandscape) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = snapshot.dayOfWeek.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = snapshot.dateLabel,
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = snapshot.greeting,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.mutedText
                )
                if (preferences.showSeconds) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "%02d SEC".format(snapshot.second),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.accentSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$hourStr:$minStr",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 104.sp,
                        letterSpacing = (-4).sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = colors.onBackground
                )
                if (!preferences.is24HourFormat) {
                    Text(
                        text = snapshot.amPm,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.accent,
                        modifier = Modifier.padding(start = 8.dp, bottom = 18.dp)
                    )
                }
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${snapshot.dayOfWeek.uppercase()}  ·  ${snapshot.dateLabel.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent,
                    letterSpacing = 1.8.sp
                )
                if (!preferences.is24HourFormat) {
                    Text(
                        text = snapshot.amPm,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.mutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = hourStr,
                style = TextStyle(
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 128.sp,
                    lineHeight = 122.sp,
                    letterSpacing = (-5).sp,
                    fontFeatureSettings = "tnum"
                ),
                color = colors.onBackground
            )

            Text(
                text = minStr,
                style = TextStyle(
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 128.sp,
                    lineHeight = 122.sp,
                    letterSpacing = (-5).sp,
                    fontFeatureSettings = "tnum"
                ),
                color = colors.accent
            )

            if (preferences.showSeconds) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(((snapshot.second + 1) / 60f).coerceIn(0.02f, 1f))
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(colors.accentSecondary)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "%02d".format(snapshot.second),
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = colors.mutedText
                    )
                }
            }
        }
    }
}
