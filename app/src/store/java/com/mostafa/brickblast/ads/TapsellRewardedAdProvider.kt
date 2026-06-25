package com.mostafa.brickblast.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import com.mostafa.brickblast.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusInitListener
import ir.tapsell.plus.model.AdNetworkError
import ir.tapsell.plus.model.AdNetworks
import ir.tapsell.plus.model.TapsellPlusAdModel
import ir.tapsell.plus.model.TapsellPlusErrorModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Store-flavor rewarded ads via Tapsell Plus.
 * Set TAPSELL_APP_KEY and TAPSELL_REWARDED_ZONE_ID in local.properties.
 */
@Singleton
class TapsellRewardedAdProvider @Inject constructor(
    @ApplicationContext private val appContext: Context
) : RewardedAdProvider {

    private var responseId: String? = null
    private var initialized = false

    private var lastActivity: Activity? = null

    override fun initialize(application: Application) {
        val appKey = BuildConfig.TAPSELL_APP_KEY
        if (appKey.isBlank() || initialized) return
        TapsellPlus.initialize(application, appKey, object : TapsellPlusInitListener {
            override fun onInitializeSuccess(adNetworks: AdNetworks) {
                initialized = true
            }

            override fun onInitializeFailed(adNetworks: AdNetworks, adNetworkError: AdNetworkError) {
                initialized = false
            }
        })
    }

    override fun preloadRewardedAd(activity: Activity) {
        lastActivity = activity
        val zoneId = BuildConfig.TAPSELL_REWARDED_ZONE_ID
        if (zoneId.isBlank()) return
        TapsellPlus.requestRewardedVideoAd(activity, zoneId, object : AdRequestCallback() {
            override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                responseId = tapsellPlusAdModel.responseId
            }

            override fun error(message: String) {
                responseId = null
            }
        })
    }

    override fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onClosed: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        val id = responseId
        if (id.isNullOrBlank()) {
            onFailed("Ad is not ready yet")
            lastActivity?.let { preloadRewardedAd(it) }
            return
        }
        var rewarded = false
        TapsellPlus.showRewardedVideoAd(activity, id, object : AdShowListener() {
            override fun onRewarded(tapsellPlusAdModel: TapsellPlusAdModel) {
                rewarded = true
                onRewarded()
            }

            override fun onClosed(tapsellPlusAdModel: TapsellPlusAdModel) {
                responseId = null
                lastActivity?.let { preloadRewardedAd(it) }
                if (!rewarded) onClosed()
            }

            override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel) {
                responseId = null
                lastActivity?.let { preloadRewardedAd(it) }
                onFailed(tapsellPlusErrorModel.errorMessage ?: "Failed to show ad")
            }
        })
    }

    override val isRewardedAdReady: Boolean
        get() = !responseId.isNullOrBlank()
}
