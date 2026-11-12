package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.LocalHorizonColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AmbientBackdrop(
    modifier: Modifier = Modifier,
    pulseIntensity: Float = 0.22f
) {
    val colors = LocalHorizonColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_drift")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        if (colors.isDark) {
            Image(
                painter = painterResource(id = R.drawable.img_horizon_aura),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.24f,
                modifier = Modifier.fillMaxSize()
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val minDim = size.minDimension

            val primaryCenter = Offset(
                x = w * (0.5f + 0.16f * cos(phase)),
                y = h * (0.34f + 0.10f * sin(phase))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accent.copy(alpha = pulseIntensity),
                        colors.accent.copy(alpha = pulseIntensity * 0.28f),
                        colors.background.copy(alpha = 0f)
                    ),
                    center = primaryCenter,
                    radius = minDim * 0.85f
                ),
                center = primaryCenter,
                radius = minDim * 0.85f
            )

            val secondaryCenter = Offset(
                x = w * (0.48f - 0.18f * sin(phase * 0.7f)),
                y = h * (0.72f + 0.08f * cos(phase * 0.7f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accentSecondary.copy(alpha = pulseIntensity * 0.72f),
                        colors.background.copy(alpha = 0f)
                    ),
                    center = secondaryCenter,
                    radius = minDim * 0.7f
                ),
                center = secondaryCenter,
                radius = minDim * 0.7f
            )
        }
    }
}
