package com.mostafa.brickblast.ui.util

import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/** True when the active UI locale is Persian (fa). */
fun isPersianLocale(languageTag: String?): Boolean = when (languageTag) {
    "fa" -> true
    "en" -> false
    else -> {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        if (!appLocales.isEmpty) {
            appLocales[0]?.language == "fa"
        } else {
            Locale.getDefault().language == "fa"
        }
    }
}
