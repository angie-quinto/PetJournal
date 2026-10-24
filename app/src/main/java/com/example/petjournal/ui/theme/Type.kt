package com.example.petjournal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DefaultTypography = Typography()

val Typography = DefaultTypography.copy(
    headlineLarge = DefaultTypography.headlineLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = DefaultTypography.headlineMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.25).sp
    ),
    titleLarge = DefaultTypography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = DefaultTypography.titleMedium.copy(
        fontWeight = FontWeight.Medium
    ),
    labelLarge = DefaultTypography.labelLarge.copy(
        fontWeight = FontWeight.SemiBold
    )
)
