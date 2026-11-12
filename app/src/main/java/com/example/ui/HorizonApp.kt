package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.HorizonTab
import com.example.ui.components.AmbientBackdrop
import com.example.ui.components.HorizonSettingsSheet
import com.example.ui.screens.BreatheScreen
import com.example.ui.screens.ClockScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.theme.LocalHorizonColors
import kotlinx.coroutines.delay

@Composable
fun HorizonApp(
    viewModel: HorizonViewModel
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isHudVisible by viewModel.isHudVisible.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val clockSnapshot by viewModel.clockSnapshot.collectAsStateWithLifecycle()
    val focusState by viewModel.focusState.collectAsStateWithLifecycle()
    val breatheState by viewModel.breatheState.collectAsStateWithLifecycle()

    val colors = LocalHorizonColors.current

    var showZenHint by remember { mutableStateOf(false) }
    LaunchedEffect(isHudVisible) {
        if (!isHudVisible) {
            showZenHint = true
            delay(2200L)
            showZenHint = false
        } else {
            showZenHint = false
        }
    }

    // Ensure hardware Back restores HUD or returns from secondary tabs cleanly
    BackHandler(enabled = !isHudVisible || selectedTab != HorizonTab.CLOCK) {
        if (!isHudVisible) {
            viewModel.showHud()
        } else {
            viewModel.selectTab(HorizonTab.CLOCK)
        }
    }

    val backdropPulse = when {
        selectedTab == HorizonTab.BREATHE && breatheState.isActive -> 0.34f
        selectedTab == HorizonTab.FOCUS && focusState.isRunning -> 0.28f
        else -> 0.20f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { viewModel.toggleHudVisibility() }
            )
            .testTag("full_screen_canvas")
    ) {
        AmbientBackdrop(pulseIntensity = backdropPulse)

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(tween(240)) togetherWith fadeOut(tween(180))
            },
            modifier = Modifier.fillMaxSize(),
            label = "main_tab_content"
        ) { tab ->
            when (tab) {
                HorizonTab.CLOCK -> ClockScreen(
                    snapshot = clockSnapshot,
                    preferences = preferences,
                    isHudVisible = isHudVisible,
                    onSelectClockStyle = viewModel::setClockStyle
                )
                HorizonTab.FOCUS -> FocusScreen(
                    focusState = focusState,
                    completedSessions = preferences.completedFocusSessions,
                    isHudVisible = isHudVisible,
                    onSelectPhase = viewModel::selectFocusPhase,
                    onSelectPresetMinutes = viewModel::setFocusPresetMinutes,
                    onAdjustMinutes = viewModel::adjustFocusMinutes,
                    onToggleTimer = viewModel::toggleFocusTimer,
                    onResetTimer = viewModel::resetFocusTimer
                )
                HorizonTab.BREATHE -> BreatheScreen(
                    breatheState = breatheState,
                    isHudVisible = isHudVisible,
                    onSelectPattern = viewModel::selectBreathPattern,
                    onToggleBreathing = viewModel::toggleBreathing
                )
            }
        }

        // Top Floating Studio Bar (respects camera notch / safeDrawing insets)
        AnimatedVisibility(
            visible = isHudVisible,
            enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { -it / 2 },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { -it / 2 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Brand badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.surface.copy(alpha = 0.78f))
                        .border(1.dp, colors.mutedText.copy(alpha = 0.16f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.app_name).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onBackground
                    )
                }

                // Right action pills: Hide Controls (Zen), Full Screen System Bar Toggle, Settings
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleHudVisibility() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = colors.surface.copy(alpha = 0.78f),
                            contentColor = colors.onBackground
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .border(1.dp, colors.mutedText.copy(alpha = 0.16f), CircleShape)
                            .testTag("zen_hide_hud_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VisibilityOff,
                            contentDescription = stringResource(R.string.action_zen_hide)
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.setImmersiveFullScreen(!preferences.isImmersiveFullScreen)
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (preferences.isImmersiveFullScreen) {
                                colors.accent.copy(alpha = 0.22f)
                            } else {
                                colors.surface.copy(alpha = 0.78f)
                            },
                            contentColor = if (preferences.isImmersiveFullScreen) {
                                colors.accent
                            } else {
                                colors.onBackground
                            }
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .border(
                                1.dp,
                                if (preferences.isImmersiveFullScreen) {
                                    colors.accent.copy(alpha = 0.45f)
                                } else {
                                    colors.mutedText.copy(alpha = 0.16f)
                                },
                                CircleShape
                            )
                            .testTag("immersive_fullscreen_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (preferences.isImmersiveFullScreen) {
                                Icons.Rounded.FullscreenExit
                            } else {
                                Icons.Rounded.Fullscreen
                            },
                            contentDescription = if (preferences.isImmersiveFullScreen) {
                                stringResource(R.string.action_fullscreen_exit)
                            } else {
                                stringResource(R.string.action_fullscreen_enter)
                            }
                        )
                    }

                    IconButton(
                        onClick = { viewModel.openSettings() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = colors.surface.copy(alpha = 0.78f),
                            contentColor = colors.onBackground
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .border(1.dp, colors.mutedText.copy(alpha = 0.16f), CircleShape)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = stringResource(R.string.action_settings)
                        )
                    }
                }
            }
        }

        // Subtle Zen Mode hint pill that auto-fades out
        AnimatedVisibility(
            visible = showZenHint,
            enter = fadeIn(tween(240)),
            exit = fadeOut(tween(500)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.surface.copy(alpha = 0.85f))
                    .border(1.dp, colors.mutedText.copy(alpha = 0.2f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.zen_hint_badge),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.mutedText
                )
            }
        }

        // Bottom Floating Mode Dock (explicitly handles navigationBars insets)
        AnimatedVisibility(
            visible = isHudVisible,
            enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 2 },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(colors.surface.copy(alpha = 0.88f))
                    .border(1.dp, colors.mutedText.copy(alpha = 0.18f), RoundedCornerShape(32.dp))
                    .padding(6.dp)
            ) {
                DockTabItem(
                    label = stringResource(R.string.tab_clock),
                    selected = selectedTab == HorizonTab.CLOCK,
                    selectedIcon = Icons.Rounded.Schedule,
                    unselectedIcon = Icons.Outlined.Schedule,
                    onClick = { viewModel.selectTab(HorizonTab.CLOCK) },
                    testTag = "nav_tab_clock"
                )
                DockTabItem(
                    label = stringResource(R.string.tab_focus),
                    selected = selectedTab == HorizonTab.FOCUS,
                    selectedIcon = Icons.Rounded.Timer,
                    unselectedIcon = Icons.Outlined.Timer,
                    onClick = { viewModel.selectTab(HorizonTab.FOCUS) },
                    testTag = "nav_tab_focus"
                )
                DockTabItem(
                    label = stringResource(R.string.tab_breathe),
                    selected = selectedTab == HorizonTab.BREATHE,
                    selectedIcon = Icons.Rounded.Air,
                    unselectedIcon = Icons.Outlined.Air,
                    onClick = { viewModel.selectTab(HorizonTab.BREATHE) },
                    testTag = "nav_tab_breathe"
                )
            }
        }

        if (isSettingsOpen) {
            HorizonSettingsSheet(
                preferences = preferences,
                onDismiss = viewModel::closeSettings,
                onSelectTheme = viewModel::setAtmosphereTheme,
                onToggleImmersive = viewModel::setImmersiveFullScreen,
                onToggleKeepScreenOn = viewModel::setKeepScreenOn,
                onToggle24Hour = viewModel::set24HourFormat,
                onToggleSeconds = viewModel::setShowSeconds
            )
        }
    }
}

@Composable
private fun DockTabItem(
    label: String,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = LocalHorizonColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(26.dp))
            .background(
                if (selected) colors.accent else colors.surface.copy(alpha = 0f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = if (selected) colors.background else colors.mutedText,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) colors.background else colors.mutedText
        )
    }
}
