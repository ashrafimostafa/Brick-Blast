package com.example.brickblast.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        HighScoreEntity::class,
        AchievementEntity::class,
        PlayerStatsEntity::class,
        PlayerUpgradesEntity::class,
        PlayerWalletEntity::class,
        GameSaveEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BrickBlastDatabase : RoomDatabase() {
    abstract fun highScoreDao(): HighScoreDao
    abstract fun achievementDao(): AchievementDao
    abstract fun playerStatsDao(): PlayerStatsDao
    abstract fun playerUpgradesDao(): PlayerUpgradesDao
    abstract fun playerWalletDao(): PlayerWalletDao
    abstract fun gameSaveDao(): GameSaveDao
}
