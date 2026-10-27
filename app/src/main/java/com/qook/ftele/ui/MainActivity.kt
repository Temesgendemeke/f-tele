package com.qook.ftele.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.qook.ftele.data.RuleStore
import com.qook.ftele.engine.PatternPacks
import com.qook.ftele.engine.Patterns
import com.qook.ftele.engine.Simulations
import com.qook.ftele.model.LogEntry
import com.qook.ftele.model.MatchMode
import com.qook.ftele.model.Rule
import com.qook.ftele.model.RuleAction
import com.qook.ftele.model.RuleTarget
import com.qook.ftele.ui.theme.FTeleTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private var currentTab by mutableIntStateOf(0)
    private var isEnabled by mutableStateOf(true)
    private var hasPermission by mutableStateOf(false)
    private var currentMode by mutableStateOf("blocklist")
    private var themeMode by mutableStateOf("system")
    private var logs by mutableStateOf<List<LogEntry>>(emptyList())
    private var customRules by mutableStateOf<List<Rule>>(emptyList())
    private var effectiveRules by mutableStateOf<List<Rule>>(emptyList())
    private var activeShieldCount by mutableIntStateOf(0)

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            refreshLogs()
            refreshHandler.postDelayed(this, 3000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        currentTab = savedInstanceState?.getInt("current_tab", 0) ?: 0
        syncAllData()

        setContent {
            FTeleTheme(themeSetting = themeMode) {
                MainScreen(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    isEnabled = isEnabled,
                    onToggleMaster = { setMasterEnabled(it) },
                    hasPermission = hasPermission,
                    activeShieldCount = activeShieldCount,
                    currentMode = currentMode,
                    themeMode = themeMode,
                    logs = logs,
                    customRules = customRules,
                    effectiveRules = effectiveRules,
                    isCategoryEnabled = { catId -> RuleStore.isCategoryEnabled(this, catId) },
                    onToggleCategory = { catId, enabled ->
                        RuleStore.setCategoryEnabled(this, catId, enabled)
                        syncRulesAndCategories()
                    },
                    onOpenPermissionSettings = {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    onSimulateSms = { runSimulation() },
                    onClearLogs = {
                        RuleStore.clearLog(this)
                        refreshLogs()
                        Toast.makeText(this, "Activity log cleared.", Toast.LENGTH_SHORT).show()
                    },
                    onAllowSender = { toggleSenderRule(it, RuleAction.ALLOW) },
                    onBlockSender = { toggleSenderRule(it, RuleAction.BLOCK) },
                    onSaveRule = { rule ->
                        val updated = customRules.toMutableList()
                        val existingIdx = updated.indexOfFirst { it.key == rule.key }
                        if (existingIdx != -1) {
                            updated[existingIdx] = rule
                        } else {
                            updated.add(0, rule)
                        }
                        RuleStore.saveCustomRules(this, updated)
                        syncRulesAndCategories()
                        Toast.makeText(this, "Rule saved.", Toast.LENGTH_SHORT).show()
                    },
                    onToggleRule = { rule ->
                        val updated = customRules.toMutableList()
                        val idx = updated.indexOfFirst { it.id == rule.id }
                        if (idx != -1) {
                            updated[idx] = rule.copy(enabled = !rule.enabled)
                            RuleStore.saveCustomRules(this, updated)
                            syncRulesAndCategories()
                        }
                    },
                    onRemoveRule = { rule ->
                        val updated = customRules.filter { it.id != rule.id }
                        RuleStore.saveCustomRules(this, updated)
                        syncRulesAndCategories()
                        Toast.makeText(this, "Rule removed.", Toast.LENGTH_SHORT).show()
                    },
                    onChangeMode = { mode ->
                        RuleStore.setMode(this, mode)
                        currentMode = mode
                    },
                    onChangeTheme = { theme ->
                        RuleStore.setThemeMode(this, theme)
                        themeMode = theme
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkNotificationPermission()
        syncAllData()
        refreshHandler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("current_tab", currentTab)
    }

    private fun syncAllData() {
        isEnabled = RuleStore.isEnabled(this)
        currentMode = RuleStore.getMode(this)
        themeMode = RuleStore.getThemeMode(this)
        syncRulesAndCategories()
        refreshLogs()
        checkNotificationPermission()
    }

    private fun syncRulesAndCategories() {
        customRules = RuleStore.getCustomRules(this)
        effectiveRules = RuleStore.getAllEffectiveRules(this)
        activeShieldCount = PatternPacks.CATEGORIES.count { RuleStore.isCategoryEnabled(this, it.id) }
    }

    private fun refreshLogs() {
        logs = RuleStore.getLogs(this)
    }

    private fun setMasterEnabled(enabled: Boolean) {
        RuleStore.setEnabled(this, enabled)
        isEnabled = enabled
        Toast.makeText(
            this,
            if (enabled) "Protection active" else "Protection paused",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun checkNotificationPermission() {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: ""
        hasPermission = flat.contains(packageName)
    }

    private fun toggleSenderRule(sender: String, action: RuleAction) {
        val cleanSender = sender.trim()
        val rule = Rule(
            id = PatternPacks.newRuleId(),
            action = action,
            target = RuleTarget.SENDER,
            mode = MatchMode.EXACT,
            value = cleanSender,
            category = "Quick Action: $cleanSender",
            enabled = true
        )
        val updated = customRules.toMutableList()
        val existingIdx = updated.indexOfFirst {
            it.target == RuleTarget.SENDER && Patterns.loose(it.value) == Patterns.loose(cleanSender)
        }
        if (existingIdx != -1) {
            updated[existingIdx] = rule
        } else {
            updated.add(0, rule)
        }
        RuleStore.saveCustomRules(this, updated)
        syncRulesAndCategories()
        Toast.makeText(this, "Rule added for sender: $cleanSender", Toast.LENGTH_SHORT).show()
    }

    private fun runSimulation() {
        val sample = Simulations.SAMPLES.random(Random(System.currentTimeMillis()))
        val rules = RuleStore.getAllEffectiveRules(this)
        val mode = RuleStore.getMode(this)
        val decision = Patterns.decide(rules, mode, sample.sender, sample.text)

        RuleStore.appendLog(
            this,
            LogEntry(
                id = "${System.currentTimeMillis()}",
                sender = sample.sender,
                text = sample.text,
                time = System.currentTimeMillis(),
                muted = decision.mute,
                reason = decision.reason
            )
        )
        refreshLogs()
        Toast.makeText(
            this,
            if (decision.mute) "Simulated: SMS Muted" else "Simulated: SMS Allowed",
            Toast.LENGTH_SHORT
        ).show()
    }
}
