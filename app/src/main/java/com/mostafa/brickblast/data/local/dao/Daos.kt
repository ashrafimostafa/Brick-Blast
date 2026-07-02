package com.mostafa.brickblast.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mostafa.brickblast.data.local.entity.AchievementEntity
import com.mostafa.brickblast.data.local.entity.GameSaveEntity
import com.mostafa.brickblast.data.local.entity.HighScoreEntity
import com.mostafa.brickblast.data.local.entity.PlayerStatsEntity
import com.mostafa.brickblast.data.local.entity.PlayerUpgradesEntity
import com.mostafa.brickblast.data.local.entity.PlayerWalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT :limit")
    fun getTopScores(limit: Int = 10): Flow<List<HighScoreEntity>>

    @Query("SELECT COALESCE(MAX(score), 0) FROM high_scores")
    suspend fun getBestScore(): Int

    @Insert
    suspend fun insert(score: HighScoreEntity)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAll(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET progress = :progress, unlocked = :unlocked, unlockedAt = :unlockedAt WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Long, unlocked: Boolean, unlockedAt: Long?)
}

@Dao
interface PlayerStatsDao {
    @Query("SELECT * FROM player_stats WHERE id = 1")
    fun observe(): Flow<PlayerStatsEntity?>

    @Query("SELECT * FROM player_stats WHERE id = 1")
    suspend fun get(): PlayerStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: PlayerStatsEntity)
}

@Dao
interface PlayerUpgradesDao {
    @Query("SELECT * FROM player_upgrades WHERE id = 1")
    fun observe(): Flow<PlayerUpgradesEntity?>

    @Query("SELECT * FROM player_upgrades WHERE id = 1")
    suspend fun get(): PlayerUpgradesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(upgrades: PlayerUpgradesEntity)
}

@Dao
interface PlayerWalletDao {
    @Query("SELECT coins FROM player_wallet WHERE id = 1")
    fun observeCoins(): Flow<Long?>

    @Query("SELECT coins FROM player_wallet WHERE id = 1")
    suspend fun getCoins(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(wallet: PlayerWalletEntity)
}

@Dao
interface GameSaveDao {
    @Query("SELECT * FROM game_save WHERE saveKey = :saveKey")
    suspend fun get(saveKey: String): GameSaveEntity?

    @Query("SELECT * FROM game_save WHERE hasActiveSave = 1 ORDER BY timestamp DESC")
    suspend fun getMostRecent(): GameSaveEntity?

    @Query("SELECT * FROM game_save WHERE hasActiveSave = 1 ORDER BY timestamp DESC")
    fun observeActive(): Flow<List<GameSaveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(save: GameSaveEntity)

    @Query("DELETE FROM game_save WHERE saveKey = :saveKey")
    suspend fun clear(saveKey: String)
}
