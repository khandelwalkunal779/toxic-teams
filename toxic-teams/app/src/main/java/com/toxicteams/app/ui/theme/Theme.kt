package com.toxicteams.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CorporateGreen,
    onPrimary = OpticalPureBlack,
    primaryContainer = CorporateGreenDark,
    onPrimaryContainer = TextHighEmphasis,

    secondary = ElectricLavender,
    onSecondary = OpticalPureWhite,
    secondaryContainer = ElectricLavenderDark,
    onSecondaryContainer = ElectricLavenderLight,

    tertiary = ElectricCyan,
    onTertiary = OpticalPureBlack,

    background = CharcoalBackground,
    onBackground = TextHighEmphasis,

    surface = CharcoalSurface,
    onSurface = TextHighEmphasis,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = TextMediumEmphasis,

    outline = SlateBorder,
    outlineVariant = SlateSurfaceContainerHigh,

    error = ErrorRed,
    onError = OpticalPureWhite
)

@Composable
fun ToxicTeamsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Toxic Teams is intentionally a dark-themed utility for optimal contrast against optical sensors
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = CharcoalBackground.toArgb()
                window.navigationBarColor = CharcoalBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

