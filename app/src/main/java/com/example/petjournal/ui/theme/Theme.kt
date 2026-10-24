package com.example.petjournal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenDark,
    onPrimary = OnPrimaryGreenDark,
    primaryContainer = PrimaryContainerGreenDark,
    onPrimaryContainer = OnPrimaryContainerGreenDark,
    secondary = SecondaryTealDark,
    onSecondary = OnSecondaryTealDark,
    secondaryContainer = SecondaryContainerTealDark,
    onSecondaryContainer = OnSecondaryContainerTealDark,
    tertiary = TertiaryBrownDark,
    onTertiary = OnTertiaryBrownDark,
    tertiaryContainer = TertiaryContainerBrownDark,
    onTertiaryContainer = OnTertiaryContainerBrownDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimaryGreen,
    primaryContainer = PrimaryContainerGreen,
    onPrimaryContainer = OnPrimaryContainerGreen,
    secondary = SecondaryTeal,
    onSecondary = OnSecondaryTeal,
    secondaryContainer = SecondaryContainerTeal,
    onSecondaryContainer = OnSecondaryContainerTeal,
    tertiary = TertiaryBrown,
    onTertiary = OnTertiaryBrown,
    tertiaryContainer = TertiaryContainerBrown,
    onTertiaryContainer = OnTertiaryContainerBrown,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

private val PetJournalShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun PetJournalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = PetJournalShapes,
        content = content
    )
}
