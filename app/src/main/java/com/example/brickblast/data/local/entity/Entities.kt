package com.example.brickblast.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class HighScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val score: Int,
    val round: Int,
    val mode: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val target: Long,
    val progress: Long = 0,
    val unlocked: Boolean = false,
    val unlockedAt: Long? = null
)

@Entity(tableName = "player_stats")
data class PlayerStatsEntity(
    @PrimaryKey val id: Int = 1,
    val highestRound: Int = 0,
    val totalBricksDestroyed: Long = 0,
    val totalBallsLaunched: Long = 0,
    val totalPlayTimeMs: Long = 0,
    val totalCoinsEarned: Long = 0,
    val totalGamesPlayed: Int = 0
)

@Entity(tableName = "player_upgrades")
data class PlayerUpgradesEntity(
    @PrimaryKey val id: Int = 1,
    val ballDamageLevel: Int = 1,
    val startingBallsLevel: Int = 1,
    val coinMultiplierLevel: Int = 1,
    val criticalHitLevel: Int = 1
)

@Entity(tableName = "player_wallet")
data class PlayerWalletEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Long = 0
)

@Entity(tableName = "game_save")
data class GameSaveEntity(
    @PrimaryKey val id: Int = 1,
    val round: Int = 1,
    val score: Int = 0,
    val totalBalls: Int = 1,
    val coinsThisSession: Int = 0,
    val mode: String = "CLASSIC",
    val bricksJson: String = "",
    val collectablesJson: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val hasActiveSave: Boolean = false
)
