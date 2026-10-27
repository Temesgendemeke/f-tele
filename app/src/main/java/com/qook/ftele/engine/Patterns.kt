package com.qook.ftele.engine

import com.qook.ftele.model.Decision
import com.qook.ftele.model.MatchMode
import com.qook.ftele.model.Rule
import com.qook.ftele.model.RuleAction
import com.qook.ftele.model.RuleTarget
import java.text.Normalizer
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object Patterns {

    private val ZERO_WIDTH = Regex("[\\u200B-\\u200D\\uFEFF\\u00AD\\u2060]")
    private val WHITESPACE = Regex("[\\s\\u00A0]+")
    private val LOOSE_STRIP = Regex("[\\s\\-()_.]")
    private val SPECIAL_CHARS =
        setOf('.', '*', '+', '?', '^', '$', '{', '}', '(', ')', '|', '[', ']', '\\')

    private val regexCache = ConcurrentHashMap<String, Regex?>()

    /** Unicode-fold, drop invisible characters, lowercase, collapse whitespace. */
    fun clean(input: String): String =
        Normalizer.normalize(input, Normalizer.Form.NFKC)
            .replace(ZERO_WIDTH, "")
            .lowercase(Locale.ROOT)
            .replace(WHITESPACE, " ")
            .trim()

    /** Like [clean], but also drops common separators for loose matching. */
    fun loose(input: String): String = clean(input).replace(LOOSE_STRIP, "")

    private fun cached(key: String, build: () -> Regex?): Regex? =
        regexCache.computeIfAbsent(key) { build() }

    fun wildcardToRegex(pattern: String): Regex? {
        val out = StringBuilder()
        for (ch in pattern) {
            when (ch) {
                '*' -> out.append(".*")
                '?' -> out.append(".")
                else -> if (ch in SPECIAL_CHARS) out.append('\\').append(ch) else out.append(ch)
            }
        }
        return try {
            Regex(out.toString(), RegexOption.IGNORE_CASE)
        } catch (_: Exception) {
            null
        }
    }

    /** Test one pattern against one string. Empty patterns never match. */
    fun matchOne(value: String, mode: MatchMode, haystack: String): Boolean {
        if (value.isEmpty()) return false
        return when (mode) {
            MatchMode.EXACT -> loose(haystack) == loose(value)
            MatchMode.PREFIX -> loose(haystack).startsWith(loose(value))
            MatchMode.SUFFIX -> loose(haystack).endsWith(loose(value))
            MatchMode.WILDCARD -> cached("w:$value") { wildcardToRegex(value) }
                ?.containsMatchIn(clean(haystack)) ?: false
            MatchMode.REGEX -> cached("r:$value") {
                try {
                    Regex(value, RegexOption.IGNORE_CASE)
                } catch (_: Exception) {
                    null
                }
            }?.containsMatchIn(clean(haystack)) ?: false
            MatchMode.CONTAINS -> loose(haystack).contains(loose(value))
        }
    }

    /** Test one rule against title and text, respecting optional sender filter. */
    fun matchRule(rule: Rule, title: String, text: String): Boolean {
        if (!rule.enabled || rule.value.isEmpty()) return false
        if (!rule.senderFilter.isNullOrBlank()) {
            if (!matchOne(rule.senderFilter, MatchMode.CONTAINS, title)) {
                return false
            }
        }
        val targets = when (rule.target) {
            RuleTarget.BOTH -> listOf(title, text)
            RuleTarget.SENDER -> listOf(title)
            RuleTarget.BODY -> listOf(text)
        }
        return targets.any { matchOne(rule.value, rule.mode, it) }
    }

    /**
     * Decide whether a notification should be muted.
     * Allow rules ALWAYS take priority over block rules.
     */
    fun decide(
        rules: List<Rule>,
        mode: String,
        title: String,
        text: String
    ): Decision {
        // 1. Strict Priority: Check all enabled ALLOW rules first
        for (rule in rules) {
            if (rule.enabled && rule.action == RuleAction.ALLOW && matchRule(rule, title, text)) {
                return Decision(
                    mute = false,
                    reason = "Allowed: ${rule.category ?: rule.value}"
                )
            }
        }

        // 2. Check all enabled BLOCK rules next
        for (rule in rules) {
            if (rule.enabled && rule.action == RuleAction.BLOCK && matchRule(rule, title, text)) {
                return Decision(
                    mute = true,
                    reason = "Muted: ${rule.category ?: rule.value}"
                )
            }
        }

        // 3. Fallback based on application mode
        return if (mode.equals("allowlist", ignoreCase = true)) {
            Decision(
                mute = true,
                reason = "Muted: Not in allow list (Strict Mode)"
            )
        } else {
            Decision(
                mute = false,
                reason = "Allowed: No block rule matched"
            )
        }
    }
}
