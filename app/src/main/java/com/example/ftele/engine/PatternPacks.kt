package com.example.ftele.engine

import com.example.ftele.model.MatchMode
import com.example.ftele.model.PatternPack
import com.example.ftele.model.PresetRule
import com.example.ftele.model.Rule
import com.example.ftele.model.RuleAction
import com.example.ftele.model.RuleTarget
import java.util.UUID

object PatternPacks {

    fun newRuleId(): String = UUID.randomUUID().toString()

    val ALL: List<PatternPack> = listOf(
        PatternPack(
            id = "otp",
            name = "OTP & verification codes",
            description = "Always show one-time passcodes, even from blocked senders.",
            seed = true,
            rules = listOf(
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "otp"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "verification code"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "one-time passcode"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.WILDCARD, "code * do not share")
            )
        ),
        PatternPack(
            id = "trusted-senders",
            name = "Trusted senders",
            description = "A starter allow list. Add your bank and contacts in the app.",
            seed = true,
            rules = listOf(
                PresetRule(RuleAction.ALLOW, RuleTarget.SENDER, MatchMode.CONTAINS, "CBE")
            )
        ),
        PatternPack(
            id = "scam-words",
            name = "Scam & fraud words",
            description = "Common lures used by SMS scammers.",
            seed = true,
            rules = listOf(
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "lottery"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "prize"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "you have won"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "claim your"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "kyc"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "account will be blocked"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "loan approved"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "urgent action"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "crypto")
            )
        ),
        PatternPack(
            id = "telecom-promos",
            name = "Telecom / carrier promos",
            description = "Recharge, plan and data-balance offers from operators.",
            seed = false,
            rules = listOf(
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "plan expires"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "recharge"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "data balance"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "limited period offer"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "cashback"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "unsubscribe")
            )
        ),
        PatternPack(
            id = "suspicious-links",
            name = "Suspicious link shorteners",
            description = "Shortened links commonly used to hide scam destinations.",
            seed = false,
            rules = listOf(
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "bit.ly"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "tinyurl.com"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "cutt.ly"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "rb.gy"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "t.co/")
            )
        )
    )

    fun presetToRule(preset: PresetRule): Rule =
        Rule(
            id = newRuleId(),
            action = preset.action,
            target = preset.target,
            mode = preset.mode,
            value = preset.value,
            enabled = true
        )

    fun packToRules(pack: PatternPack): List<Rule> =
        pack.rules.map { presetToRule(it) }

    fun seedRules(): List<Rule> =
        ALL.filter { it.seed }.flatMap { packToRules(it) }
}
