package com.mostafa.brickblast.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mostafa.brickblast.domain.model.ChallengeConfig
import com.mostafa.brickblast.domain.model.ChallengeProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.challengeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "brick_blast_challenges"
)

@Singleton
class ChallengeProgressStore @Inject constructor(
    private val context: Context
) {
    private object Keys {
        val HIGHEST_UNLOCKED = intPreferencesKey("highest_unlocked")
        val COMPLETED = stringPreferencesKey("completed_levels")
    }

    val progress: Flow<ChallengeProgress> = context.challengeDataStore.data.map { prefs ->
        val highest = prefs[Keys.HIGHEST_UNLOCKED] ?: 1
        val completed = prefs[Keys.COMPLETED]
            ?.split(',')
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
        ChallengeProgress(
            highestUnlocked = highest.coerceIn(1, ChallengeConfig.TOTAL_LEVELS),
            completedLevels = completed
        )
    }

    suspend fun getProgress(): ChallengeProgress = progress.first()

    suspend fun completeLevel(level: Int) {
        if (level !in 1..ChallengeConfig.TOTAL_LEVELS) return
        context.challengeDataStore.edit { prefs ->
            val currentHighest = prefs[Keys.HIGHEST_UNLOCKED] ?: 1
            val completed = prefs[Keys.COMPLETED]
                ?.split(',')
                ?.mapNotNull { it.toIntOrNull() }
                ?.toMutableSet()
                ?: mutableSetOf()
            completed.add(level)
            prefs[Keys.COMPLETED] = completed.sorted().joinToString(",")
            val nextUnlock = minOf(level + 1, ChallengeConfig.TOTAL_LEVELS)
            prefs[Keys.HIGHEST_UNLOCKED] = maxOf(currentHighest, nextUnlock)
        }
    }
}
