package com.qook.ftele.data

import android.content.Context
import android.content.SharedPreferences
import com.qook.ftele.engine.PatternPacks
import com.qook.ftele.model.LogEntry
import com.qook.ftele.model.MatchMode
import com.qook.ftele.model.Rule
import com.qook.ftele.model.RuleAction
import com.qook.ftele.model.RuleTarget
import org.json.JSONArray
import org.json.JSONObject

object RuleStore {

    private const val PREFS_NAME = "ftele_prefs"
    private const val KEY_ENABLED = "enabled"
    private const val MAX_LOGS = 250

    private fun prefs(c: Context): SharedPreferences =
        c.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- Master Protection Switch ---

    fun isEnabled(c: Context): Boolean = prefs(c).getBoolean(KEY_ENABLED, true)

    fun setEnabled(c: Context, value: Boolean) {
        prefs(c).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    // --- Filtering Mode ("blocklist" or "allowlist") ---

    fun getMode(c: Context): String = prefs(c).getString("mode", "blocklist") ?: "blocklist"

    fun setMode(c: Context, value: String) {
        prefs(c).edit().putString("mode", value).apply()
    }

    // --- Theme Mode ("system", "dark", "light") ---

    fun getThemeMode(c: Context): String = prefs(c).getString("theme_mode", "system") ?: "system"

    fun setThemeMode(c: Context, value: String) {
        prefs(c).edit().putString("theme_mode", value).apply()
    }

    // --- Behavioral Category Toggles (Spam Shields & User Preferences) ---

    fun isCategoryEnabled(c: Context, catId: String): Boolean {
        if (catId == "cat_protection") return true // Non-negotiable core shield
        val category = PatternPacks.CATEGORIES.find { it.id == catId }
        val defaultVal = category?.defaultEnabled ?: false
        return prefs(c).getBoolean("cat_$catId", defaultVal)
    }

    fun setCategoryEnabled(c: Context, catId: String, enabled: Boolean) {
        if (catId == "cat_protection") return // Core protection cannot be disabled
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
            .putString("rules", array.toString())
            .apply()
    }

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
        } catch (_: Exception) {
        }
        return list
    }

    // --- Activity Log History ---

    fun getLogs(c: Context): List<LogEntry> {
        val raw = prefs(c).getString("logs", null) ?: return emptyList()
        val list = mutableListOf<LogEntry>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(
                    LogEntry(
                        id = obj.optString("id", i.toString()),
                        sender = obj.optString("sender", "Unknown"),
                        text = obj.optString("text", ""),
                        time = obj.optLong("time", 0L),
                        muted = obj.optBoolean("muted", false),
                        reason = obj.optString("reason", "")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun appendLog(c: Context, entry: LogEntry) {
        val existing = getLogs(c).toMutableList()
        existing.add(0, entry)
        while (existing.size > MAX_LOGS) {
            existing.removeAt(existing.size - 1)
        }
        val array = JSONArray()
        for (e in existing) {
            array.put(
                JSONObject()
                    .put("id", e.id)
                    .put("sender", e.sender)
                    .put("text", e.text)
                    .put("time", e.time)
                    .put("muted", e.muted)
                    .put("reason", e.reason)
            )
        }
        prefs(c).edit().putString("logs", array.toString()).apply()
    }

    fun clearLog(c: Context) {
        prefs(c).edit().remove("logs").apply()
    }
}
