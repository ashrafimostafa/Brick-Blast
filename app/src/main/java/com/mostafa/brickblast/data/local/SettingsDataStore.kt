package com.mostafa.brickblast.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.mostafa.brickblast.domain.model.AppSettings
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
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    private fun fromPrefs(prefs: Preferences) = AppSettings(
        soundEnabled = prefs[Keys.SOUND] ?: true,
        musicEnabled = prefs[Keys.MUSIC] ?: true,
        vibrationEnabled = prefs[Keys.VIBRATION] ?: true,
        showTrajectory = prefs[Keys.TRAJECTORY] ?: true,
        particleEffects = prefs[Keys.PARTICLES] ?: true,
        darkTheme = prefs[Keys.DARK_THEME] ?: true
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
            prefs[Keys.DARK_THEME] = updated.darkTheme
        }
    }
}
