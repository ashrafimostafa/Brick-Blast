package com.example.brickblast.data.repository

import com.example.brickblast.data.local.SettingsDataStore
import com.example.brickblast.data.local.dao.AchievementDao
import com.example.brickblast.data.local.dao.GameSaveDao
import com.example.brickblast.data.local.dao.HighScoreDao
import com.example.brickblast.data.local.dao.PlayerStatsDao
import com.example.brickblast.data.local.dao.PlayerUpgradesDao
import com.example.brickblast.data.local.dao.PlayerWalletDao
import com.example.brickblast.data.local.entity.AchievementEntity
import com.example.brickblast.data.local.entity.GameSaveEntity
import com.example.brickblast.data.local.entity.HighScoreEntity
import com.example.brickblast.data.local.entity.PlayerStatsEntity
import com.example.brickblast.data.local.entity.PlayerUpgradesEntity
import com.example.brickblast.data.local.entity.PlayerWalletEntity
import com.example.brickblast.domain.model.Achievement
import com.example.brickblast.domain.model.AchievementDefinitions
import com.example.brickblast.domain.model.AppSettings
import com.example.brickblast.domain.model.GameMode
import com.example.brickblast.domain.model.GameSaveState
import com.example.brickblast.domain.model.PlayerStatistics
import com.example.brickblast.domain.model.PlayerUpgrades
import com.example.brickblast.domain.model.UpgradeType
import com.example.brickblast.domain.repository.GameSaveRepository
import com.example.brickblast.domain.repository.HighScoreRepository
import com.example.brickblast.domain.repository.PlayerRepository
import com.example.brickblast.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : SettingsRepository {
    override val settings: Flow<AppSettings> = dataStore.settings
    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) =
        dataStore.update(transform)
}

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val walletDao: PlayerWalletDao,
    private val upgradesDao: PlayerUpgradesDao,
    private val statsDao: PlayerStatsDao,
    private val achievementDao: AchievementDao
) : PlayerRepository {

    init {
        // Achievements seeded on first access via getAchievements flow
    }

    override val coins: Flow<Long> = walletDao.observeCoins().map { it ?: 0L }

    override val upgrades: Flow<PlayerUpgrades> = upgradesDao.observe().map { entity ->
        entity?.toDomain() ?: PlayerUpgrades()
    }

    override val statistics: Flow<PlayerStatistics> = statsDao.observe().map { entity ->
        entity?.toDomain() ?: PlayerStatistics()
    }

    override val achievements: Flow<List<Achievement>> = achievementDao.getAll().map { list ->
        if (list.isEmpty()) AchievementDefinitions.ALL
        else list.map { it.toDomain() }
    }

    override suspend fun getCoins(): Long {
        ensureWallet()
        return walletDao.getCoins() ?: 0L
    }

    override suspend fun addCoins(amount: Long) {
        ensureWallet()
        val current = walletDao.getCoins() ?: 0L
        walletDao.upsert(PlayerWalletEntity(coins = current + amount))
    }

    override suspend fun spendCoins(amount: Long): Boolean {
        ensureWallet()
        val current = walletDao.getCoins() ?: 0L
        if (current < amount) return false
        walletDao.upsert(PlayerWalletEntity(coins = current - amount))
        return true
    }

    override suspend fun getUpgrades(): PlayerUpgrades {
        ensureUpgrades()
        return upgradesDao.get()?.toDomain() ?: PlayerUpgrades()
    }

    override suspend fun upgrade(type: UpgradeType): Boolean {
        val current = getUpgrades()
        val cost = current.upgradeCost(type)
        if (!spendCoins(cost.toLong())) return false
        val updated = when (type) {
            UpgradeType.BALL_DAMAGE -> current.copy(ballDamageLevel = current.ballDamageLevel + 1)
            UpgradeType.STARTING_BALLS -> current.copy(startingBallsLevel = current.startingBallsLevel + 1)
            UpgradeType.COIN_MULTIPLIER -> current.copy(coinMultiplierLevel = current.coinMultiplierLevel + 1)
            UpgradeType.CRITICAL_HIT -> current.copy(criticalHitLevel = current.criticalHitLevel + 1)
        }
        upgradesDao.upsert(updated.toEntity())
        return true
    }

    override suspend fun getStatistics(): PlayerStatistics {
        ensureStats()
        return statsDao.get()?.toDomain() ?: PlayerStatistics()
    }

    override suspend fun updateStatistics(transform: (PlayerStatistics) -> PlayerStatistics) {
        ensureStats()
        val current = statsDao.get()?.toDomain() ?: PlayerStatistics()
        statsDao.upsert(transform(current).toEntity())
    }

    override suspend fun checkAchievements(
        bricksDestroyed: Long,
        round: Int,
        coins: Long,
        balls: Int
    ): List<Achievement> {
        ensureAchievements()
        val entities = achievementDao.getAll()
        val newlyUnlocked = mutableListOf<Achievement>()
        // Note: we read synchronously via a one-shot; in production use first()
        // For simplicity, update each achievement type
        val all = achievementDao.getAll().first()
        for (entity in all) {
            val newProgress = when (entity.id) {
                "destroy_100_bricks" -> maxOf(entity.progress, bricksDestroyed)
                "reach_round_50" -> maxOf(entity.progress, round.toLong())
                "collect_500_coins" -> maxOf(entity.progress, coins)
                "own_50_balls" -> maxOf(entity.progress, balls.toLong())
                else -> entity.progress
            }
            val unlocked = entity.unlocked || newProgress >= entity.target
            if (unlocked && !entity.unlocked) {
                newlyUnlocked.add(entity.copy(progress = newProgress, unlocked = true, unlockedAt = System.currentTimeMillis()).toDomain())
            }
            achievementDao.updateProgress(
                entity.id,
                newProgress,
                unlocked,
                if (unlocked) entity.unlockedAt ?: System.currentTimeMillis() else null
            )
        }
        return newlyUnlocked
    }

    private suspend fun ensureWallet() {
        if (walletDao.getCoins() == null) {
            walletDao.upsert(PlayerWalletEntity())
        }
    }

    private suspend fun ensureUpgrades() {
        if (upgradesDao.get() == null) {
            upgradesDao.upsert(PlayerUpgradesEntity())
        }
    }

    private suspend fun ensureStats() {
        if (statsDao.get() == null) {
            statsDao.upsert(PlayerStatsEntity())
        }
    }

    private suspend fun ensureAchievements() {
        val all = achievementDao.getAll().first()
        if (all.isEmpty()) {
            achievementDao.insertAll(
                AchievementDefinitions.ALL.map { it.toEntity() }
            )
        }
    }

    private fun PlayerUpgradesEntity.toDomain() = PlayerUpgrades(
        ballDamageLevel, startingBallsLevel, coinMultiplierLevel, criticalHitLevel
    )

    private fun PlayerUpgrades.toEntity() = PlayerUpgradesEntity(
        ballDamageLevel = ballDamageLevel,
        startingBallsLevel = startingBallsLevel,
        coinMultiplierLevel = coinMultiplierLevel,
        criticalHitLevel = criticalHitLevel
    )

    private fun PlayerStatsEntity.toDomain() = PlayerStatistics(
        highestRound, totalBricksDestroyed, totalBallsLaunched, totalPlayTimeMs, totalCoinsEarned, totalGamesPlayed
    )

    private fun PlayerStatistics.toEntity() = PlayerStatsEntity(
        highestRound = highestRound,
        totalBricksDestroyed = totalBricksDestroyed,
        totalBallsLaunched = totalBallsLaunched,
        totalPlayTimeMs = totalPlayTimeMs,
        totalCoinsEarned = totalCoinsEarned,
        totalGamesPlayed = totalGamesPlayed
    )

    private fun AchievementEntity.toDomain() = Achievement(id, title, description, target, progress, unlocked, unlockedAt)
    private fun Achievement.toEntity() = AchievementEntity(id, title, description, target, progress, unlocked, unlockedAt)
}

