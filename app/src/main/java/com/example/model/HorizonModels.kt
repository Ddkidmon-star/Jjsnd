package com.example.model

enum class HorizonTab {
    CLOCK,
    FOCUS,
    BREATHE
}

enum class ClockStyle(val title: String) {
    SOLAR_RING("Solar Ring"),
    SPLIT_FLIP("Split Flip"),
    EDITORIAL("Editorial")
}

enum class FocusPhase(val label: String, val defaultMinutes: Int) {
    FOCUS("Deep Focus", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

enum class BreathPattern(
    val title: String,
    val ratioLabel: String,
    val inhaleSec: Int,
    val holdTopSec: Int,
    val exhaleSec: Int,
    val holdBottomSec: Int
) {
    BOX("Box Focus", "4 · 4 · 4 · 4", 4, 4, 4, 4),
    CALM("Calm 4-7-8", "4 · 7 · 8 · 0", 4, 7, 8, 0),
    COHERENCE("Coherence", "5 · 0 · 5 · 0", 5, 0, 5, 0)
}

enum class AtmosphereTheme(val id: String, val displayName: String, val isDark: Boolean) {
    OBSIDIAN_AMBER("obsidian_amber", "Obsidian Amber", true),
    AURORA_TEAL("aurora_teal", "Aurora Teal", true),
    TWILIGHT_ROSE("twilight_rose", "Twilight Rose", true),
    DAYLIGHT_PAPER("daylight_paper", "Daylight Paper", false);

    companion object {
        fun fromId(id: String?): AtmosphereTheme =
            entries.firstOrNull { it.id == id } ?: OBSIDIAN_AMBER
    }
}

data class HorizonPreferences(
    val isImmersiveFullScreen: Boolean = true,
    val atmosphereTheme: AtmosphereTheme = AtmosphereTheme.OBSIDIAN_AMBER,
    val clockStyle: ClockStyle = ClockStyle.SOLAR_RING,
    val is24HourFormat: Boolean = false,
    val showSeconds: Boolean = true,
    val keepScreenOn: Boolean = true,
    val focusMinutes: Int = 25,
    val completedFocusSessions: Int = 0
)

data class FocusTimerState(
    val phase: FocusPhase = FocusPhase.FOCUS,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val justCompleted: Boolean = false
) {
    val progress: Float
        get() = if (totalSeconds <= 0) 0f else (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()
}

enum class BreathStage(val prompt: String) {
    READY("Ready"),
    INHALE("Inhale"),
    HOLD_TOP("Hold"),
    EXHALE("Exhale"),
    HOLD_BOTTOM("Rest")
}

data class BreatheState(
    val pattern: BreathPattern = BreathPattern.BOX,
    val isActive: Boolean = false,
    val stage: BreathStage = BreathStage.READY,
    val stageProgress: Float = 0f,
    val stageRemainingSeconds: Int = 0,
    val completedCycles: Int = 0
)
