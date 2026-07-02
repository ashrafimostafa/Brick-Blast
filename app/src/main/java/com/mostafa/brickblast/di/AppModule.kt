package com.mostafa.brickblast.di

import android.content.Context
import androidx.room.Room
import com.mostafa.brickblast.data.local.BrickBlastDatabase
import com.mostafa.brickblast.data.local.dao.AchievementDao
import com.mostafa.brickblast.data.local.dao.GameSaveDao
import com.mostafa.brickblast.data.local.dao.HighScoreDao
import com.mostafa.brickblast.data.local.dao.PlayerStatsDao
import com.mostafa.brickblast.data.local.dao.PlayerUpgradesDao
import com.mostafa.brickblast.data.local.dao.PlayerWalletDao
import com.mostafa.brickblast.data.repository.ChallengeRepositoryImpl
import com.mostafa.brickblast.data.repository.GameSaveRepositoryImpl
import com.mostafa.brickblast.data.repository.HighScoreRepositoryImpl
import com.mostafa.brickblast.data.repository.PlayerRepositoryImpl
import com.mostafa.brickblast.data.repository.SettingsRepositoryImpl
import com.mostafa.brickblast.domain.repository.ChallengeRepository
import com.mostafa.brickblast.domain.repository.GameSaveRepository
import com.mostafa.brickblast.domain.repository.HighScoreRepository
import com.mostafa.brickblast.domain.repository.PlayerRepository
import com.mostafa.brickblast.domain.repository.SettingsRepository
import com.mostafa.brickblast.game.engine.GameEngine
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppContextModule {
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BrickBlastDatabase =
        Room.databaseBuilder(context, BrickBlastDatabase::class.java, "brick_blast.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideHighScoreDao(db: BrickBlastDatabase): HighScoreDao = db.highScoreDao()
    @Provides fun provideAchievementDao(db: BrickBlastDatabase): AchievementDao = db.achievementDao()
    @Provides fun providePlayerStatsDao(db: BrickBlastDatabase): PlayerStatsDao = db.playerStatsDao()
    @Provides fun providePlayerUpgradesDao(db: BrickBlastDatabase): PlayerUpgradesDao = db.playerUpgradesDao()
    @Provides fun providePlayerWalletDao(db: BrickBlastDatabase): PlayerWalletDao = db.playerWalletDao()
    @Provides fun provideGameSaveDao(db: BrickBlastDatabase): GameSaveDao = db.gameSaveDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository

    @Binds @Singleton
    abstract fun bindGameSaveRepository(impl: GameSaveRepositoryImpl): GameSaveRepository

    @Binds @Singleton
    abstract fun bindHighScoreRepository(impl: HighScoreRepositoryImpl): HighScoreRepository

    @Binds @Singleton
    abstract fun bindChallengeRepository(impl: ChallengeRepositoryImpl): ChallengeRepository
}

@Module
@InstallIn(SingletonComponent::class)
object GameModule {

    @Provides
    @Singleton
    fun provideGameEngine(@ApplicationContext context: Context): GameEngine = GameEngine(context)
}
