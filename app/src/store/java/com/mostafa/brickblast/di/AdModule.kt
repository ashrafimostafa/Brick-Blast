package com.mostafa.brickblast.di

import com.mostafa.brickblast.ads.RewardedAdProvider
import com.mostafa.brickblast.ads.TapsellRewardedAdProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdModule {
    @Binds
    @Singleton
    abstract fun bindRewardedAdProvider(impl: TapsellRewardedAdProvider): RewardedAdProvider
}
