package com.mostafa.brickblast.ads

import android.app.Activity
import android.app.Application
import javax.inject.Inject
import javax.inject.Singleton

/** FOSS / F-Droid build: no ads, no proprietary SDK. */
@Singleton
class NoOpRewardedAdProvider @Inject constructor() : RewardedAdProvider {
    override fun initialize(application: Application) = Unit
    override fun preloadRewardedAd(activity: Activity) = Unit
    override fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onClosed: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        onFailed("Ads are disabled in this build")
    }
    override val isRewardedAdReady: Boolean = false
}
