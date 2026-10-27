package com.example.ftele.data

import android.content.Context
import com.example.ftele.engine.PatternPacks
import com.example.ftele.engine.Patterns
import com.example.ftele.model.BehaviorCategory
import com.example.ftele.model.Decision
import com.example.ftele.model.LogEntry
import com.example.ftele.model.MatchMode
import com.example.ftele.model.Rule
import com.example.ftele.model.RuleAction
import com.example.ftele.model.RuleTarget
import org.json.JSONArray
import org.json.JSONObject

object RuleStore {
    private const val PREFS = "sms_muter"
    private const val MAX_LOG = 150

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(c: Context): Boolean = prefs(c).getBoolean("enabled", true)

    fun setEnabled(c: Context, value: Boolean) {
        prefs(c).edit().putBoolean("enabled", value).apply()
    }

    /** "blocklist": mute only blocked senders/words. "allowlist": mute everything except allowed. */
    fun getMode(c: Context): String = prefs(c).getString("mode", "blocklist") ?: "blocklist"

    fun setMode(c: Context, value: String) {
        prefs(c).edit().putString("mode", value).apply()
    }

    /** Theme mode: "system", "dark", "light" */
    fun getThemeMode(c: Context): String = prefs(c).getString("theme_mode", "system") ?: "system"

    fun setThemeMode(c: Context, value: String) {
        prefs(c).edit().putString("theme_mode", value).apply()
    }

    // --- Behavioral Category Toggles ---

    fun isCategoryEnabled(c: Context, catId: String): Boolean {
        if (catId == "cat_protection") return true // Non-negotiable shield
        return prefs(c).getBoolean("cat_$catId", true)
    }

    fun setCategoryEnabled(c: Context, catId: String, enabled: Boolean) {
        if (catId == "cat_protection") return // Cannot be turned off
        prefs(c).edit().putBoolean("cat_$catId", enabled).apply()
    }

    fun getCategoryRules(c: Context): List<Rule> {
        val list = mutableListOf<Rule>()
        for (category in PatternPacks.CATEGORIES) {
            if (isCategoryEnabled(c, category.id)) {
                list.addAll(category.rules.map { preset ->
                    Rule(
                        id = "cat_${category.id}_${preset.value.hashCode()}",
                        action = preset.action,
                        target = preset.target,
                        mode = preset.mode,
                        value = preset.value,
                        senderFilter = preset.senderFilter,
                        category = preset.category ?: category.title,
                        enabled = true
                    )
                })
            }
        }
        return list
    }

    // --- Custom User Rules ---

    fun getCustomRules(c: Context): List<Rule> {
        val raw = prefs(c).getString("custom_rules", null)
        if (raw != null) {
            return parseRules(raw)
        }
        // Legacy fallback
        val legacy = prefs(c).getString("rules", null)
        return if (legacy != null) parseRules(legacy) else emptyList()
    }

    fun saveCustomRules(c: Context, rules: List<Rule>) {
        val array = JSONArray()
        for (r in rules) {
            val obj = JSONObject()
                .put("id", r.id)
                .put("action", r.action.value)
                .put("target", r.target.value)
                .put("mode", r.mode.value)
                .put("value", r.value)
                .put("senderFilter", r.senderFilter ?: "")
                .put("category", r.category ?: "")
                .put("enabled", r.enabled)
            array.put(obj)
        }
        prefs(c).edit()
            .putString("custom_rules", array.toString())
            .putString("rules", array.toString()) // Keep legacy key in sync
            .apply()
    }

    // Backward-compatible alias for custom rules
    fun getRules(c: Context): List<Rule> = getCustomRules(c)
    fun saveRules(c: Context, rules: List<Rule>) = saveCustomRules(c, rules)

    /**
     * All effective rules: Active behavioral category rules + user custom rules.
     */
    fun getAllEffectiveRules(c: Context): List<Rule> {
        return getCategoryRules(c) + getCustomRules(c)
    }

    fun parseRules(jsonStr: String): List<Rule> {
        val list = mutableListOf<Rule>()
        if (jsonStr.isBlank()) return list
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val id = obj.optString("id", PatternPacks.newRuleId())
                val value = obj.optString("value", "")
                if (value.isEmpty()) continue

                val actionStr = obj.optString("action", "block")
                val action = RuleAction.fromValue(actionStr)

                val target = if (obj.has("target")) {
                    RuleTarget.fromValue(obj.optString("target", "body"))
                } else if (obj.optString("type") == "sender") {
                    RuleTarget.SENDER
                } else {
                    RuleTarget.BODY
                }

                val mode = if (obj.has("mode")) {
                    MatchMode.fromValue(obj.optString("mode", "contains"))
                } else {
                    MatchMode.CONTAINS
                }

                val senderFilter = obj.optString("senderFilter", "").ifBlank { null }
                val category = obj.optString("category", "").ifBlank { null }
                val enabled = obj.optBoolean("enabled", true)

                list.add(
                    Rule(
                        id = id,
                        action = action,
                        target = target,
                        mode = mode,
                        value = value,
                        senderFilter = senderFilter,
                        category = category,
                        enabled = enabled
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    // --- Activity Log ---

    fun getLog(c: Context): List<LogEntry> {
        val list = mutableListOf<LogEntry>()
        val raw = prefs(c).getString("log", "[]") ?: "[]"
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(
                    LogEntry(
                        id = obj.optString("id", i.toString()),
                        sender = obj.optString("sender", ""),
                        text = obj.optString("text", ""),
                        time = obj.optLong("time", System.currentTimeMillis()),
                        muted = obj.optBoolean("muted", false),
                        reason = obj.optString("reason", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun clearLog(c: Context) {
        prefs(c).edit().putString("log", "[]").apply()
    }

    /**
     * Decides notification fate using all active category shields + custom rules.
     */
    fun decide(c: Context, title: String, text: String): Decision {
        val rules = getAllEffectiveRules(c)
        return Patterns.decide(rules, getMode(c), title, text)
    }

    @Synchronized
    fun addLog(c: Context, sender: String, text: String, decision: Decision) {
        val current = getLog(c).toMutableList()
        val entry = LogEntry(
            id = System.nanoTime().toString(),
            sender = sender,
            text = if (text.length > 240) text.substring(0, 240) else text,
            time = System.currentTimeMillis(),
            muted = decision.mute,
            reason = decision.reason
        )

        current.add(0, entry)
        if (current.size > MAX_LOG) {
            current.subList(MAX_LOG, current.size).clear()
        }

        val array = JSONArray()
        for (item in current) {
            val obj = JSONObject()
                .put("id", item.id)
                .put("sender", item.sender)
                .put("text", item.text)
                .put("time", item.time)
                .put("muted", item.muted)
                .put("reason", item.reason)
            array.put(obj)
        }
        prefs(c).edit().putString("log", array.toString()).apply()
    }
}
