package com.qook.ftele.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class FTeleColors(
    val bg: Color,
    val card: Color,
    val cardSoft: Color,
    val line: Color,
    val lineSoft: Color,
    val ink: Color,
    val inkSoft: Color,
    val sub: Color,
    val accent: Color,
    val accentSoft: Color,
    val block: Color,
    val blockSoft: Color,
    val allow: Color,
    val allowSoft: Color
)

private val DarkFTeleColors = FTeleColors(
    bg = FTelePalette.Obsidian,
    card = FTelePalette.Charcoal,
    cardSoft = FTelePalette.SlateDark,
    line = FTelePalette.LineDark,
    lineSoft = FTelePalette.LineDarkSoft,
    ink = FTelePalette.InkDark,
    inkSoft = FTelePalette.InkDarkSoft,
    sub = FTelePalette.SubDark,
    accent = FTelePalette.ShieldGreenDark,
    accentSoft = FTelePalette.ShieldGreenSoftDark,
    block = FTelePalette.StateRedDark,
    blockSoft = FTelePalette.StateRedSoftDark,
    allow = FTelePalette.ShieldGreenDark,
    allowSoft = FTelePalette.ShieldGreenSoftDark
)

private val LightFTeleColors = FTeleColors(
    bg = FTelePalette.Paper,
    card = FTelePalette.PureWhite,
    cardSoft = FTelePalette.Cloud,
    line = FTelePalette.LineLight,
    lineSoft = FTelePalette.LineLightSoft,
    ink = FTelePalette.InkLight,
    inkSoft = FTelePalette.InkLightSoft,
    sub = FTelePalette.SubLight,
    accent = FTelePalette.ShieldGreenLight,
    accentSoft = FTelePalette.ShieldGreenSoftLight,
    block = FTelePalette.StateRedLight,
    blockSoft = FTelePalette.StateRedSoftLight,
    allow = FTelePalette.ShieldGreenLight,
    allowSoft = FTelePalette.ShieldGreenSoftLight
)

val LocalFTeleColors = staticCompositionLocalOf { DarkFTeleColors }

object FTeleTheme {
    val colors: FTeleColors
        @Composable
        get() = LocalFTeleColors.current
}

@Composable
fun FTeleTheme(
    themeSetting: String = "system",
    content: @Composable () -> Unit
) {
    val isDark = when (themeSetting) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val fteleColors = if (isDark) DarkFTeleColors else LightFTeleColors

    val materialColors = if (isDark) {
        darkColorScheme(
            primary = fteleColors.accent,
            surface = fteleColors.card,
            background = fteleColors.bg,
            onSurface = fteleColors.ink,
            onBackground = fteleColors.ink
        )
    } else {
        lightColorScheme(
            primary = fteleColors.accent,
            surface = fteleColors.card,
            background = fteleColors.bg,
            onSurface = fteleColors.ink,
            onBackground = fteleColors.ink
        )
    }

    CompositionLocalProvider(LocalFTeleColors provides fteleColors) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}
