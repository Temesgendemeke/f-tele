package com.example.ftele.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.example.ftele.data.RuleStore
import com.example.ftele.engine.PatternPacks
import com.example.ftele.engine.Patterns
import com.example.ftele.engine.Simulations
import com.example.ftele.model.LogEntry
import com.example.ftele.model.MatchMode
import com.example.ftele.model.Rule
import com.example.ftele.model.RuleAction
import com.example.ftele.model.RuleTarget
import com.example.ftele.ui.theme.FTeleTheme
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
                    onSelectMode = { mode ->
                        RuleStore.setMode(this, mode)
                        currentMode = mode
                    },
                    onSelectTheme = { theme ->
                        RuleStore.setThemeMode(this, theme)
                        themeMode = theme
                    }
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("current_tab", currentTab)
    }

    override fun onResume() {
        super.onResume()
        syncAllData()
        refreshHandler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    private fun syncAllData() {
        isEnabled = RuleStore.isEnabled(this)
        currentMode = RuleStore.getMode(this)
        themeMode = RuleStore.getThemeMode(this)
        hasPermission = checkPermission()
        syncRulesAndCategories()
        refreshLogs()
    }

    private fun syncRulesAndCategories() {
        customRules = RuleStore.getCustomRules(this)
        effectiveRules = RuleStore.getAllEffectiveRules(this)

        var count = 0
        for (c in PatternPacks.CATEGORIES) {
            if (RuleStore.isCategoryEnabled(this, c.id)) count++
        }
        activeShieldCount = count
    }

    private fun refreshLogs() {
        logs = RuleStore.getLog(this)
    }

    private fun setMasterEnabled(enabled: Boolean) {
        RuleStore.setEnabled(this, enabled)
        isEnabled = enabled
        window.decorView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        val msg = if (enabled) "Protection active" else "Protection paused"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun checkPermission(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: ""
        return flat.contains(packageName)
    }

    private fun toggleSenderRule(sender: String, action: RuleAction) {
        val looseSender = Patterns.loose(sender)
        val updated = customRules.toMutableList()
        val existing = updated.find {
            it.target == RuleTarget.SENDER && Patterns.loose(it.value) == looseSender
        }

        if (existing != null && existing.action == action) {
            updated.remove(existing)
        } else {
            if (existing != null) updated.remove(existing)
            val newRule = Rule(
                id = PatternPacks.newRuleId(),
                action = action,
                target = RuleTarget.SENDER,
                mode = MatchMode.EXACT,
                value = sender,
                category = "Sender Rule",
                enabled = true
            )
            updated.add(0, newRule)
        }

        RuleStore.saveCustomRules(this, updated)
        syncRulesAndCategories()
    }

    private fun runSimulation() {
        val sample = Simulations.SAMPLES[Random.nextInt(Simulations.SAMPLES.size)]
        val decision = RuleStore.decide(this, sample.sender, sample.text)
        RuleStore.addLog(this, sample.sender, sample.text, decision)
        refreshLogs()
        Toast.makeText(this, "Simulated SMS! Check Activity log.", Toast.LENGTH_SHORT).show()
    }
}
