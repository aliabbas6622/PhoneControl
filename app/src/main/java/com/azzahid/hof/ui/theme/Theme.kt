package com.azzahid.hof.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Cyan,
    onPrimary = Obsidian,
    primaryContainer = CyanSubtle,
    onPrimaryContainer = CyanBright,
    secondary = Violet,
    onSecondary = Obsidian,
    secondaryContainer = VioletSubtle,
    onSecondaryContainer = Violet,
    tertiary = Amber,
    onTertiary = Obsidian,
    tertiaryContainer = AmberSubtle,
    onTertiaryContainer = Amber,
    error = SoftRed,
    onError = Obsidian,
    errorContainer = SoftRedSubtle,
    onErrorContainer = SoftRed,
    background = Obsidian,
    onBackground = SoftWhite,
    surface = DarkGray,
    onSurface = SoftWhite,
    surfaceVariant = MidGray,
    onSurfaceVariant = SecondaryText,
    outline = OutlineGray,
    outlineVariant = SubtleGray,
    surfaceContainerLowest = Obsidian,
    surfaceContainerLow = DarkGray,
    surfaceContainer = MidGray,
    surfaceContainerHigh = SubtleGray,
    surfaceContainerHighest = CardGray
)

private val LightColorScheme = lightColorScheme(
    primary = CyanDark,
    onPrimary = PureWhite,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = CyanDark,
    secondary = VioletDark,
    onSecondary = PureWhite,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = VioletDark,
    tertiary = AmberDark,
    onTertiary = PureWhite,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = AmberDark,
    error = SoftRedDark,
    onError = PureWhite,
    errorContainer = LightErrorContainer,
    onErrorContainer = SoftRedDark,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutline,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun HTTPOnFireTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
