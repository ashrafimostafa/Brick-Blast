package com.example.brickblast.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brickblast.domain.model.AppSettings
import com.example.brickblast.domain.model.PlayerStatistics
import com.example.brickblast.domain.model.PlayerUpgrades
import com.example.brickblast.domain.model.UpgradeType
import com.example.brickblast.domain.repository.GameSaveRepository
import com.example.brickblast.domain.repository.HighScoreRepository
import com.example.brickblast.domain.repository.PlayerRepository
import com.example.brickblast.domain.repository.SettingsRepository
import com.example.brickblast.game.audio.AudioManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    gameSaveRepository: GameSaveRepository,
    playerRepository: PlayerRepository
) : ViewModel() {
    val hasActiveSave = gameSaveRepository.hasActiveSave()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val coins = playerRepository.coins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val audioManager: AudioManager
) : ViewModel() {
    val settings = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(soundEnabled = enabled) }
            audioManager.setSoundEnabled(enabled)
        }
    }

    fun toggleMusic(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(musicEnabled = enabled) }
            audioManager.setMusicEnabled(enabled)
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(vibrationEnabled = enabled) }
        }
    }

    fun toggleTrajectory(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(showTrajectory = enabled) }
        }
    }

    fun toggleParticles(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(particleEffects = enabled) }
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(darkTheme = enabled) }
        }
    }
}

@HiltViewModel
class UpgradeViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {
    val upgrades = playerRepository.upgrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerUpgrades())
    val coins = playerRepository.coins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun upgrade(type: UpgradeType) {
        viewModelScope.launch { playerRepository.upgrade(type) }
    }
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    playerRepository: PlayerRepository,
    highScoreRepository: HighScoreRepository
) : ViewModel() {
    val statistics = playerRepository.statistics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerStatistics())
    val achievements = playerRepository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val topScores = highScoreRepository.getTopScores(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {
    val coins = playerRepository.coins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val upgrades = playerRepository.upgrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerUpgrades())

    fun purchaseUpgrade(type: UpgradeType) {
        viewModelScope.launch { playerRepository.upgrade(type) }
    }
}
