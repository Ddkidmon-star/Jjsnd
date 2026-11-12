package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.model.AtmosphereTheme

@Immutable
data class HorizonAtmosphereColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val accent: Color,
    val accentSecondary: Color,
    val onBackground: Color,
    val mutedText: Color,
    val isDark: Boolean
)

val LocalHorizonColors = staticCompositionLocalOf {
    HorizonAtmosphereColors(
        background = ObsidianBg,
        surface = ObsidianSurface,
        surfaceElevated = ObsidianSurfaceVariant,
        accent = ObsidianAmber,
        accentSecondary = ObsidianCoral,
        onBackground = ObsidianText,
        mutedText = ObsidianTextMuted,
        isDark = true
    )
}

fun atmosphereColorsFor(theme: AtmosphereTheme): HorizonAtmosphereColors = when (theme) {
    AtmosphereTheme.OBSIDIAN_AMBER -> HorizonAtmosphereColors(
        background = ObsidianBg,
        surface = ObsidianSurface,
        surfaceElevated = ObsidianSurfaceVariant,
        accent = ObsidianAmber,
        accentSecondary = ObsidianCoral,
        onBackground = ObsidianText,
        mutedText = ObsidianTextMuted,
        isDark = true
    )
    AtmosphereTheme.AURORA_TEAL -> HorizonAtmosphereColors(
        background = AuroraBg,
        surface = AuroraSurface,
        surfaceElevated = AuroraSurfaceVariant,
        accent = AuroraMint,
        accentSecondary = AuroraCyan,
        onBackground = AuroraText,
        mutedText = AuroraTextMuted,
        isDark = true
    )
    AtmosphereTheme.TWILIGHT_ROSE -> HorizonAtmosphereColors(
        background = TwilightBg,
        surface = TwilightSurface,
        surfaceElevated = TwilightSurfaceVariant,
        accent = TwilightRose,
        accentSecondary = TwilightViolet,
        onBackground = TwilightText,
        mutedText = TwilightTextMuted,
        isDark = true
    )
    AtmosphereTheme.DAYLIGHT_PAPER -> HorizonAtmosphereColors(
        background = DaylightBg,
        surface = DaylightSurface,
        surfaceElevated = DaylightSurfaceVariant,
        accent = DaylightTerracotta,
        accentSecondary = DaylightOchre,
        onBackground = DaylightText,
        mutedText = DaylightTextMuted,
        isDark = false
    )
}

private fun toMaterialColorScheme(colors: HorizonAtmosphereColors): ColorScheme =
    if (colors.isDark) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = colors.background,
            secondary = colors.accentSecondary,
            onSecondary = colors.background,
            background = colors.background,
            onBackground = colors.onBackground,
            surface = colors.surface,
            onSurface = colors.onBackground,
            surfaceVariant = colors.surfaceElevated,
            onSurfaceVariant = colors.mutedText,
            outline = colors.mutedText.copy(alpha = 0.35f)
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = Color.White,
            secondary = colors.accentSecondary,
            onSecondary = Color.White,
            background = colors.background,
            onBackground = colors.onBackground,
            surface = colors.surface,
            onSurface = colors.onBackground,
            surfaceVariant = colors.surfaceElevated,
            onSurfaceVariant = colors.mutedText,
            outline = colors.mutedText.copy(alpha = 0.35f)
        )
    }

@Composable
fun HorizonTheme(
    atmosphereTheme: AtmosphereTheme = AtmosphereTheme.OBSIDIAN_AMBER,
    content: @Composable () -> Unit
) {
    val atmosphereColors = atmosphereColorsFor(atmosphereTheme)
    val colorScheme = toMaterialColorScheme(atmosphereColors)

    CompositionLocalProvider(LocalHorizonColors provides atmosphereColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HorizonTypography,
            content = content
        )
    }
}
