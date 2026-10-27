package com.qook.ftele.engine

data class SampleSms(
    val sender: String,
    val text: String,
    val expectedDecision: String
)

object Simulations {
    val SAMPLES = listOf(
        SampleSms(
            sender = "127",
            text = "Dear Temesgen You have received ETB 243.75 from CHAPA FINANCIAL TECHNOLOGY SHARE COMPANY - short code 500423 on 02/12/2025 22:05:23. Your transaction number is CF16M70KJI. Thank you for using telebirr",
            expectedDecision = "Allowed (Transaction)"
        ),
        SampleSms(
            sender = "127",
            text = "በቴሌብር ስላደረጉት የገንዘብ ዝውውር እናመሰግናለን። ወደ *830*1# በመደወል በቴሌፕለይ ጨዋታዎች እለታዊ 25,000 ብር፣ ሳምንታዊ 100,000 ብር እና ዘመናዊ የኤሌክትሪክ መኪና እንዲሁም ሌሎች አጓጊ ሽልማቶችን ያሸንፉ!",
            expectedDecision = "Muted (Lottery/Teleplay)"
        ),
        SampleSms(
            sender = "Safaricom",
            text = "<#> 9098 is your verification code. NEVER share your M-PESA Safaricom verification code. oeJMZxkqPJq",
            expectedDecision = "Allowed (OTP)"
        ),
        SampleSms(
            sender = "Safaricom",
            text = "ነፃ 1 ጂቢ+ 35 ደቂቃ ለእርስዎ፣ *777# ላይ በመደወል በሽን በመምረጥ አስደሳች ቅናሾችን ያግኙ",
            expectedDecision = "Muted (Telecom Promo)"
        ),
        SampleSms(
            sender = "605",
            text = "44ኛው ዙር አድማስ ዲጂታል ሎተሪ ሊወጣ ጥቂት ቀናት ቀሩት! 4 ሚሊየን ብር እና ሌሎችም ሽልማቶች ዕድለኛ ለመሆን ወደ 605 A ብለው ይላኩ!",
            expectedDecision = "Muted (Pure Spam Sender)"
        ),
        SampleSms(
            sender = "CBE",
            text = "Dear Temesgen your Account 1*********7169 has been Credited with ETB 2,000.00 from Meron Demeke, on 12/12/2025 with Ref No FT253464TYJV. Your Current Balance is ETB 3,450.00.",
            expectedDecision = "Allowed (Bank Credit)"
        ),
        SampleSms(
            sender = "EthioTelecom",
            text = "Dear Customer, you have successfully recharged ETB 100.00 airtime. Your new balance is ETB 145.20.",
            expectedDecision = "Allowed (Airtime Recharge)"
        ),
        SampleSms(
            sender = "EthioTelecom",
            text = "ቀሪ ሂሳብዎ 145.20 ብር ነው። የአየር ሰዓትዎ የሚያበቃበት ቀን 30/11/2026 ነው።",
            expectedDecision = "Allowed (Balance Inquiry)"
        ),
        SampleSms(
            sender = "apollo",
            text = "በአፖሎ እስከ 100,000 ብር ያለማስያዣ ይበደሩ! አሁኑኑ የአፖሎ ሂሳብ በመክፈትና ሂሳብዎን በማንቀሳቀስ፣ የጎደለ መሙያ ብድር ተጠቃሚ ይሁኑ። https://onelink.to/apollo",
            expectedDecision = "Muted (Loan Promo)"
        ),
        SampleSms(
            sender = "Awash",
            text = "your account 0132*****98 has been debited with ETB 500.00. Your current balance is ETB 1,230.50. Receipt: https://awashpay.com/tx/A78932",
            expectedDecision = "Allowed (Awash Bank Debit)"
        ),
        SampleSms(
            sender = "BOA",
            text = "your account 5*****42 was debited with ETB 1,200.00. Available Balance: ETB 8,500.25. Receipt: https://boa.et/r?trx=B99214",
            expectedDecision = "Allowed (BOA Bank Debit)"
        ),
        SampleSms(
            sender = "Dashen",
            text = "your account '0021*****1201' is credited with ETB 3,500.00 on 14/10/2026. current balance is ETB 12,400.00",
            expectedDecision = "Allowed (Dashen Bank Credit)"
        )
    )
}
