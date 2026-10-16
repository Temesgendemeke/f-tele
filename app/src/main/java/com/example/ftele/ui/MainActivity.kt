package com.example.ftele.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ftele.R
import com.example.ftele.data.RuleStore
import com.example.ftele.databinding.ActivityMainBinding
import com.example.ftele.databinding.ItemShieldBinding
import com.example.ftele.engine.PatternPacks
import com.example.ftele.engine.Patterns
import com.example.ftele.engine.Simulations
import com.example.ftele.model.LogEntry
import com.example.ftele.model.MatchMode
import com.example.ftele.model.Rule
import com.example.ftele.model.RuleAction
import com.example.ftele.model.RuleTarget
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var activityAdapter: ActivityAdapter
    private lateinit var ruleAdapter: RuleAdapter

    private var customRules: MutableList<Rule> = mutableListOf()
    private var logs: List<LogEntry> = emptyList()

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            refreshData(onlyLog = true)
            refreshHandler.postDelayed(this, 3000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupMasterSwitch()
        setupPermissionWarning()
        setupModeToggle()
        setupTabs()
        setupActivityList()
        setupShieldsSection()
        setupRulesSection()
        setupTryIt()

        refreshData(onlyLog = false)
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
        refreshData(onlyLog = false)
        refreshHandler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    private fun setupMasterSwitch() {
        b.switchMaster.isChecked = RuleStore.isEnabled(this)
        b.switchMaster.setOnCheckedChangeListener { _, isChecked ->
            RuleStore.setEnabled(this, isChecked)
        }
    }

    private fun setupPermissionWarning() {
        b.btnOpenAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun checkPermission() {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: ""
        val hasPermission = flat.contains(packageName)
        b.cardPermission.visibility = if (hasPermission) View.GONE else View.VISIBLE
    }

    private fun setupModeToggle() {
        val currentMode = RuleStore.getMode(this)
        updateModeUI(currentMode)

        b.btnModeBlocklist.setOnClickListener {
            RuleStore.setMode(this, "blocklist")
            updateModeUI("blocklist")
            refreshTryIt()
        }
        b.btnModeAllowlist.setOnClickListener {
            RuleStore.setMode(this, "allowlist")
            updateModeUI("allowlist")
            refreshTryIt()
        }
    }

    private fun updateModeUI(mode: String) {
        val isBlocklist = mode == "blocklist"
        b.btnModeBlocklist.setBackgroundResource(if (isBlocklist) R.drawable.bg_tab_indicator else R.drawable.bg_input)
        b.btnModeBlocklist.setTextColor(if (isBlocklist) 0xFFFFFFFF.toInt() else ContextCompat.getColor(this, R.color.ink))

        b.btnModeAllowlist.setBackgroundResource(if (!isBlocklist) R.drawable.bg_tab_indicator else R.drawable.bg_input)
        b.btnModeAllowlist.setTextColor(if (!isBlocklist) 0xFFFFFFFF.toInt() else ContextCompat.getColor(this, R.color.ink))

        b.tvModeDesc.text = if (isBlocklist) {
            "Only senders and words on your block list are muted. Everything else rings as usual."
        } else {
            "Every SMS is muted unless it matches an allow rule. Add your banks and contacts first."
        }
    }

    private fun setupTabs() {
        showTab(0)
        b.btnTabActivity.setOnClickListener { showTab(0) }
        b.btnTabShields.setOnClickListener { showTab(1) }
        b.btnTabRules.setOnClickListener { showTab(2) }
    }

    private fun showTab(index: Int) {
        b.layoutActivity.visibility = if (index == 0) View.VISIBLE else View.GONE
        b.layoutShields.visibility = if (index == 1) View.VISIBLE else View.GONE
        b.layoutRules.visibility = if (index == 2) View.VISIBLE else View.GONE

        b.btnTabActivity.setBackgroundResource(if (index == 0) R.drawable.bg_tab_indicator else android.R.color.transparent)
        b.btnTabActivity.setTextColor(if (index == 0) 0xFFFFFFFF.toInt() else ContextCompat.getColor(this, R.color.sub))

        b.btnTabShields.setBackgroundResource(if (index == 1) R.drawable.bg_tab_indicator else android.R.color.transparent)
        b.btnTabShields.setTextColor(if (index == 1) 0xFFFFFFFF.toInt() else ContextCompat.getColor(this, R.color.sub))

        b.btnTabRules.setBackgroundResource(if (index == 2) R.drawable.bg_tab_indicator else android.R.color.transparent)
        b.btnTabRules.setTextColor(if (index == 2) 0xFFFFFFFF.toInt() else ContextCompat.getColor(this, R.color.sub))
    }

    private fun setupActivityList() {
        activityAdapter = ActivityAdapter(
            onAllowSender = { sender -> toggleSenderRule(sender, RuleAction.ALLOW) },
            onBlockSender = { sender -> toggleSenderRule(sender, RuleAction.BLOCK) }
        )
        b.rvActivity.layoutManager = LinearLayoutManager(this)
        b.rvActivity.adapter = activityAdapter

        b.btnSimulateEmpty.setOnClickListener { runSimulation() }
        b.btnClearActivity.setOnClickListener {
            RuleStore.clearLog(this)
            refreshData(onlyLog = true)
        }
    }

    private fun toggleSenderRule(sender: String, action: RuleAction) {
        val looseSender = Patterns.loose(sender)
        val existing = customRules.find {
            it.target == RuleTarget.SENDER && Patterns.loose(it.value) == looseSender
        }

        if (existing != null && existing.action == action) {
            customRules.remove(existing)
        } else {
            if (existing != null) customRules.remove(existing)
            val newRule = Rule(
                id = PatternPacks.newRuleId(),
                action = action,
                target = RuleTarget.SENDER,
                mode = MatchMode.EXACT,
                value = sender,
                category = "Sender Rule",
                enabled = true
            )
            customRules.add(0, newRule)
        }

        RuleStore.saveCustomRules(this, customRules)
        refreshData(onlyLog = false)
        refreshTryIt()
    }

    private fun setupShieldsSection() {
        b.btnSimulateShield.setOnClickListener {
            runSimulation()
            showTab(0)
            Toast.makeText(this, "Simulated SMS! Check Activity log.", Toast.LENGTH_SHORT).show()
        }
        renderShields()
    }

    private fun renderShields() {
        b.layoutShieldsList.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for (category in PatternPacks.CATEGORIES) {
            val itemBinding = ItemShieldBinding.inflate(inflater, b.layoutShieldsList, false)
            itemBinding.tvShieldIcon.text = category.icon
            itemBinding.tvShieldTitle.text = category.title
            itemBinding.tvShieldDesc.text = category.description
            itemBinding.tvShieldCount.text = "${category.rules.size} behavioral rules"

            if (category.isProtection) {
                itemBinding.tvShieldProtectionBadge.visibility = View.VISIBLE
                itemBinding.switchShield.visibility = View.GONE
            } else {
                itemBinding.tvShieldProtectionBadge.visibility = View.GONE
                itemBinding.switchShield.visibility = View.VISIBLE
                itemBinding.switchShield.isChecked = RuleStore.isCategoryEnabled(this, category.id)
                itemBinding.switchShield.setOnCheckedChangeListener { _, isChecked ->
                    RuleStore.setCategoryEnabled(this, category.id, isChecked)
                    refreshData(onlyLog = false)
                    refreshTryIt()
                }
            }

            b.layoutShieldsList.addView(itemBinding.root)
        }
    }

    private fun setupRulesSection() {
        ruleAdapter = RuleAdapter(
            onToggleRule = { rule ->
                val idx = customRules.indexOfFirst { it.id == rule.id }
                if (idx != -1) {
                    customRules[idx] = rule.copy(enabled = !rule.enabled)
                    RuleStore.saveCustomRules(this, customRules)
                    refreshData(onlyLog = false)
                    refreshTryIt()
                }
            },
            onRemoveRule = { rule ->
                customRules.removeAll { it.id == rule.id }
                RuleStore.saveCustomRules(this, customRules)
                refreshData(onlyLog = false)
                refreshTryIt()
            }
        )

        b.rvRules.layoutManager = LinearLayoutManager(this)
        b.rvRules.adapter = ruleAdapter

        b.toggleTarget.check(R.id.btn_target_body)
        b.toggleAction.check(R.id.btn_action_block)

        b.btnAddRule.setOnClickListener {
            val value = b.etDraftValue.text?.toString()?.trim() ?: ""
            if (value.isEmpty()) return@setOnClickListener

            val senderFilter = b.etDraftSender.text?.toString()?.trim()?.ifBlank { null }

            val target = when (b.toggleTarget.checkedRadioButtonId) {
                R.id.btn_target_sender -> RuleTarget.SENDER
                R.id.btn_target_body -> RuleTarget.BODY
                else -> RuleTarget.BOTH
            }

            val mode = when (b.chipgroupMode.checkedRadioButtonId) {
                R.id.chip_mode_exact -> MatchMode.EXACT
                R.id.chip_mode_prefix -> MatchMode.PREFIX
                R.id.chip_mode_suffix -> MatchMode.SUFFIX
                R.id.chip_mode_wildcard -> MatchMode.WILDCARD
                R.id.chip_mode_regex -> MatchMode.REGEX
                else -> MatchMode.CONTAINS
            }

            if (mode == MatchMode.REGEX) {
                try {
                    Regex(value)
                } catch (_: Exception) {
                    b.tvRegexError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
            }
            b.tvRegexError.visibility = View.GONE

            val action = if (b.toggleAction.checkedRadioButtonId == R.id.btn_action_allow) {
                RuleAction.ALLOW
            } else {
                RuleAction.BLOCK
            }

            val newRule = Rule(
                id = PatternPacks.newRuleId(),
                action = action,
                target = target,
                mode = mode,
                value = value,
                senderFilter = senderFilter,
                category = "Custom Rule",
                enabled = true
            )

            val existingIdx = customRules.indexOfFirst { it.key == newRule.key }
            if (existingIdx != -1) {
                customRules[existingIdx] = newRule
            } else {
                customRules.add(0, newRule)
            }

            RuleStore.saveCustomRules(this, customRules)
            b.etDraftValue.setText("")
            b.etDraftSender.setText("")
            refreshData(onlyLog = false)
            refreshTryIt()
        }
    }

    private fun setupTryIt() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                refreshTryIt()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        b.etTestSender.addTextChangedListener(watcher)
        b.etTestText.addTextChangedListener(watcher)
    }

    private fun refreshTryIt() {
        val sender = b.etTestSender.text?.toString() ?: ""
        val text = b.etTestText.text?.toString() ?: ""

        val effectiveRules = RuleStore.getAllEffectiveRules(this)
        val decision = Patterns.decide(effectiveRules, RuleStore.getMode(this), sender, text)

        if (decision.mute) {
            b.tvTestChip.text = "Would mute"
            b.tvTestChip.setTextColor(ContextCompat.getColor(this, R.color.block))
            b.tvTestChip.setBackgroundResource(R.drawable.bg_chip_muted)
        } else {
            b.tvTestChip.text = "Would show"
            b.tvTestChip.setTextColor(ContextCompat.getColor(this, R.color.allow))
            b.tvTestChip.setBackgroundResource(R.drawable.bg_chip_shown)
        }

        b.tvTestReason.text = decision.reason
    }

    private fun runSimulation() {
        val sample = Simulations.SAMPLES[Random.nextInt(Simulations.SAMPLES.size)]
        val decision = RuleStore.decide(this, sample.sender, sample.text)
        RuleStore.addLog(this, sample.sender, sample.text, decision)
        refreshData(onlyLog = true)
    }

    private fun refreshData(onlyLog: Boolean) {
        logs = RuleStore.getLog(this)
        if (logs.isEmpty()) {
            b.rvActivity.visibility = View.GONE
            b.layoutEmptyActivity.visibility = View.VISIBLE
            b.btnClearActivity.visibility = View.GONE
        } else {
            b.rvActivity.visibility = View.VISIBLE
            b.layoutEmptyActivity.visibility = View.GONE
            b.btnClearActivity.visibility = View.VISIBLE
            activityAdapter.submitList(logs)
        }

        val effectiveRules = RuleStore.getAllEffectiveRules(this)
        activityAdapter.updateRules(effectiveRules)

        b.btnTabActivity.text = "Activity (${logs.size})"

        var activeShieldCount = 0
        for (c in PatternPacks.CATEGORIES) {
            if (RuleStore.isCategoryEnabled(this, c.id)) activeShieldCount++
        }
        b.btnTabShields.text = "Shields ($activeShieldCount/${PatternPacks.CATEGORIES.size})"

        if (!onlyLog) {
            customRules = RuleStore.getCustomRules(this).toMutableList()
            ruleAdapter.submitList(customRules.toList())
            b.tvEmptyRules.visibility = if (customRules.isEmpty()) View.VISIBLE else View.GONE
            b.btnTabRules.text = "Custom Rules (${customRules.size})"
        }
    }
}
