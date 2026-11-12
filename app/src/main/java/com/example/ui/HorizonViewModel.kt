package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HorizonPreferencesRepository
import com.example.model.AtmosphereTheme
import com.example.model.BreathPattern
import com.example.model.BreathStage
import com.example.model.BreatheState
import com.example.model.ClockStyle
import com.example.model.FocusPhase
import com.example.model.FocusTimerState
import com.example.model.HorizonPreferences
import com.example.model.HorizonTab
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ClockSnapshot(
    val hour24: Int,
    val hour12: Int,
    val minute: Int,
    val second: Int,
    val millis: Int,
    val amPm: String,
    val dayOfWeek: String,
    val dateLabel: String,
    val dayProgress: Float,
    val greeting: String
) {
    companion object {
        fun now(): ClockSnapshot {
            val cal = Calendar.getInstance()
            val h24 = cal.get(Calendar.HOUR_OF_DAY)
            val h12Raw = cal.get(Calendar.HOUR)
            val h12 = if (h12Raw == 0) 12 else h12Raw
            val min = cal.get(Calendar.MINUTE)
            val sec = cal.get(Calendar.SECOND)
            val ms = cal.get(Calendar.MILLISECOND)
            val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"

            val dayFmt = SimpleDateFormat("EEEE", Locale.getDefault())
            val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())
            val totalDaySeconds = h24 * 3600 + min * 60 + sec
            val dayProgress = totalDaySeconds / 86400f

            val greeting = when (h24) {
                in 5..11 -> "Morning Light"
                in 12..16 -> "Afternoon Flow"
                in 17..20 -> "Golden Hour"
                else -> "Night Watch"
            }

            return ClockSnapshot(
                hour24 = h24,
                hour12 = h12,
                minute = min,
                second = sec,
                millis = ms,
                amPm = amPm,
                dayOfWeek = dayFmt.format(cal.time),
                dateLabel = dateFmt.format(cal.time),
                dayProgress = dayProgress,
                greeting = greeting
            )
        }
    }
}

class HorizonViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HorizonPreferencesRepository(application.applicationContext)

    val preferences: StateFlow<HorizonPreferences> = repository.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HorizonPreferences()
    )

    private val _selectedTab = MutableStateFlow(HorizonTab.CLOCK)
    val selectedTab: StateFlow<HorizonTab> = _selectedTab.asStateFlow()

    private val _isHudVisible = MutableStateFlow(true)
    val isHudVisible: StateFlow<Boolean> = _isHudVisible.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _clockSnapshot = MutableStateFlow(ClockSnapshot.now())
    val clockSnapshot: StateFlow<ClockSnapshot> = _clockSnapshot.asStateFlow()

    private val _focusState = MutableStateFlow(FocusTimerState())
    val focusState: StateFlow<FocusTimerState> = _focusState.asStateFlow()

    private val _breatheState = MutableStateFlow(BreatheState())
    val breatheState: StateFlow<BreatheState> = _breatheState.asStateFlow()

    private var focusJob: Job? = null
    private var breatheJob: Job? = null

    init {
        startClockTicker()
        viewModelScope.launch {
            val savedPrefs = repository.preferencesFlow.first()
            val sec = savedPrefs.focusMinutes * 60
            _focusState.value = FocusTimerState(
                phase = FocusPhase.FOCUS,
                totalSeconds = sec,
                remainingSeconds = sec,
                isRunning = false
            )
        }
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (isActive) {
                _clockSnapshot.value = ClockSnapshot.now()
                delay(100L)
            }
        }
    }

    fun selectTab(tab: HorizonTab) {
        _selectedTab.value = tab
        _isHudVisible.value = true
    }

    fun toggleHudVisibility() {
        _isHudVisible.update { !it }
    }

    fun showHud() {
        _isHudVisible.value = true
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun setImmersiveFullScreen(enabled: Boolean) {
        viewModelScope.launch { repository.setImmersiveFullScreen(enabled) }
    }

    fun setAtmosphereTheme(theme: AtmosphereTheme) {
        viewModelScope.launch { repository.setAtmosphereTheme(theme) }
    }

    fun setClockStyle(style: ClockStyle) {
        viewModelScope.launch { repository.setClockStyle(style) }
    }

    fun cycleClockStyle() {
        val styles = ClockStyle.entries
        val next = styles[(styles.indexOf(preferences.value.clockStyle) + 1) % styles.size]
        setClockStyle(next)
    }

    fun set24HourFormat(enabled: Boolean) {
        viewModelScope.launch { repository.set24HourFormat(enabled) }
    }

    fun setShowSeconds(enabled: Boolean) {
        viewModelScope.launch { repository.setShowSeconds(enabled) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { repository.setKeepScreenOn(enabled) }
    }

    // --- Focus Timer Logic ---

    fun selectFocusPhase(phase: FocusPhase) {
        focusJob?.cancel()
        val minutes = if (phase == FocusPhase.FOCUS) preferences.value.focusMinutes else phase.defaultMinutes
        val seconds = minutes * 60
        _focusState.value = FocusTimerState(
            phase = phase,
            totalSeconds = seconds,
            remainingSeconds = seconds,
            isRunning = false,
            justCompleted = false
        )
    }

    fun setFocusPresetMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(1, 120)
        if (_focusState.value.phase == FocusPhase.FOCUS) {
            viewModelScope.launch { repository.setFocusMinutes(clamped) }
        }
        focusJob?.cancel()
        val seconds = clamped * 60
        _focusState.update {
            it.copy(
                totalSeconds = seconds,
                remainingSeconds = seconds,
                isRunning = false,
                justCompleted = false
            )
        }
    }

    fun adjustFocusMinutes(deltaMinutes: Int) {
        val currentMins = (_focusState.value.totalSeconds / 60).coerceAtLeast(1)
        setFocusPresetMinutes((currentMins + deltaMinutes).coerceIn(1, 120))
    }

    fun toggleFocusTimer() {
        val current = _focusState.value
        if (current.isRunning) {
            pauseFocusTimer()
        } else {
            startFocusTimer()
        }
    }

    private fun startFocusTimer() {
        focusJob?.cancel()
        val startRemaining = if (_focusState.value.remainingSeconds <= 0) {
            _focusState.value.totalSeconds
        } else {
            _focusState.value.remainingSeconds
        }
        _focusState.update {
            it.copy(
                remainingSeconds = startRemaining,
                isRunning = true,
                justCompleted = false
            )
        }
        focusJob = viewModelScope.launch {
            var lastTickMs = System.currentTimeMillis()
            var accumulatedMs = 0L
            while (isActive && _focusState.value.isRunning) {
                delay(100L)
                val now = System.currentTimeMillis()
                accumulatedMs += (now - lastTickMs)
                lastTickMs = now
                if (accumulatedMs >= 1000L) {
                    val elapsedSec = (accumulatedMs / 1000L).toInt()
                    accumulatedMs %= 1000L
                    val nextRem = (_focusState.value.remainingSeconds - elapsedSec).coerceAtLeast(0)
                    if (nextRem == 0) {
                        val wasFocus = _focusState.value.phase == FocusPhase.FOCUS
                        _focusState.update {
                            it.copy(
                                remainingSeconds = 0,
                                isRunning = false,
                                justCompleted = true
                            )
                        }
                        if (wasFocus) {
                            repository.incrementCompletedSessions()
                        }
                        break
                    } else {
                        _focusState.update { it.copy(remainingSeconds = nextRem) }
                    }
                }
            }
        }
    }

    private fun pauseFocusTimer() {
        focusJob?.cancel()
        _focusState.update { it.copy(isRunning = false) }
    }

    fun resetFocusTimer() {
        focusJob?.cancel()
        _focusState.update {
            it.copy(
                remainingSeconds = it.totalSeconds,
                isRunning = false,
                justCompleted = false
            )
        }
    }

    // --- Breathe Logic ---

    fun selectBreathPattern(pattern: BreathPattern) {
        val wasActive = _breatheState.value.isActive
        breatheJob?.cancel()
        _breatheState.value = BreatheState(
            pattern = pattern,
            isActive = false,
            stage = BreathStage.READY,
            stageProgress = 0f,
            stageRemainingSeconds = pattern.inhaleSec,
            completedCycles = _breatheState.value.completedCycles
        )
        if (wasActive) {
            startBreathing()
        }
    }

    fun toggleBreathing() {
        if (_breatheState.value.isActive) {
            stopBreathing()
        } else {
            startBreathing()
        }
    }

    private fun startBreathing() {
        breatheJob?.cancel()
        val pattern = _breatheState.value.pattern
        _breatheState.update {
            it.copy(
                isActive = true,
                stage = BreathStage.INHALE,
                stageProgress = 0f,
                stageRemainingSeconds = pattern.inhaleSec
            )
        }

        breatheJob = viewModelScope.launch {
            val stages = buildList {
                if (pattern.inhaleSec > 0) add(BreathStage.INHALE to pattern.inhaleSec)
                if (pattern.holdTopSec > 0) add(BreathStage.HOLD_TOP to pattern.holdTopSec)
                if (pattern.exhaleSec > 0) add(BreathStage.EXHALE to pattern.exhaleSec)
                if (pattern.holdBottomSec > 0) add(BreathStage.HOLD_BOTTOM to pattern.holdBottomSec)
            }
            var index = 0
            while (isActive && _breatheState.value.isActive) {
                val (stage, durationSec) = stages[index]
                val durationMs = durationSec * 1000L
                val stageStart = System.currentTimeMillis()
                while (isActive && _breatheState.value.isActive) {
                    val elapsed = (System.currentTimeMillis() - stageStart).coerceAtMost(durationMs)
                    val progress = (elapsed.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                    val remaining = kotlin.math.ceil((durationMs - elapsed) / 1000.0).toInt().coerceAtLeast(1)
                    _breatheState.update {
                        it.copy(
                            stage = stage,
                            stageProgress = progress,
                            stageRemainingSeconds = remaining
                        )
                    }
                    if (elapsed >= durationMs) break
                    delay(16L)
                }
                index = (index + 1) % stages.size
                if (index == 0) {
                    _breatheState.update { it.copy(completedCycles = it.completedCycles + 1) }
                }
            }
        }
    }

    private fun stopBreathing() {
        breatheJob?.cancel()
        _breatheState.update {
            it.copy(
                isActive = false,
                stage = BreathStage.READY,
                stageProgress = 0f,
                stageRemainingSeconds = it.pattern.inhaleSec
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        focusJob?.cancel()
        breatheJob?.cancel()
    }
}
