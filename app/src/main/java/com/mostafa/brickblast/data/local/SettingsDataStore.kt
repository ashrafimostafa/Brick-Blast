package com.mostafa.brickblast.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mostafa.brickblast.domain.model.AppSettings
import com.mostafa.brickblast.domain.model.ColorPackIds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "brick_blast_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    private val context: Context
) {
    private object Keys {
        val SOUND = booleanPreferencesKey("sound_enabled")
        val MUSIC = booleanPreferencesKey("music_enabled")
        val VIBRATION = booleanPreferencesKey("vibration_enabled")
        val TRAJECTORY = booleanPreferencesKey("show_trajectory")
        val PARTICLES = booleanPreferencesKey("particle_effects")
        val RICH_EXPLOSIONS = booleanPreferencesKey("rich_explosions")
        val ACHIEVEMENT_AUTO_DISMISS = booleanPreferencesKey("achievement_auto_dismiss")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val LANGUAGE_TAG = stringPreferencesKey("language_tag")
        val SELECTED_COLOR_PACK = stringPreferencesKey("selected_color_pack")
        val OWNED_COLOR_PACKS = stringSetPreferencesKey("owned_color_packs")
    }

    private fun fromPrefs(prefs: Preferences) = AppSettings(
        soundEnabled = prefs[Keys.SOUND] ?: true,
        musicEnabled = prefs[Keys.MUSIC] ?: true,
        vibrationEnabled = prefs[Keys.VIBRATION] ?: true,
        showTrajectory = prefs[Keys.TRAJECTORY] ?: true,
        particleEffects = prefs[Keys.PARTICLES] ?: true,
        richExplosions = prefs[Keys.RICH_EXPLOSIONS] ?: true,
        achievementAutoDismiss = prefs[Keys.ACHIEVEMENT_AUTO_DISMISS] ?: true,
        darkTheme = prefs[Keys.DARK_THEME] ?: true,
        languageTag = prefs[Keys.LANGUAGE_TAG],
        selectedColorPackId = prefs[Keys.SELECTED_COLOR_PACK] ?: ColorPackIds.CLASSIC,
        ownedColorPackIds = prefs[Keys.OWNED_COLOR_PACKS] ?: setOf(ColorPackIds.CLASSIC)
    )

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs -> fromPrefs(prefs) }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { prefs ->
            val updated = transform(fromPrefs(prefs))
            prefs[Keys.SOUND] = updated.soundEnabled
            prefs[Keys.MUSIC] = updated.musicEnabled
            prefs[Keys.VIBRATION] = updated.vibrationEnabled
            prefs[Keys.TRAJECTORY] = updated.showTrajectory
            prefs[Keys.PARTICLES] = updated.particleEffects
            prefs[Keys.RICH_EXPLOSIONS] = updated.richExplosions
            prefs[Keys.ACHIEVEMENT_AUTO_DISMISS] = updated.achievementAutoDismiss
            prefs[Keys.DARK_THEME] = updated.darkTheme
            if (updated.languageTag != null) {
                prefs[Keys.LANGUAGE_TAG] = updated.languageTag
            } else {
                prefs.remove(Keys.LANGUAGE_TAG)
            }
            prefs[Keys.SELECTED_COLOR_PACK] = updated.selectedColorPackId
            prefs[Keys.OWNED_COLOR_PACKS] = updated.ownedColorPackIds
        }
    }
}
