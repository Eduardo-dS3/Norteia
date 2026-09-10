package io.github.eduardods3.norteia.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography.
 *
 * Ships with the platform sans-serif so the APK stays tiny and the first build
 * "just works". To give the app more personality, drop a variable font (e.g.
 * Inter or Space Grotesk, both SIL OFL) into `res/font/` and point
 * [brandFamily] at it — nothing else needs to change.
 */
private val brandFamily: FontFamily = FontFamily.Default

/** Tall, tight number style for the central heading. Used ad-hoc, not in the set. */
val HeadingDisplay = TextStyle(
    fontFamily = brandFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 76.sp,
    lineHeight = 76.sp,
    letterSpacing = (-2).sp,
)

val NorteiaTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = brandFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = brandFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = brandFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = brandFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = brandFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = brandFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp,
    ),
)
