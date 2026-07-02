package com.mostafa.brickblast

import android.app.Application
import com.mostafa.brickblast.ads.RewardedAdProvider
import com.mostafa.brickblast.domain.repository.SettingsRepository
import com.mostafa.brickblast.ui.util.LocaleManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class BrickBlastApplication : Application() {

    @Inject lateinit var rewardedAdProvider: RewardedAdProvider
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        applyStoredLanguage()
        rewardedAdProvider.initialize(this)
    }

    private fun applyStoredLanguage() {
        val tag = runBlocking { settingsRepository.settings.first().languageTag }
        LocaleManager.apply(tag)
    }
}
