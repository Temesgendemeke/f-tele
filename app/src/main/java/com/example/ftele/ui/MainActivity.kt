package com.example.ftele.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ftele.R
import com.example.ftele.data.RuleStore
import com.example.ftele.databinding.ActivityMainBinding
import com.example.ftele.databinding.ItemPackBinding
import com.example.ftele.engine.PatternPacks
import com.example.ftele.engine.Patterns
import com.example.ftele.engine.Simulations
import com.example.ftele.model.LogEntry
import com.example.ftele.model.MatchMode
import com.example.ftele.model.PatternPack
import com.example.ftele.model.Rule
import com.example.ftele.model.RuleAction
import com.example.ftele.model.RuleTarget
import com.google.android.material.tabs.TabLayout
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var activityAdapter: ActivityAdapter
    private lateinit var ruleAdapter: RuleAdapter

    private var rules: MutableList<Rule> = mutableListOf()
    private var logs: List<LogEntry> = emptyList()

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            refreshData(onlyLog = true)
            refreshHandler.postDelayed(this, 2000)
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
        setupRulesSection()
        setupTryIt()
        refreshPacks()
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
        val storedRules = RuleStore.getRules(this)
        rules = if (storedRules == null) {
            val defaults = PatternPacks.seedRules()
            RuleStore.saveRules(this, defaults)
            defaults.toMutableList()
        } else {
            storedRules.toMutableList()
        }
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
        b.tabLayout.addTab(b.tabLayout.newTab().setText("Rules (0)"))

        b.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        b.layoutActivity.visibility = View.VISIBLE
                        b.layoutRules.visibility = View.GONE
                    }
                    1 -> {
                        b.layoutActivity.visibility = View.GONE
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
        val existing = rules.find {
            it.target == RuleTarget.SENDER && Patterns.loose(it.value) == looseSender
        }

        if (existing != null && existing.action == action) {
            rules.remove(existing)
        } else {
            if (existing != null) rules.remove(existing)
            val newRule = Rule(
                id = PatternPacks.newRuleId(),
                action = action,
                target = RuleTarget.SENDER,
                mode = MatchMode.CONTAINS,
                value = sender,
                enabled = true
            )
            rules.add(0, newRule)
        }

        RuleStore.saveRules(this, rules)
        refreshData(onlyLog = false)
    }

    private fun setupRulesSection() {
        b.toggleTarget.check(R.id.btn_target_body)
        b.toggleAction.check(R.id.btn_action_block)

        ruleAdapter = RuleAdapter(
            onToggleRule = { rule ->
                val index = rules.indexOfFirst { it.id == rule.id }
                if (index != -1) {
                    rules[index] = rule.copy(enabled = !rule.enabled)
                    RuleStore.saveRules(this, rules)
                    refreshData(onlyLog = false)
                }
            },
            onRemoveRule = { rule ->
                rules.removeAll { it.id == rule.id }
                RuleStore.saveRules(this, rules)
                refreshData(onlyLog = false)
            }
        )
        b.rvRules.layoutManager = LinearLayoutManager(this)
        b.rvRules.adapter = ruleAdapter

        b.etDraftValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateRegex()
                refreshTryIt()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        b.chipgroupMode.setOnCheckedStateChangeListener { _, _ ->
            updateDraftHint()
            validateRegex()
            refreshTryIt()
        }

        b.toggleTarget.addOnButtonCheckedListener { _, _, _ -> refreshTryIt() }
        b.toggleAction.addOnButtonCheckedListener { _, _, _ -> refreshTryIt() }

        b.btnAddRule.setOnClickListener { addDraftRule() }
    }

    private fun getSelectedDraftMode(): MatchMode = when (b.chipgroupMode.checkedChipId) {
        R.id.chip_mode_exact -> MatchMode.EXACT
        R.id.chip_mode_prefix -> MatchMode.PREFIX
        R.id.chip_mode_suffix -> MatchMode.SUFFIX
        R.id.chip_mode_wildcard -> MatchMode.WILDCARD
        R.id.chip_mode_regex -> MatchMode.REGEX
        else -> MatchMode.CONTAINS
    }

    private fun getSelectedDraftTarget(): RuleTarget = when (b.toggleTarget.checkedButtonId) {
        R.id.btn_target_sender -> RuleTarget.SENDER
        R.id.btn_target_both -> RuleTarget.BOTH
        else -> RuleTarget.BODY
    }

    private fun getSelectedDraftAction(): RuleAction = when (b.toggleAction.checkedButtonId) {
        R.id.btn_action_allow -> RuleAction.ALLOW
        else -> RuleAction.BLOCK
    }

    private fun updateDraftHint() {
        b.etDraftValue.hint = when (getSelectedDraftMode()) {
            MatchMode.REGEX -> "Regular expression"
            MatchMode.WILDCARD -> "Pattern with * and ?"
            else -> "Text to match"
        }
    }

    private fun validateRegex(): Boolean {
        if (getSelectedDraftMode() != MatchMode.REGEX) {
            b.tvRegexError.visibility = View.GONE
            return true
        }
        val text = b.etDraftValue.text.toString().trim()
        if (text.isEmpty()) {
            b.tvRegexError.visibility = View.GONE
            return true
        }
        return try {
            Regex(text)
            b.tvRegexError.visibility = View.GONE
            true
        } catch (_: Exception) {
            b.tvRegexError.visibility = View.VISIBLE
            false
        }
    }

    private fun addDraftRule() {
        val value = b.etDraftValue.text.toString().trim()
        if (value.isEmpty()) return
        if (!validateRegex()) return

        val action = getSelectedDraftAction()
        val target = getSelectedDraftTarget()
        val mode = getSelectedDraftMode()

        val candidate = Rule(
            id = PatternPacks.newRuleId(),
            action = action,
            target = target,
            mode = mode,
            value = value,
            enabled = true
        )

        val alreadyExists = rules.any { it.key == candidate.key }
        if (!alreadyExists) {
            rules.add(0, candidate)
            RuleStore.saveRules(this, rules)
            refreshData(onlyLog = false)
        }

        b.etDraftValue.setText("")
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
        val sender = b.etTestSender.text.toString()
        val text = b.etTestText.text.toString()

        val draftText = b.etDraftValue.text.toString().trim()
        val effectiveRules = rules.toMutableList()

        if (draftText.isNotEmpty() && validateRegex()) {
            effectiveRules.add(
                0,
                Rule(
                    id = "draft",
                    action = getSelectedDraftAction(),
                    target = getSelectedDraftTarget(),
                    mode = getSelectedDraftMode(),
                    value = draftText,
                    enabled = true
                )
            )
        }

        val appMode = RuleStore.getMode(this)
        val decision = Patterns.decide(effectiveRules, appMode, sender, text)

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

    private fun refreshPacks() {
        b.layoutPacksContainer.removeAllViews()
        val currentKeys = rules.map { it.key }.toSet()

        for (pack in PatternPacks.ALL) {
            val packBinding = ItemPackBinding.inflate(
                LayoutInflater.from(this),
                b.layoutPacksContainer,
                false
            )
            packBinding.tvPackName.text = pack.name
            packBinding.tvPackDesc.text = pack.description

            val packRuleKeys = pack.rules.map {
                "${it.action.value}|${it.target.value}|${it.mode.value}|${it.value.trim().lowercase()}"
            }
            val isAdded = packRuleKeys.all { currentKeys.contains(it) }

            if (isAdded) {
                packBinding.btnAddPack.text = "Added"
                packBinding.btnAddPack.isEnabled = false
                packBinding.btnAddPack.setTextColor(ContextCompat.getColor(this, R.color.sub))
                packBinding.btnAddPack.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.line))
            } else {
                packBinding.btnAddPack.text = "Add"
                packBinding.btnAddPack.isEnabled = true
                packBinding.btnAddPack.setTextColor(ContextCompat.getColor(this, R.color.accent))
                packBinding.btnAddPack.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accentSoft))
                packBinding.btnAddPack.setOnClickListener {
                    addPack(pack)
                }
            }

            b.layoutPacksContainer.addView(packBinding.root)
        }
    }

    private fun addPack(pack: PatternPack) {
        val currentKeys = rules.map { it.key }.toSet()
        val additions = pack.rules
            .filter {
                val key = "${it.action.value}|${it.target.value}|${it.mode.value}|${it.value.trim().lowercase()}"
                !currentKeys.contains(key)
            }
            .map { PatternPacks.presetToRule(it) }

        if (additions.isNotEmpty()) {
            rules.addAll(0, additions)
            RuleStore.saveRules(this, rules)
            refreshData(onlyLog = false)
        }
    }

    private fun runSimulation() {
        val sample = Simulations.SAMPLES[Random.nextInt(Simulations.SAMPLES.size)]
        val decision = RuleStore.decide(this, sample.sender, sample.text)
        RuleStore.addLog(this, sample.sender, sample.text, decision)
        refreshData(onlyLog = true)
    }

    private fun refreshData(onlyLog: Boolean) {
        logs = RuleStore.getLog(this)
        activityAdapter.submitList(logs)
        activityAdapter.updateRules(rules)

        val logCount = logs.size
        b.tabLayout.getTabAt(0)?.text = "Activity ($logCount)"
        b.layoutEmptyActivity.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
        b.btnClearActivity.visibility = if (logs.isNotEmpty()) View.VISIBLE else View.GONE

        if (!onlyLog) {
            val stored = RuleStore.getRules(this)
            if (stored != null) {
                rules.clear()
                rules.addAll(stored)
            }
            ruleAdapter.submitList(rules.toList())
            val ruleCount = rules.size
            b.tabLayout.getTabAt(1)?.text = "Rules ($ruleCount)"
            b.tvEmptyRules.visibility = if (rules.isEmpty()) View.VISIBLE else View.GONE
            refreshPacks()
            refreshTryIt()
        }
    }
}
