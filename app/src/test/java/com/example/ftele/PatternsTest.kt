package com.example.ftele

import com.example.ftele.data.RuleStore
import com.example.ftele.engine.Patterns
import com.example.ftele.model.MatchMode
import com.example.ftele.model.Rule
import com.example.ftele.model.RuleAction
import com.example.ftele.model.RuleTarget
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
        enabled: Boolean = true,
        id: String = "r"
    ) = Rule(
        id = id,
        action = action,
        target = target,
        mode = mode,
        value = value,
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
        assertTrue(Patterns.matchOne("loan*approved", MatchMode.WILDCARD, "your LOAN is approved"))
        assertTrue(Patterns.matchOne("l?an", MatchMode.WILDCARD, "loan"))
        assertFalse(Patterns.matchOne("loan*approved", MatchMode.WILDCARD, "loan pending"))
        assertFalse(Patterns.matchOne("a.b", MatchMode.WILDCARD, "axb"))
    }

    @Test
    fun testRegexAndInvalidRegex() {
        assertTrue(Patterns.matchOne("\\d{4}", MatchMode.REGEX, "otp 4821 please"))
        assertTrue(Patterns.matchOne("^\\+234", MatchMode.REGEX, "+2348030000000"))
        assertFalse(Patterns.matchOne("(unclosed", MatchMode.REGEX, "anything"))
    }

    @Test
    fun testTargets() {
        val sender = rule(value = "scam", target = RuleTarget.SENDER)
        assertTrue(Patterns.decide(listOf(sender), "blocklist", "scam", "hello").mute)

        val body = rule(value = "scam", target = RuleTarget.BODY)
        assertFalse(Patterns.decide(listOf(body), "blocklist", "friend", "hello").mute)

        val both = rule(value = "scam", target = RuleTarget.BOTH)
        assertTrue(Patterns.decide(listOf(both), "blocklist", "friend", "a scam").mute)
    }

    @Test
    fun testAllowWinsOverBlock() {
        val rules = listOf(
            rule(id = "a", action = RuleAction.BLOCK, value = "otp"),
            rule(id = "b", action = RuleAction.ALLOW, value = "otp")
        )
        val out = Patterns.decide(rules, "blocklist", "Bank", "your otp is 1234")
        assertFalse(out.mute)
        assertTrue(out.reason.contains("Allowed"))
    }

    @Test
    fun testFirstMatchingBlockReported() {
        val rules = listOf(
            rule(id = "a", value = "lottery"),
            rule(id = "b", value = "prize")
        )
        val out = Patterns.decide(rules, "blocklist", "x", "lottery prize")
        assertTrue(out.mute)
        assertEquals("Blocked: lottery", out.reason)
    }

    @Test
    fun testDisabledRulesDoNotMatch() {
        val rules = listOf(rule(value = "spam", enabled = false))
        assertFalse(Patterns.decide(rules, "blocklist", "x", "spam").mute)
    }

    @Test
    fun testAllowlistMode() {
        val rules = listOf(rule(action = RuleAction.ALLOW, target = RuleTarget.SENDER, value = "cbe"))
        assertFalse(Patterns.decide(rules, "allowlist", "CBE", "hi").mute)
        assertTrue(Patterns.decide(rules, "allowlist", "Other", "hi").mute)
    }

    @Test
    fun testLegacyRuleParsing() {
        val json = """[{"id":"1","type":"keyword","value":"otp","action":"allow"},{"id":"2","type":"sender","value":"CBE","action":"block"}]"""
        val parsed = RuleStore.parseRules(json)
        assertEquals(2, parsed.size)
        assertEquals(RuleTarget.BODY, parsed[0].target)
        assertEquals(MatchMode.CONTAINS, parsed[0].mode)
        assertEquals(RuleAction.ALLOW, parsed[0].action)

        assertEquals(RuleTarget.SENDER, parsed[1].target)
        assertEquals(RuleAction.BLOCK, parsed[1].action)
    }
}
