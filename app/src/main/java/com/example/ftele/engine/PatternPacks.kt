package com.example.ftele.engine

import com.example.ftele.model.BehaviorCategory
import com.example.ftele.model.MatchMode
import com.example.ftele.model.PatternPack
import com.example.ftele.model.PresetRule
import com.example.ftele.model.Rule
import com.example.ftele.model.RuleAction
import com.example.ftele.model.RuleTarget
import java.util.UUID

object PatternPacks {

    fun newRuleId(): String = UUID.randomUUID().toString()

    /**
     * Smart Behavioral Categories tailored for carrier and banking SMS behaviors.
     * Captures both English and Amharic patterns extracted from real device logs.
     */
    val CATEGORIES: List<BehaviorCategory> = listOf(
        BehaviorCategory(
            id = "cat_protection",
            title = "Financial & OTP Shield",
            description = "Guaranteed Safe. Never mutes OTPs, verification codes, bank transactions (CBE, Awash, BOA, Dashen, telebirr, MPESA, Zemen, Hibret, etc.), airtime recharges, or missed calls.",
            icon = "🛡️",
            isProtection = true,
            defaultEnabled = true,
            rules = listOf(
                // OTP & Security
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "verification code", category = "OTP & Security"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "is your code", category = "OTP & Security"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "one-time passcode", category = "OTP & Security"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.WILDCARD, "code * do not share", category = "OTP & Security"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "ምስጢር ቁጥር", category = "OTP & Security"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "የማረጋገጫ", category = "OTP & Security"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "የይለፍ ቃል", category = "OTP & Security"),

                // Banking & Money Transfers (All Ethiopian Banks & Mobile Money)
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "has been credited", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "credited with", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "credited by", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "is credited", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "has been debited", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "debited with", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "debited by", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "was debited", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "current balance", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "available balance", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "avail. bal", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "balance now is", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "transferred etb", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "transfer completed", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "you have received etb", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "cash withdrawal", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "atm cash", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "transaction number is", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "txn id", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "receipt:", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "airtime from", category = "Airtime"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "recharged successfully", category = "Airtime"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "balance is insufficient", category = "Account Alerts"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "debt is fully covered", category = "Account Alerts"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "ተቀብለዋል", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "ተቀንሷል", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "ቀሪ ሒሳብ", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "ቀሪ ሂሳብ", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "ገቢ ተደርጓል", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "ገቢ ሆኗል", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "ወጪ ሆኗል", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "አስተላልፈዋል", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "hafteen herreega", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "lakkoofsa nagahee", category = "Bank & Transactions"),
                PresetRule(RuleAction.ALLOW, RuleTarget.SENDER, MatchMode.EXACT, "CBE", category = "CBE Official"),

                // Missed call notifications (MCN)
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "called you", category = "Missed Calls"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "please call back", category = "Missed Calls"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "mcn:", category = "Missed Calls"),
                PresetRule(RuleAction.ALLOW, RuleTarget.BODY, MatchMode.CONTAINS, "beep call request from", category = "Missed Calls")
            )
        ),
        BehaviorCategory(
            id = "cat_lottery",
            title = "Lottery & Lucky Draws",
            description = "Mutes lottery promotions, raffle draws, EV car sweepstakes, weekly draw tickets, silver coins, and teleplay games.",
            icon = "🎰",
            isProtection = false,
            defaultEnabled = true,
            rules = listOf(
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ሎተሪ", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "አድማስ", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ቴሌፕለይ", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "teleplay", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ዕጣ", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ይሸለሙ", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ያሸንፉ", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ተሸላሚ", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ዕድለኛ", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "silver coin", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "draw ticket", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "mega promo", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "jackpot", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "lottery", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "prize", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "you have won", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ዓለም ዋንጫ", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "byd መኪና", category = "Lottery & Lucky Draws"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ev car", category = "Lottery & Lucky Draws")
            )
        ),
        BehaviorCategory(
            id = "cat_telecom_promos",
            title = "Telecom Marketing & Offers",
            description = "Mutes aggressive data package pushes, SuperApp promo incentives, and corporate festival marketing.",
            icon = "📢",
            isProtection = false,
            defaultEnabled = true,
            rules = listOf(
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ነፃ 1 ጂቢ", category = "Telecom Marketing & Offers"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "አስደሳች ቅናሾችን", category = "Telecom Marketing & Offers"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ቅናሽ ያግኙ", category = "Telecom Marketing & Offers"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ኦዳ አዋርድ", category = "Telecom Marketing & Offers"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ታላቁ ሩጫ", category = "Telecom Marketing & Offers"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "onelink.to", category = "Telecom Marketing & Offers"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "bit.ly/telebirr", category = "Telecom Marketing & Offers"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ኪድጆ ቲቪ", category = "Telecom Marketing & Offers"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "kidjo tv", category = "Telecom Marketing & Offers"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "limited period offer", category = "Telecom Marketing & Offers"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.WILDCARD, "*አደረሰዎ*ኢትዮ ቴሌኮም*", category = "Telecom Marketing & Offers")
            )
        ),
        BehaviorCategory(
            id = "cat_vas",
            title = "VAS & Ringtone Subscriptions",
            description = "Mutes annoying Caller Ring Back Tone (CallSign/CRBT) subscriptions, daily horoscope, and value-added services.",
            icon = "🎵",
            isProtection = false,
            defaultEnabled = true,
            rules = listOf(
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ጥሪ ፊርማ", category = "VAS & Ringtone"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "callsign", category = "VAS & Ringtone"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "crbt", category = "VAS & Ringtone"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "*623*", category = "VAS & Ringtone"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "*9013*", category = "VAS & Ringtone"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ለመመዝገብ *", category = "VAS & Ringtone")
            )
        ),
        BehaviorCategory(
            id = "cat_loans",
            title = "Loan & Credit Advertisements",
            description = "Mutes unsolicited bank loan pushes, instant micro-credit ads, and loan offers.",
            icon = "💳",
            isProtection = false,
            defaultEnabled = true,
            rules = listOf(
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ያለማስያዣ ይበደሩ", category = "Loan & Credit Ads"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "የጎደለ መሙያ ብድር", category = "Loan & Credit Ads"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ፈጣን ብድር", category = "Loan & Credit Ads"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "apollo", category = "Loan & Credit Ads"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "100,000 ብር ብድር", category = "Loan & Credit Ads"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "loan approved", category = "Loan & Credit Ads")
            )
        ),
        BehaviorCategory(
            id = "cat_pure_spam_senders",
            title = "Pure Spam Shortcodes (Whole Senders)",
            description = "Mutes entire senders that send 100% advertising junk and never send personal or critical messages.",
            icon = "🔕",
            isProtection = false,
            defaultEnabled = true,
            rules = listOf(
                PresetRule(RuleAction.BLOCK, RuleTarget.SENDER, MatchMode.EXACT, "605", category = "Pure Spam Sender"),
                PresetRule(RuleAction.BLOCK, RuleTarget.SENDER, MatchMode.EXACT, "131", category = "Pure Spam Sender"),
                PresetRule(RuleAction.BLOCK, RuleTarget.SENDER, MatchMode.EXACT, "830", category = "Pure Spam Sender"),
                PresetRule(RuleAction.BLOCK, RuleTarget.SENDER, MatchMode.EXACT, "CallSign", category = "Pure Spam Sender"),
                PresetRule(RuleAction.BLOCK, RuleTarget.SENDER, MatchMode.EXACT, "EoC", category = "Pure Spam Sender")
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
            senderFilter = preset.senderFilter,
            category = preset.category,
            enabled = true
        )

    fun categoryToRules(category: BehaviorCategory): List<Rule> =
        category.rules.map { presetToRule(it) }

    fun seedRules(): List<Rule> =
        CATEGORIES.filter { it.defaultEnabled }.flatMap { categoryToRules(it) }

    // Legacy packs compatibility
    val ALL: List<PatternPack> = CATEGORIES.map { cat ->
        PatternPack(
            id = cat.id,
            name = cat.title,
            description = cat.description,
            seed = cat.defaultEnabled,
            rules = cat.rules
        )
    }
}
