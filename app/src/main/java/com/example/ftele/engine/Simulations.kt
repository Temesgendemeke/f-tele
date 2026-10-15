package com.example.ftele.engine

data class SampleSms(val sender: String, val text: String)

object Simulations {
    val SAMPLES = listOf(
        SampleSms(
            sender = "Telebirr",
            text = "Weekend Flash Sale! 100% bonus airtime on every recharge today. Dial *127# to participate."
        ),
        SampleSms(
            sender = "CBE",
            text = "Your account 1000***456 has been credited with 3,500.00 ETB. Reference: CBE9482."
        ),
        SampleSms(
            sender = "LuckyDraw",
            text = "Congratulations! You won 50,000 ETB in today's lottery. Reply CLAIM to receive your prize."
        ),
        SampleSms(
            sender = "Google",
            text = "G-839201 is your Google verification code. Never share this code with anyone."
        )
    )
}
