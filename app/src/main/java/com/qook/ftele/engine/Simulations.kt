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
            text = "Dear Customer You have received ETB 250.00 from ABC SERVICES SHARE COMPANY - short code 100000 on 15/01/2026 14:30:00. Your transaction number is TX99A12B34. Thank you for using telebirr",
            expectedDecision = "Allowed (Transaction)"
        ),
        SampleSms(
            sender = "127",
            text = "በቴሌብር ስላደረጉት የገንዘብ ዝውውር እናመሰግናለን። ወደ *830*1# በመደወል በቴሌፕለይ ጨዋታዎች እለታዊ 25,000 ብር፣ ሳምንታዊ 100,000 ብር እና ዘመናዊ የኤሌክትሪክ መኪና እንዲሁም ሌሎች አጓጊ ሽልማቶችን ያሸንፉ!",
            expectedDecision = "Muted (Lottery/Teleplay)"
        ),
        SampleSms(
            sender = "Safaricom",
            text = "<#> 1234 is your verification code. NEVER share your M-PESA Safaricom verification code. abCD12efGH3",
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
            text = "Dear Customer your Account 1*********0000 has been Credited with ETB 1,500.00 from Abebe Kebede, on 15/01/2026 with Ref No FT1234567890. Your Current Balance is ETB 4,200.00.",
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
            text = "your account 0123*****00 has been debited with ETB 500.00. Your current balance is ETB 1,230.50. Receipt: https://awashpay.com/tx/A12345",
            expectedDecision = "Allowed (Awash Bank Debit)"
        ),
        SampleSms(
            sender = "BOA",
            text = "your account 1*****00 was debited with ETB 1,200.00. Available Balance: ETB 8,500.25. Receipt: https://boa.et/r?trx=B12345",
            expectedDecision = "Allowed (BOA Bank Debit)"
        ),
        SampleSms(
            sender = "Dashen",
            text = "your account '0000*****0000' is credited with ETB 3,500.00 on 14/01/2026. current balance is ETB 12,400.00",
            expectedDecision = "Allowed (Dashen Bank Credit)"
        )
    )
}
