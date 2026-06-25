package com.mostafa.brickblast.ads

import android.app.Activity
import android.app.Application

/**
 * Abstraction for rewarded ads. F-Droid builds use a no-op implementation;
 * the store flavor wires Tapsell rewarded video.
 */
interface RewardedAdProvider {
    fun initialize(application: Application)
    fun preloadRewardedAd(activity: Activity)
    fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onClosed: () -> Unit,
        onFailed: (String) -> Unit
    )
    val isRewardedAdReady: Boolean
}
