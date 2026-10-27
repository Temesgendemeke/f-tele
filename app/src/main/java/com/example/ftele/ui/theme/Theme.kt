package com.example.ftele.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class FTeleColors(
    val bg: Color,
    val card: Color,
    val cardSoft: Color,
    val ink: Color,
    val inkSoft: Color,
    val sub: Color,
    val line: Color,
    val lineSoft: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentText: Color,
    val allow: Color,
    val allowSoft: Color,
    val block: Color,
    val blockSoft: Color,
    val warning: Color,
    val warningSoft: Color
)

val LightFTeleColors = FTeleColors(
    bg = BgLight,
    card = CardLight,
    cardSoft = CardSoftLight,
    ink = InkLight,
    inkSoft = InkSoftLight,
    sub = SubLight,
    line = LineLight,
    lineSoft = LineSoftLight,
    accent = AccentLight,
    accentSoft = AccentSoftLight,
    accentText = Color.White,
    allow = AllowLight,
    allowSoft = AllowSoftLight,
    block = BlockLight,
    blockSoft = BlockSoftLight,
    warning = WarningLight,
    warningSoft = WarningSoftLight
)

val DarkFTeleColors = FTeleColors(
    bg = BgDark,
    card = CardDark,
    cardSoft = CardSoftDark,
    ink = InkDark,
    inkSoft = InkSoftDark,
    sub = SubDark,
    line = LineDark,
    lineSoft = LineSoftDark,
    accent = AccentDark,
    accentSoft = AccentSoftDark,
    accentText = Color.White,
    allow = AllowDark,
    allowSoft = AllowSoftDark,
    block = BlockDark,
    blockSoft = BlockSoftDark,
    warning = WarningDark,
    warningSoft = WarningSoftDark
)

val LocalFTeleColors = staticCompositionLocalOf { LightFTeleColors }

private val LightColorScheme = lightColorScheme(
    primary = AccentLight,
    background = BgLight,
    surface = CardLight,
    onBackground = InkLight,
    onSurface = InkLight
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentDark,
    background = BgDark,
    surface = CardDark,
    onBackground = InkDark,
    onSurface = InkDark
)

@Composable
fun FTeleTheme(
    themeSetting: String = "system", // "system", "light", "dark"
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeSetting) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val customColors = if (darkTheme) DarkFTeleColors else LightFTeleColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = customColors.bg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalFTeleColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

object FTeleTheme {
    val colors: FTeleColors
        @Composable
        get() = LocalFTeleColors.current
}