@Singleton
class GameSaveRepositoryImpl @Inject constructor(
    private val gameSaveDao: GameSaveDao
) : GameSaveRepository {

    override suspend fun saveGame(state: GameSaveState) {
        gameSaveDao.upsert(
            GameSaveEntity(
                round = state.round,
                score = state.score,
                totalBalls = state.totalBalls,
                coinsThisSession = state.coinsThisSession,
                mode = state.mode.name,
                bricksJson = state.bricksJson,
                collectablesJson = state.collectablesJson,
                timestamp = state.timestamp,
                hasActiveSave = true
            )
        )
    }

    override suspend fun loadGame(): GameSaveState? {
        val entity = gameSaveDao.get() ?: return null
        if (!entity.hasActiveSave) return null
        return GameSaveState(
            round = entity.round,
            score = entity.score,
            totalBalls = entity.totalBalls,
            coinsThisSession = entity.coinsThisSession,
            mode = GameMode.valueOf(entity.mode),
            bricksJson = entity.bricksJson,
            collectablesJson = entity.collectablesJson,
            timestamp = entity.timestamp
        )
    }

    override suspend fun clearSave() = gameSaveDao.clear()

    override fun hasActiveSave(): Flow<Boolean> =
        gameSaveDao.observe().map { it?.hasActiveSave == true }
}

@Singleton
class HighScoreRepositoryImpl @Inject constructor(
    private val highScoreDao: HighScoreDao
) : HighScoreRepository {

    override suspend fun saveHighScore(score: Int, round: Int, mode: String) {
        highScoreDao.insert(HighScoreEntity(score = score, round = round, mode = mode))
    }

    override suspend fun getBestScore(): Int = highScoreDao.getBestScore()

    override fun getTopScores(limit: Int): Flow<List<Triple<Int, Int, String>>> =
        highScoreDao.getTopScores(limit).map { list ->
            list.map { Triple(it.score, it.round, it.mode) }
        }
}
