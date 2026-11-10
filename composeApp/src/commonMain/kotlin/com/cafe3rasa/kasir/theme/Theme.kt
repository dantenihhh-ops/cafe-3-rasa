package com.cafe3rasa.kasir.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KasirColorScheme = lightColorScheme(
    primary = KasirColors.PrimaryTeal,
    onPrimary = KasirColors.TextWhite,
    secondary = KasirColors.PrimaryDarkNavy,
    onSecondary = KasirColors.TextWhite,
    tertiary = KasirColors.AccentRust,
    onTertiary = KasirColors.TextWhite,
    background = KasirColors.Background,
    onBackground = KasirColors.TextPrimary,
    surface = KasirColors.Surface,
    onSurface = KasirColors.TextPrimary,
    outline = KasirColors.BorderLight,
    surfaceVariant = KasirColors.Background,
    onSurfaceVariant = KasirColors.TextSecondary
)

@Composable
fun KasirCafeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KasirColorScheme,
        typography = KasirTypography,
        content = content
    )
}
