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
import com.qook.ftele.engine.BackgroundHelper
import com.qook.ftele.engine.PatternPacks
import com.qook.ftele.engine.Patterns
import com.qook.ftele.model.LogEntry
import com.qook.ftele.model.MatchMode
import com.qook.ftele.model.Rule
import com.qook.ftele.model.RuleAction
import com.qook.ftele.model.RuleTarget
import com.qook.ftele.ui.theme.FTeleTheme

class MainActivity : ComponentActivity() {

    private var currentTab by mutableIntStateOf(0)
    private var isEnabled by mutableStateOf(true)
    private var hasPermission by mutableStateOf(false)
    private var hasContactPermission by mutableStateOf(false)
    private var hasSmsPermission by mutableStateOf(false)
    private var isIgnoringBattery by mutableStateOf(false)
    private var deviceTip by mutableStateOf("")
    private var currentMode by mutableStateOf("blocklist")
    private var themeMode by mutableStateOf("system")
    private var logs by mutableStateOf<List<LogEntry>>(emptyList())
    private var customRules by mutableStateOf<List<Rule>>(emptyList())
    private var effectiveRules by mutableStateOf<List<Rule>>(emptyList())
    private var categoryStates by mutableStateOf<Map<String, Boolean>>(emptyMap())
    private var activeShieldCount by mutableIntStateOf(0)

    private val contactPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasContactPermission = granted
        if (granted) {
            Toast.makeText(this, "Contacts protection active.", Toast.LENGTH_SHORT).show()
            syncAllData()
        }
    }

    private val smsPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasSmsPermission = granted
        if (granted) {
            Toast.makeText(this, "SMS inbox history loaded.", Toast.LENGTH_SHORT).show()
            syncAllData()
        }
    }

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

        if (!com.qook.ftele.engine.SmsInboxHelper.hasSmsPermission(this)) {
            smsPermissionLauncher.launch(android.Manifest.permission.READ_SMS)
        }

        setContent {
            FTeleTheme(themeSetting = themeMode) {
                MainScreen(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    isEnabled = isEnabled,
                    onToggleMaster = { setMasterEnabled(it) },
                    hasPermission = hasPermission,
                    hasContactPermission = hasContactPermission,
                    onRequestContactPermission = {
                        contactPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                    },
                    hasSmsPermission = hasSmsPermission,
                    onRequestSmsPermission = {
                        smsPermissionLauncher.launch(android.Manifest.permission.READ_SMS)
                    },
                    isIgnoringBattery = isIgnoringBattery,
                    deviceTip = deviceTip,
                    activeShieldCount = activeShieldCount,
                    currentMode = currentMode,
                    themeMode = themeMode,
                    logs = logs,
                    customRules = customRules,
                    effectiveRules = effectiveRules,
                    categoryStates = categoryStates,
                    onToggleCategory = { catId, enabled ->
                        RuleStore.setCategoryEnabled(this, catId, enabled)
                        categoryStates = categoryStates.toMutableMap().apply { put(catId, enabled) }
                        syncRulesAndCategories()
                    },
                    onOpenPermissionSettings = {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    onOpenBatterySettings = {
                        BackgroundHelper.openBatteryOptimizationSettings(this)
                    },
                    onOpenAutostartSettings = {
                        BackgroundHelper.openAutostartSettings(this)
                    },
                    onClearLogs = {
                        RuleStore.clearLog(this)
                        refreshLogs()
                        Toast.makeText(this, "Activity log cleared.", Toast.LENGTH_SHORT).show()
                    },
                    onAllowPattern = { addPatternRule(it, RuleAction.ALLOW) },
                    onBlockPattern = { addPatternRule(it, RuleAction.BLOCK) },
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
        isIgnoringBattery = BackgroundHelper.isIgnoringBatteryOptimizations(this)
        deviceTip = BackgroundHelper.getDeviceTip()
        hasContactPermission = com.qook.ftele.engine.ContactHelper.hasContactPermission(this)
        hasSmsPermission = com.qook.ftele.engine.SmsInboxHelper.hasSmsPermission(this)
        syncRulesAndCategories()
        refreshLogs()
        if (hasSmsPermission) {
            loadInboxSmsAsync()
        }
        checkNotificationPermission()
    }

    private fun syncRulesAndCategories() {
        categoryStates = PatternPacks.CATEGORIES.associate { it.id to RuleStore.isCategoryEnabled(this, it.id) }
        customRules = RuleStore.getCustomRules(this)
        effectiveRules = RuleStore.getAllEffectiveRules(this)
        activeShieldCount = PatternPacks.CATEGORIES.count { categoryStates[it.id] == true }
    }

    private var cachedInboxLogs: List<LogEntry> = emptyList()
    @Volatile private var isFetchingInbox = false

    private fun loadInboxSmsAsync() {
        if (!hasSmsPermission || isFetchingInbox) return
        isFetchingInbox = true
        Thread {
            try {
                val fetched = com.qook.ftele.engine.SmsInboxHelper.fetchInboxMessages(
                    context = this,
                    rules = effectiveRules,
                    mode = currentMode,
                    limit = 500
                )
                runOnUiThread {
                    cachedInboxLogs = fetched
                    isFetchingInbox = false
                    updateMergedLogs()
                }
            } catch (e: Exception) {
                isFetchingInbox = false
            }
        }.start()
    }

    private fun refreshLogs() {
        updateMergedLogs()
    }

    private fun updateMergedLogs() {
        val liveLogs = RuleStore.getLogs(this)
        if (cachedInboxLogs.isNotEmpty()) {
            val liveKeys = liveLogs.map { "${it.sender.trim().lowercase()}:::${Patterns.loose(it.text)}" }.toSet()
            val uniqueInbox = cachedInboxLogs.filter { "${it.sender.trim().lowercase()}:::${Patterns.loose(it.text)}" !in liveKeys }
            logs = (liveLogs + uniqueInbox).sortedByDescending { it.time }
        } else {
            logs = liveLogs
        }
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

    private fun addPatternRule(pattern: String, action: RuleAction) {
        val clean = pattern.trim()
        if (clean.isBlank()) return

        val rule = Rule(
            id = PatternPacks.newRuleId(),
            action = action,
            target = RuleTarget.BODY,
            mode = MatchMode.CONTAINS,
            value = clean,
            category = if (action == RuleAction.BLOCK) "Block Pattern: $clean" else "Allow Pattern: $clean",
            enabled = true
        )
        val updated = customRules.toMutableList()
        val existingIdx = updated.indexOfFirst {
            it.target == RuleTarget.BODY && Patterns.loose(it.value) == Patterns.loose(clean)
        }
        if (existingIdx != -1) {
            updated[existingIdx] = rule
        } else {
            updated.add(0, rule)
        }
        RuleStore.saveCustomRules(this, updated)
        syncRulesAndCategories()
        val actionText = if (action == RuleAction.BLOCK) "Blocked message pattern" else "Allowed message pattern"
        Toast.makeText(this, "$actionText: \"$clean\"", Toast.LENGTH_SHORT).show()
    }
}
