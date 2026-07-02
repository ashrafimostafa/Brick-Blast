package com.mostafa.brickblast.domain.repository

import com.mostafa.brickblast.domain.model.Achievement
import com.mostafa.brickblast.domain.model.AppSettings
import com.mostafa.brickblast.domain.model.GameMode
import com.mostafa.brickblast.domain.model.GameSaveState
import com.mostafa.brickblast.domain.model.PlayerStatistics
import com.mostafa.brickblast.domain.model.PlayerUpgrades
import com.mostafa.brickblast.domain.model.ChallengeProgress
import com.mostafa.brickblast.domain.model.UpgradeType
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun updateSettings(transform: (AppSettings) -> AppSettings)
}

interface PlayerRepository {
    val coins: Flow<Long>
    val upgrades: Flow<PlayerUpgrades>
    val statistics: Flow<PlayerStatistics>
    val achievements: Flow<List<Achievement>>

    suspend fun getCoins(): Long
    suspend fun addCoins(amount: Long)
    suspend fun spendCoins(amount: Long): Boolean
    suspend fun getUpgrades(): PlayerUpgrades
    suspend fun upgrade(type: UpgradeType): Boolean
    suspend fun getStatistics(): PlayerStatistics
    suspend fun updateStatistics(transform: (PlayerStatistics) -> PlayerStatistics)
    suspend fun checkAchievements(
        bricksDestroyed: Long = 0,
        round: Int = 0,
        coins: Long = 0,
        balls: Int = 0
    ): List<Achievement>
}

interface GameSaveRepository {
    suspend fun saveGame(state: GameSaveState)
    suspend fun loadGame(mode: GameMode): GameSaveState?
    suspend fun clearSave(mode: GameMode)
    suspend fun getMostRecentSave(): GameSaveState?
    fun hasActiveSave(): Flow<Boolean>
}

interface HighScoreRepository {
    suspend fun saveHighScore(score: Int, round: Int, mode: String)
    suspend fun getBestScore(): Int
    fun getTopScores(limit: Int = 10): Flow<List<Triple<Int, Int, String>>>
}

interface ChallengeRepository {
    val progress: Flow<ChallengeProgress>
    suspend fun getProgress(): ChallengeProgress
    suspend fun completeLevel(level: Int)
}
