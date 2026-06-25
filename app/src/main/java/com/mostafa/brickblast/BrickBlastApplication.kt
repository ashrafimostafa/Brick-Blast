package com.mostafa.brickblast

import android.app.Application
import com.mostafa.brickblast.ads.RewardedAdProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BrickBlastApplication : Application() {

    @Inject lateinit var rewardedAdProvider: RewardedAdProvider

    override fun onCreate() {
        super.onCreate()
        rewardedAdProvider.initialize(this)
    }
}
