package com.qook.ftele.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qook.ftele.model.LogEntry
import com.qook.ftele.model.Rule
import com.qook.ftele.ui.components.FTeleBottomNavBar
import com.qook.ftele.ui.components.ScreenTab
import com.qook.ftele.ui.screens.ActivityScreen
import com.qook.ftele.ui.screens.RulesScreen
import com.qook.ftele.ui.screens.SettingsScreen
import com.qook.ftele.ui.screens.ShieldsScreen
import com.qook.ftele.ui.theme.FTeleTheme

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
    onChangeMode: (String) -> Unit,
    onChangeTheme: (String) -> Unit
) {
    val colors = FTeleTheme.colors
    val tab = ScreenTab.ALL[currentTab.coerceIn(0, ScreenTab.ALL.size - 1)]
    val needsAttention = !hasPermission

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.card)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentTab == 0) "F-Tele Guard" else tab.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentTab == 0) "Offline SMS spam protection" else tab.subtitle,
                            fontSize = 12.sp,
                            color = colors.sub
                        )
                    }

                    // Master Protection Switch on Tab 0 (Activity)
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
            FTeleBottomNavBar(
                currentTab = currentTab,
                needsAttention = needsAttention,
                onTabSelected = onTabSelected
            )
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
                    onChangeMode = onChangeMode,
                    onChangeTheme = onChangeTheme,
                    onClearLogs = onClearLogs
                )
            }
        }
    }
}
