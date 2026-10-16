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
import com.google.android.material.tabs.TabLayout
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
        WindowCompat.setDecorFitsSystemWindows(window, false)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        initData()
        setupHeader()
        setupModeCard()
        setupTabs()
        setupActivityList()
        setupShieldsSection()
        setupRulesSection()
        setupTryIt()
        refreshTryIt()
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
        refreshData(onlyLog = false)
        refreshHandler.postDelayed(refreshRunnable, 2000)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    private fun initData() {
        customRules = RuleStore.getCustomRules(this).toMutableList()
    }

    private fun checkPermission() {
        val granted = isAccessGranted()
        b.cardPermission.visibility = if (granted) View.GONE else View.VISIBLE
    }

    private fun isAccessGranted(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        ) ?: ""
        return enabled.split(":").any { it.startsWith("$packageName/") }
    }

    private fun setupHeader() {
        b.switchMaster.isChecked = RuleStore.isEnabled(this)
        b.switchMaster.setOnCheckedChangeListener { _, isChecked ->
            RuleStore.setEnabled(this, isChecked)
        }

        b.btnOpenAccess.setOnClickListener {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun setupModeCard() {
        val currentMode = RuleStore.getMode(this)
        if (currentMode == "allowlist") {
            b.toggleMode.check(R.id.btn_mode_allowlist)
            updateModeDesc("allowlist")
        } else {
            b.toggleMode.check(R.id.btn_mode_blocklist)
            updateModeDesc("blocklist")
        }

        b.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val newMode = if (checkedId == R.id.btn_mode_allowlist) "allowlist" else "blocklist"
            RuleStore.setMode(this, newMode)
            updateModeDesc(newMode)
            refreshTryIt()
        }
    }

    private fun updateModeDesc(mode: String) {
        if (mode == "blocklist") {
            b.tvModeDesc.text =
                "Only senders and words on your block list are muted. Everything else rings as usual."
        } else {
            b.tvModeDesc.text =
                "Every SMS is muted unless it matches an allow rule. Add your banks and contacts first."
        }
    }

    private fun setupTabs() {
        b.tabLayout.removeAllTabs()
        b.tabLayout.addTab(b.tabLayout.newTab().setText("Activity (0)"))
        b.tabLayout.addTab(b.tabLayout.newTab().setText("Shields"))
        b.tabLayout.addTab(b.tabLayout.newTab().setText("Custom Rules (0)"))

        b.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        b.layoutActivity.visibility = View.VISIBLE
                        b.layoutShields.visibility = View.GONE
                        b.layoutRules.visibility = View.GONE
                    }
                    1 -> {
                        b.layoutActivity.visibility = View.GONE
                        b.layoutShields.visibility = View.VISIBLE
                        b.layoutRules.visibility = View.GONE
                    }
                    2 -> {
                        b.layoutActivity.visibility = View.GONE
                        b.layoutShields.visibility = View.GONE
                        b.layoutRules.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
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
            b.tabLayout.getTabAt(0)?.select()
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
            itemBinding.tvShieldCount.text = "${category.rules.size} rules active"

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

            val target = when (b.toggleTarget.checkedButtonId) {
                R.id.btn_target_sender -> RuleTarget.SENDER
                R.id.btn_target_body -> RuleTarget.BODY
                else -> RuleTarget.BOTH
            }

            val mode = when (b.chipgroupMode.checkedChipId) {
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

            val action = if (b.toggleAction.checkedButtonId == R.id.btn_action_allow) {
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

        b.tabLayout.getTabAt(0)?.text = "Activity (${logs.size})"

        var activeShieldCount = 0
        for (c in PatternPacks.CATEGORIES) {
            if (RuleStore.isCategoryEnabled(this, c.id)) activeShieldCount++
        }
        b.tabLayout.getTabAt(1)?.text = "Shields ($activeShieldCount/${PatternPacks.CATEGORIES.size})"

        if (!onlyLog) {
            customRules = RuleStore.getCustomRules(this).toMutableList()
            ruleAdapter.submitList(customRules.toList())
            b.tvEmptyRules.visibility = if (customRules.isEmpty()) View.VISIBLE else View.GONE
            b.tabLayout.getTabAt(2)?.text = "Custom Rules (${customRules.size})"
        }
    }
}
