package com.example.ftele.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ftele.model.LogEntry
import com.example.ftele.model.Rule
import com.example.ftele.ui.screens.ActivityScreen
import com.example.ftele.ui.screens.RulesScreen
import com.example.ftele.ui.screens.ShieldsScreen
import com.example.ftele.ui.screens.SettingsScreen
import com.example.ftele.ui.theme.FTeleTheme

sealed class ScreenTab(val index: Int, val title: String, val subtitle: String, val icon: ImageVector) {
    object Activity : ScreenTab(0, "F-Tele Guard", "Offline SMS spam protection", Icons.Outlined.Inbox)
    object Shields : ScreenTab(1, "Protection Shields", "Preset telecom & spam packs", Icons.Outlined.Security)
    object Rules : ScreenTab(2, "Custom Rules", "Keyword, sender & regex filters", Icons.Outlined.Tune)
    object Settings : ScreenTab(3, "Settings", "Filtering mode & preferences", Icons.Outlined.Settings)

    companion object {
        val ALL = listOf(Activity, Shields, Rules, Settings)
        fun fromIndex(index: Int) = ALL.find { it.index == index } ?: Activity
    }
}

@Composable
fun MainScreen(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    isEnabled: Boolean,
    onToggleMaster: (Boolean) -> Unit,
    hasPermission: Boolean,
    activeShieldCount: Int,
    currentMode: String,
    themeMode: String,
    logs: List<LogEntry>,
    customRules: List<Rule>,
    effectiveRules: List<Rule>,
    isCategoryEnabled: (String) -> Boolean,
    onToggleCategory: (String, Boolean) -> Unit,
    onOpenPermissionSettings: () -> Unit,
    onSimulateSms: () -> Unit,
    onClearLogs: () -> Unit,
    onAllowSender: (String) -> Unit,
    onBlockSender: (String) -> Unit,
    onSaveRule: (Rule) -> Unit,
    onToggleRule: (Rule) -> Unit,
    onRemoveRule: (Rule) -> Unit,
    onSelectMode: (String) -> Unit,
    onSelectTheme: (String) -> Unit
) {
    val colors = FTeleTheme.colors
    val tab = ScreenTab.fromIndex(currentTab)
    val needsAttention = !hasPermission || !isEnabled

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bg)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tab.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.ink,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.subtitle,
                            fontSize = 13.sp,
                            color = colors.sub
                        )
                    }

                    // Master Protection Switch on Tab 0
                    if (currentTab == 0) {
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = onToggleMaster,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.sub,
                                uncheckedTrackColor = colors.cardSoft
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = colors.card,
                tonalElevation = 0.dp
            ) {
                ScreenTab.ALL.forEach { item ->
                    val isSelected = currentTab == item.index

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onTabSelected(item.index) },
                        icon = {
                            Box {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(22.dp)
                                )
                                // Warning indicator dot on Settings if attention needed
                                if (item == ScreenTab.Settings && needsAttention) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(colors.warning)
                                    )
                                }
                            }
                        },
                        label = {
                            Text(
                                text = when (item) {
                                    ScreenTab.Activity -> "Activity"
                                    ScreenTab.Shields -> "Shields"
                                    ScreenTab.Rules -> "Rules"
                                    ScreenTab.Settings -> "Settings"
                                },
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.accent,
                            selectedTextColor = colors.accent,
                            unselectedIconColor = colors.sub,
                            unselectedTextColor = colors.sub,
                            indicatorColor = colors.accentSoft
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.bg)
        ) {
            when (currentTab) {
                0 -> ActivityScreen(
                    isEnabled = isEnabled,
                    hasPermission = hasPermission,
                    activeShieldCount = activeShieldCount,
                    isStrict = currentMode == "allowlist",
                    logs = logs,
                    currentRules = effectiveRules,
                    onToggleProtection = { onToggleMaster(!isEnabled) },
                    onOpenPermissionSettings = onOpenPermissionSettings,
                    onSimulateSms = onSimulateSms,
                    onClearLogs = onClearLogs,
                    onAllowSender = onAllowSender,
                    onBlockSender = onBlockSender
                )
                1 -> ShieldsScreen(
                    isCategoryEnabled = isCategoryEnabled,
                    onToggleCategory = onToggleCategory,
                    onSimulateSms = onSimulateSms
                )
                2 -> RulesScreen(
                    customRules = customRules,
                    effectiveRules = effectiveRules,
                    currentMode = currentMode,
                    onSaveRule = onSaveRule,
                    onToggleRule = onToggleRule,
                    onRemoveRule = onRemoveRule
                )
                3 -> SettingsScreen(
                    hasPermission = hasPermission,
                    currentMode = currentMode,
                    themeMode = themeMode,
                    onOpenPermissionSettings = onOpenPermissionSettings,
                    onSelectMode = onSelectMode,
                    onSelectTheme = onSelectTheme,
                    onClearAllLogs = onClearLogs
                )
            }
        }
    }
}
