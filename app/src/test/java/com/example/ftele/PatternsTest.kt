package com.example.ftele

import com.example.ftele.data.RuleStore
import com.example.ftele.engine.PatternPacks
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
    fun testSenderFilterScoping() {
        // Only block 'teleplay' if sender is 127
        val scopedRule = rule(
            value = "teleplay",
            action = RuleAction.BLOCK,
            target = RuleTarget.BODY,
            senderFilter = "127"
        )

        // Matching sender + message -> Muted
        val out1 = Patterns.decide(listOf(scopedRule), "blocklist", "127", "win with teleplay car")
        assertTrue(out1.mute)

        // Different sender with same message -> Allowed
        val out2 = Patterns.decide(listOf(scopedRule), "blocklist", "Friend", "win with teleplay car")
        assertFalse(out2.mute)
    }

    @Test
    fun testAmharicMatching() {
        val lotteryRule = rule(value = "ሎተሪ", action = RuleAction.BLOCK, target = RuleTarget.BODY)
        val outLottery = Patterns.decide(listOf(lotteryRule), "blocklist", "605", "44ኛው ዙር አድማስ ዲጂታል ሎተሪ")
        assertTrue(outLottery.mute)

        val otpRule = rule(value = "ምስጢር ቁጥር", action = RuleAction.ALLOW, target = RuleTarget.BODY)
        val outOtp = Patterns.decide(listOf(otpRule, lotteryRule), "blocklist", "Safaricom", "የM-PESA ምስጢር ቁጥርዎን አያጋሩ")
        assertFalse(outOtp.mute)
        assertTrue(outOtp.reason.contains("Allowed"))
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

    @Test
    fun testMultiBankProtectionShield() {
        val seedRules = PatternPacks.seedRules()

        // Awash Bank
        val awash = Patterns.decide(
            seedRules, "blocklist", "Awash",
            "your account 0132*****98 has been debited with ETB 500.00. Your current balance is ETB 1,230.50. Receipt: https://awashpay.com/tx/A78932"
        )
        assertFalse("Awash transaction must be allowed", awash.mute)

        // Bank of Abyssinia (BOA)
        val boa = Patterns.decide(
            seedRules, "blocklist", "BOA",
            "your account 5*****42 was debited with ETB 1,200.00. Available Balance: ETB 8,500.25. Receipt: https://boa.et/r?trx=B99214"
        )
        assertFalse("BOA transaction must be allowed", boa.mute)

        // Dashen Bank
        val dashen = Patterns.decide(
            seedRules, "blocklist", "Dashen",
            "your account '0021*****1201' is credited with ETB 3,500.00 on 14/10/2026. current balance is ETB 12,400.00"
        )
        assertFalse("Dashen transaction must be allowed", dashen.mute)

        // Zemen Bank
        val zemen = Patterns.decide(
            seedRules, "blocklist", "Zemen",
            "Birr 4,000.00 ATM cash withdrawal has been made from A/c No. 120*****91 on 12-OCT-2026. Available Bal. is Birr 15,200.00"
        )
        assertFalse("Zemen transaction must be allowed", zemen.mute)

        // telebirr receipt
        val telebirr = Patterns.decide(
            seedRules, "blocklist", "127",
            "Dear Temesgen You have received ETB 243.75 from CHAPA FINANCIAL TECHNOLOGY SHARE COMPANY on 02/12/2025. Your transaction number is CF16M70KJI. current balance is ETB 1,500.00"
        )
        assertFalse("telebirr transaction must be allowed", telebirr.mute)
    }

    @Test
    fun testCarrierBehavioralMutingShields() {
        val seedRules = PatternPacks.seedRules()

        // Teleplay lottery promotion from 127 (telecom sender) -> MUST BE MUTED
        val teleplay = Patterns.decide(
            seedRules, "blocklist", "127",
            "በቴሌብር ስላደረጉት የገንዘብ ዝውውር እናመሰግናለን። ወደ *830*1# በመደወል በቴሌፕለይ ጨዋታዎች እለታዊ 25,000 ብር እና መኪና ያሸንፉ!"
        )
        assertTrue("Teleplay lottery promo must be muted", teleplay.mute)

        // Pure spam sender 605 -> MUST BE MUTED
        val admas = Patterns.decide(
            seedRules, "blocklist", "605",
            "44ኛው ዙር አድማስ ዲጂታል ሎተሪ ሊወጣ ጥቂት ቀናት ቀሩት! ወደ 605 ይላኩ!"
        )
        assertTrue("Shortcode 605 must be muted", admas.mute)

        // Telecom marketing from Safaricom -> MUST BE MUTED
        val promo = Patterns.decide(
            seedRules, "blocklist", "Safaricom",
            "ነፃ 1 ጂቢ+ 35 ደቂቃ ለእርስዎ፣ *777# ላይ በመደወል አስደሳች ቅናሾችን ያግኙ"
        )
        assertTrue("Telecom bundle promo must be muted", promo.mute)

        // Ringtone VAS from CallSign -> MUST BE MUTED
        val ringtone = Patterns.decide(
            seedRules, "blocklist", "CallSign",
            "ይህ የጥሪ ፊርማ አገልግሎት ነው። ለዕለታዊ ጥቅል ለመመዝገብ *623*1*1# ይጫኑ"
        )
        assertTrue("Ringtone promo must be muted", ringtone.mute)
    }
}
