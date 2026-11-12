package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AtmosphereTheme
import com.example.model.HorizonPreferences
import com.example.ui.theme.LocalHorizonColors
import com.example.ui.theme.atmosphereColorsFor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizonSettingsSheet(
    preferences: HorizonPreferences,
    onDismiss: () -> Unit,
    onSelectTheme: (AtmosphereTheme) -> Unit,
    onToggleImmersive: (Boolean) -> Unit,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onToggle24Hour: (Boolean) -> Unit,
    onToggleSeconds: (Boolean) -> Unit
) {
    val colors = LocalHorizonColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.onBackground,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap any open area of the canvas to hide all controls for pure full-screen focus.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.mutedText
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.settings_theme_label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AtmosphereTheme.entries.forEach { theme ->
                    val preview = atmosphereColorsFor(theme)
                    val selected = preferences.atmosphereTheme == theme
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) colors.surfaceElevated else colors.background.copy(alpha = 0.45f)
                            )
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) colors.accent else colors.mutedText.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectTheme(theme) }
                            .padding(vertical = 12.dp, horizontal = 6.dp)
                            .testTag("theme_option_${theme.id}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(preview.background, preview.accent)
                                    )
                                )
                                .border(1.dp, preview.onBackground.copy(alpha = 0.2f), CircleShape)
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = theme.displayName,
                                    tint = preview.onBackground,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = theme.displayName.split(" ").first(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = colors.onBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = colors.mutedText.copy(alpha = 0.16f))
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.settings_display_label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent
            )
            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                icon = Icons.Rounded.Fullscreen,
                title = stringResource(R.string.settings_immersive_title),
                subtitle = stringResource(R.string.settings_immersive_desc),
                checked = preferences.isImmersiveFullScreen,
                onCheckedChange = onToggleImmersive,
                testTag = "toggle_immersive_fullscreen"
            )

            SettingToggleRow(
                icon = Icons.Rounded.BrightnessHigh,
                title = stringResource(R.string.settings_keep_screen_on_title),
                subtitle = stringResource(R.string.settings_keep_screen_on_desc),
                checked = preferences.keepScreenOn,
                onCheckedChange = onToggleKeepScreenOn,
                testTag = "toggle_keep_screen_on"
            )

            SettingToggleRow(
                icon = Icons.Rounded.Schedule,
                title = stringResource(R.string.settings_24h_title),
                subtitle = stringResource(R.string.settings_24h_desc),
                checked = preferences.is24HourFormat,
                onCheckedChange = onToggle24Hour,
                testTag = "toggle_24h_format"
            )

            SettingToggleRow(
                icon = Icons.Rounded.Timer,
                title = stringResource(R.string.settings_seconds_title),
                subtitle = stringResource(R.string.settings_seconds_desc),
                checked = preferences.showSeconds,
                onCheckedChange = onToggleSeconds,
                testTag = "toggle_show_seconds"
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val colors = LocalHorizonColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceElevated)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.mutedText
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.background,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = colors.mutedText,
                uncheckedTrackColor = colors.surfaceElevated
            )
        )
    }
}
