package com.mostafa.brickblast.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.mostafa.brickblast.R

/** Vazirmatn (Vazir) — OFL-licensed Persian/Arabic typeface. */
val VazirFontFamily = FontFamily(
    Font(R.font.vazir_regular, FontWeight.Normal),
    Font(R.font.vazir_medium, FontWeight.Medium),
    Font(R.font.vazir_bold, FontWeight.Bold)
)

private fun TextStyle.withFontFamily(fontFamily: FontFamily) = copy(fontFamily = fontFamily)

private fun Typography.withFontFamily(fontFamily: FontFamily): Typography = copy(
    displayLarge = displayLarge.withFontFamily(fontFamily),
    displayMedium = displayMedium.withFontFamily(fontFamily),
    displaySmall = displaySmall.withFontFamily(fontFamily),
    headlineLarge = headlineLarge.withFontFamily(fontFamily),
    headlineMedium = headlineMedium.withFontFamily(fontFamily),
    headlineSmall = headlineSmall.withFontFamily(fontFamily),
    titleLarge = titleLarge.withFontFamily(fontFamily),
    titleMedium = titleMedium.withFontFamily(fontFamily),
    titleSmall = titleSmall.withFontFamily(fontFamily),
    bodyLarge = bodyLarge.withFontFamily(fontFamily),
    bodyMedium = bodyMedium.withFontFamily(fontFamily),
    bodySmall = bodySmall.withFontFamily(fontFamily),
    labelLarge = labelLarge.withFontFamily(fontFamily),
    labelMedium = labelMedium.withFontFamily(fontFamily),
    labelSmall = labelSmall.withFontFamily(fontFamily)
)

val DefaultTypography = Typography()

val PersianTypography = DefaultTypography.withFontFamily(VazirFontFamily)
