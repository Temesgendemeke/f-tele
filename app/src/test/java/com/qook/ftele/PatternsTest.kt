package com.qook.ftele

import com.qook.ftele.engine.PatternPacks
import com.qook.ftele.engine.Patterns
import com.qook.ftele.model.MatchMode
import com.qook.ftele.model.Rule
import com.qook.ftele.model.RuleAction
import com.qook.ftele.model.RuleTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternsTest {

    private fun rule(
        value: String,
        action: RuleAction = RuleAction.BLOCK,
        target: RuleTarget = RuleTarget.BODY,
        mode: MatchMode = MatchMode.CONTAINS,
        senderFilter: String? = null,
        category: String? = null,
        enabled: Boolean = true,
        id: String = "r"
    ) = Rule(
        id = id,
        action = action,
        target = target,
        mode = mode,
        value = value,
        senderFilter = senderFilter,
        category = category,
        enabled = enabled
    )

    @Test
    fun testNormalization() {
        assertEquals("freegift", Patterns.loose("Ｆｒｅｅ  ＧＩＦＴ"))
        assertEquals("loan", Patterns.loose("l\u200Boan"))
        assertEquals("vmcbe", Patterns.loose("VM-CBE"))
    }

    @Test
    fun testContains() {
        assertTrue(Patterns.matchOne("verification code", MatchMode.CONTAINS, "Your VERIFICATION-CODE is 1234"))
        assertTrue(Patterns.matchOne("loan", MatchMode.CONTAINS, "car loan offer"))
        assertFalse(Patterns.matchOne("loan", MatchMode.CONTAINS, "no match here"))
    }

    @Test
    fun testExactPrefixSuffix() {
        assertTrue(Patterns.matchOne("CBE", MatchMode.EXACT, "cbe"))
        assertFalse(Patterns.matchOne("CBE", MatchMode.EXACT, "CBE Bank"))
        assertTrue(Patterns.matchOne("VM-", MatchMode.PREFIX, "VM-HDFCBK"))
        assertFalse(Patterns.matchOne("HD", MatchMode.PREFIX, "VM-HDFCBK"))
        assertTrue(Patterns.matchOne("bank", MatchMode.SUFFIX, "Your Bank"))
    }

    @Test
    fun testWildcard() {
        assertTrue(Patterns.matchOne("*lottery*", MatchMode.WILDCARD, "win lottery today"))
        assertTrue(Patterns.matchOne("win * cash", MatchMode.WILDCARD, "win extra cash"))
        assertFalse(Patterns.matchOne("win * cash", MatchMode.WILDCARD, "win nothing"))
    }

    @Test
    fun testRegex() {
        assertTrue(Patterns.matchOne("\\b\\d{4,6}\\b", MatchMode.REGEX, "Code is 12345"))
        assertFalse(Patterns.matchOne("^[0-9]+$", MatchMode.REGEX, "abc"))
    }

    @Test
    fun testAllowTakesPriorityOverBlock() {
        val rules = listOf(
            rule("loan", RuleAction.BLOCK),
            rule("pre-approved loan", RuleAction.ALLOW)
        )
        val decision = Patterns.decide(rules, "blocklist", "Bank", "Your pre-approved loan is ready")
        assertFalse("Allow rule must win", decision.mute)
    }

    @Test
    fun testSenderFilterOnRule() {
        val rules = listOf(
            rule("promo", senderFilter = "Telebirr", action = RuleAction.BLOCK)
        )
        assertTrue(Patterns.decide(rules, "blocklist", "telebirr", "New promo offer").mute)
        assertFalse(Patterns.decide(rules, "blocklist", "OtherSender", "New promo offer").mute)
    }

    @Test
    fun testFinancialShieldNeverMutesOtp() {
        val rules = PatternPacks.CATEGORIES.first { it.id == "cat_protection" }.rules.map {
            PatternPacks.presetToRule(it)
        }
        val otpDecision = Patterns.decide(
            rules,
            "blocklist",
            "127",
            "<#> 9098 is your verification code. NEVER share your M-PESA Safaricom verification code."
        )
        assertFalse("OTP must never be muted", otpDecision.mute)
    }

    @Test
    fun testFinancialShieldProtectsBankTransactions() {
        val rules = PatternPacks.CATEGORIES.first { it.id == "cat_protection" }.rules.map {
            PatternPacks.presetToRule(it)
        }
        val cbeDecision = Patterns.decide(
            rules,
            "blocklist",
            "CBE",
            "Dear Temesgen your Account 1*********7169 has been Credited with ETB 2,000.00 from Meron Demeke"
        )
        assertFalse("Bank transaction must not be muted", cbeDecision.mute)
    }

    @Test
    fun testUserPreferencesDisabledByDefault() {
        val prefCategories = PatternPacks.CATEGORIES.filter {
            it.type == com.qook.ftele.model.CategoryType.USER_PREFERENCE
        }
        assertTrue(prefCategories.isNotEmpty())
        for (cat in prefCategories) {
            assertFalse("${cat.id} must be disabled by default", cat.defaultEnabled)
        }
    }
}
