package com.qook.ftele.engine

import com.qook.ftele.model.BehaviorCategory
import com.qook.ftele.model.CategoryType
import com.qook.ftele.model.MatchMode
import com.qook.ftele.model.PresetRule
import com.qook.ftele.model.Rule
import com.qook.ftele.model.RuleAction
import com.qook.ftele.model.RuleTarget
import java.util.UUID

object PatternPacks {

    fun newRuleId(): String = UUID.randomUUID().toString()

    /**
     * Smart Behavioral Categories tailored for carrier, banking, and utility SMS.
     * Categorized into:
     * 1. CORE_PROTECTION: Critical OTPs & Bank transactions (always protected)
     * 2. SPAM_SHIELD: Marketing, Lottery, VAS, Loans, Spam shortcodes (muted by default)
     * 3. USER_PREFERENCE: Airtime recharges, balance inquiries, transfer receipts (OFF by default)
     */
    val CATEGORIES: List<BehaviorCategory> = listOf(
        // --- 1. CORE FINANCIAL & OTP PROTECTION ---
        BehaviorCategory(
            id = "cat_protection",
            title = "Financial & OTP Shield",
            description = "Guaranteed Safe. Never mutes OTPs, verification codes, bank transactions (CBE, Awash, BOA, Dashen, telebirr, MPESA, Zemen, Hibret, etc.), or missed call alerts.",
            icon = "shield",
            type = CategoryType.CORE_PROTECTION,
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

        // --- 2. SPAM SHIELDS (Muted by default) ---
        BehaviorCategory(
            id = "cat_lottery",
            title = "Lottery & Lucky Draws",
            description = "Mutes lottery promotions, raffle draws, EV car sweepstakes, weekly draw tickets, silver coins, and teleplay games.",
            icon = "lottery",
            type = CategoryType.SPAM_SHIELD,
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
            icon = "promos",
            type = CategoryType.SPAM_SHIELD,
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
            icon = "vas",
            type = CategoryType.SPAM_SHIELD,
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
            icon = "loans",
            type = CategoryType.SPAM_SHIELD,
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
            icon = "spam",
            type = CategoryType.SPAM_SHIELD,
            rules = listOf(
                PresetRule(RuleAction.BLOCK, RuleTarget.SENDER, MatchMode.EXACT, "605", category = "Pure Spam Sender"),
                PresetRule(RuleAction.BLOCK, RuleTarget.SENDER, MatchMode.EXACT, "131", category = "Pure Spam Sender"),
                PresetRule(RuleAction.BLOCK, RuleTarget.SENDER, MatchMode.EXACT, "830", category = "Pure Spam Sender"),
                PresetRule(RuleAction.BLOCK, RuleTarget.SENDER, MatchMode.EXACT, "CallSign", category = "Pure Spam Sender"),
                PresetRule(RuleAction.BLOCK, RuleTarget.SENDER, MatchMode.EXACT, "EoC", category = "Pure Spam Sender")
            )
        ),

        // --- 3. USER PREFERENCES (Utility Notifications - OFF BY DEFAULT) ---
        // Not spam, but informational utility notifications that users can optionally silence
        BehaviorCategory(
            id = "cat_pref_recharge",
            title = "Airtime & Recharge Confirmations",
            description = "Optional preference (NOT spam). Mutes confirmation alerts when you recharge airtime or successfully purchase voice/data packages. Default: Allowed.",
            icon = "recharge",
            type = CategoryType.USER_PREFERENCE,
            rules = listOf(
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "recharged successfully", category = "Airtime Recharge"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "recharge successful", category = "Airtime Recharge"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "airtime from", category = "Airtime Recharge"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "package purchase", category = "Package Purchase"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "package is successfully", category = "Package Purchase"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ጥቅል በተሳካ ሁኔታ ገዝተዋል", category = "Package Purchase"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "አየር ሰዓት ሞልተዋል", category = "Airtime Recharge"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "በተሳካ ሁኔታ ሞልተዋል", category = "Airtime Recharge"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "guutameera", category = "Airtime Recharge"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "bitaa milkaayeera", category = "Package Purchase")
            )
        ),
        BehaviorCategory(
            id = "cat_pref_balance",
            title = "Balance & Package Inquiries",
            description = "Optional preference (NOT spam). Mutes responses when querying your account balance or remaining data/minutes (*804#, *705#, etc.). Default: Allowed.",
            icon = "balance",
            type = CategoryType.USER_PREFERENCE,
            rules = listOf(
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "your account balance is", category = "Balance Query"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "remaining balance", category = "Balance Query"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "remaining data", category = "Package Query"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ቀሪ ሂሳብዎ", category = "Balance Query"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ቀሪ ሒሳብዎ", category = "Balance Query"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ቀሪ የጥቅል", category = "Package Query"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "ቀሪ ጥቅል", category = "Package Query"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "hafteen herreegaa", category = "Balance Query"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "hafteen paakeejii", category = "Package Query")
            )
        ),
        BehaviorCategory(
            id = "cat_pref_transfers",
            title = "Credit Transfer & Sharing Receipts",
            description = "Optional preference (NOT spam). Mutes confirmation messages when sharing airtime or sending balance to another person. Default: Allowed.",
            icon = "transfers",
            type = CategoryType.USER_PREFERENCE,
            rules = listOf(
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "transferred airtime", category = "Airtime Transfer"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "airtime share", category = "Airtime Transfer"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "አየር ሰዓት አስተላልፈዋል", category = "Airtime Transfer"),
                PresetRule(RuleAction.BLOCK, RuleTarget.BODY, MatchMode.CONTAINS, "አየር ሰዓት ተላልፎልዎታል", category = "Airtime Transfer")
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
}
