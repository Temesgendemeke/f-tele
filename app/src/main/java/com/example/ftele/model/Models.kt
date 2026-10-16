package com.example.ftele.model

enum class MatchMode(val value: String, val label: String) {
    CONTAINS("contains", "Contains"),
    EXACT("exact", "Exact"),
    PREFIX("prefix", "Starts"),
    SUFFIX("suffix", "Ends"),
    WILDCARD("wildcard", "Wildcard"),
    REGEX("regex", "Regex");

    companion object {
        fun fromValue(value: String): MatchMode =
            entries.find { it.value.equals(value, ignoreCase = true) } ?: CONTAINS
    }
}

enum class RuleTarget(val value: String, val label: String) {
    SENDER("sender", "Sender"),
    BODY("body", "Message"),
    BOTH("both", "Both");

    companion object {
        fun fromValue(value: String): RuleTarget =
            entries.find { it.value.equals(value, ignoreCase = true) } ?: BODY
    }
}

enum class RuleAction(val value: String, val label: String) {
    ALLOW("allow", "Allow"),
    BLOCK("block", "Block");

    companion object {
        fun fromValue(value: String): RuleAction =
            entries.find { it.value.equals(value, ignoreCase = true) } ?: BLOCK
    }
}

data class Rule(
    val id: String,
    val action: RuleAction,
    val target: RuleTarget,
    val mode: MatchMode,
    val value: String,
    val senderFilter: String? = null,
    val category: String? = null,
    val enabled: Boolean = true
) {
    val key: String
        get() = "${action.value}|${target.value}|${mode.value}|${senderFilter?.trim()?.lowercase() ?: ""}|${value.trim().lowercase()}"
}

data class PresetRule(
    val action: RuleAction,
    val target: RuleTarget,
    val mode: MatchMode,
    val value: String,
    val senderFilter: String? = null,
    val category: String? = null
)

data class BehaviorCategory(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isProtection: Boolean = false,
    val defaultEnabled: Boolean = true,
    val rules: List<PresetRule>
)

data class PatternPack(
    val id: String,
    val name: String,
    val description: String,
    val seed: Boolean = false,
    val rules: List<PresetRule>
)

data class LogEntry(
    val id: String,
    val sender: String,
    val text: String,
    val time: Long,
    val muted: Boolean,
    val reason: String
)

data class Decision(
    val mute: Boolean,
    val reason: String
)
