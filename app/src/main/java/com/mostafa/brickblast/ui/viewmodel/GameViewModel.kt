package com.mostafa.brickblast.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostafa.brickblast.BuildConfig
import com.mostafa.brickblast.ads.RewardedAdProvider
import com.mostafa.brickblast.domain.model.Achievement
import com.mostafa.brickblast.domain.model.AppSettings
import com.mostafa.brickblast.domain.model.GameConfig
import com.mostafa.brickblast.domain.model.GameMode
import com.mostafa.brickblast.domain.model.GamePhase
import com.mostafa.brickblast.data.local.GameStateSerializer
import com.mostafa.brickblast.domain.model.GameSaveState
import com.mostafa.brickblast.domain.model.AchievementSnapshot
import com.mostafa.brickblast.domain.model.PlayerUpgrades
import com.mostafa.brickblast.domain.model.UpgradeType
import com.mostafa.brickblast.domain.repository.ChallengeRepository
import com.mostafa.brickblast.domain.repository.GameSaveRepository
import com.mostafa.brickblast.domain.repository.HighScoreRepository
import com.mostafa.brickblast.domain.repository.PlayerRepository
import com.mostafa.brickblast.domain.repository.SettingsRepository
import com.mostafa.brickblast.game.audio.AudioManager
import com.mostafa.brickblast.game.audio.SoundEffect
import com.mostafa.brickblast.game.engine.GameEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameUiState(
    val score: Int = 0,
    val round: Int = 1,
    val totalBalls: Int = 1,
    val coins: Int = 0,
    val phase: GamePhase = GamePhase.AIMING,
    val isAiming: Boolean = false,
    val timeRemaining: Float? = null,
    val showTrajectory: Boolean = true,
    val particleEffects: Boolean = true,
    val achievementAutoDismiss: Boolean = true,
    val newAchievements: List<Achievement> = emptyList(),
    val screenWidth: Float = 1080f,
    val screenHeight: Float = 1920f,
    val showContinueOffer: Boolean = false,
    val continueAdLoading: Boolean = false,
    val frame: Long = 0
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val engine: GameEngine,
    private val playerRepository: PlayerRepository,
    private val gameSaveRepository: GameSaveRepository,
    private val highScoreRepository: HighScoreRepository,
    private val settingsRepository: SettingsRepository,
    private val challengeRepository: ChallengeRepository,
    private val audioManager: AudioManager,
    private val rewardedAdProvider: RewardedAdProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val gameEngine: GameEngine get() = engine

    private var gameStarted = false
    @Volatile private var running = false
    val isRunning: Boolean get() = running
    private var gameEndHandled = false
    private var totalCoins = 0L
    private var lastBounceSoundMs = 0L
    private var lastHudUpdateNanos = 0L
    private var continueUsedThisGame = false

    fun startGame(mode: GameMode, challengeLevel: Int = 1, continueGame: Boolean = false) {
        val restarting = engine.phase == GamePhase.GAME_OVER || engine.phase == GamePhase.VICTORY
        if (gameStarted && engine.phase == GamePhase.PAUSED) {
            resume()
            return
        }
        if (gameStarted && !restarting) {
            return
        }

        gameStarted = true
        gameEndHandled = false
        continueUsedThisGame = false
        running = false
        // Clear end-state immediately so GameScreen does not bounce back to GameOver
        // before the async init finishes (reused ViewModel + singleton engine).
        _uiState.update {
            it.copy(
                phase = GamePhase.AIMING,
                showContinueOffer = false,
                continueAdLoading = false
            )
        }

        viewModelScope.launch {
            val upgrades = playerRepository.getUpgrades()
            totalCoins = playerRepository.getCoins()
            engine.bestScore = highScoreRepository.getBestScore()
            val settings = settingsRepository.settings.first()
            audioManager.setSoundEnabled(settings.soundEnabled)
            audioManager.setMusicEnabled(settings.musicEnabled)
            if (settings.musicEnabled) audioManager.startMusic()

            var save: GameSaveState? = null
            if (continueGame) {
                save = gameSaveRepository.loadGame(mode)?.takeIf { it.mode == mode }
            } else {
                gameSaveRepository.clearSave(mode)
            }

            val effectiveChallengeLevel = when {
                save != null && mode == GameMode.CHALLENGE -> save.challengeLevel
                else -> challengeLevel
            }

            val config = GameConfig(
                mode = mode,
                challengeLevel = effectiveChallengeLevel,
                timeLimitSeconds = if (mode == GameMode.TIME_ATTACK) 60 else 0
            )

            engine.onBrickDestroyed = {
                val now = System.currentTimeMillis()
                if (now - lastBounceSoundMs > 60L) {
                    audioManager.play(SoundEffect.DESTROY)
                    lastBounceSoundMs = now
                }
            }
            engine.onBallBounce = {
                val now = System.currentTimeMillis()
                if (now - lastBounceSoundMs > 180L) {
                    audioManager.play(SoundEffect.BOUNCE)
                    lastBounceSoundMs = now
                }
            }
            engine.onCollect = { audioManager.play(SoundEffect.COLLECT) }
            engine.onShoot = { audioManager.play(SoundEffect.SHOOT) }
            engine.onGameOver = { /* handled in tick() — may show continue-offer first */ }
            engine.onRoundComplete = { r ->
                viewModelScope.launch {
                    autoSave()
                    checkAchievements()
                }
            }
            engine.onPowerUpActivated = { audioManager.play(SoundEffect.POWER_UP) }

            val w = _uiState.value.screenWidth
            val h = _uiState.value.screenHeight
            engine.initGame(w, h, config, upgrades, save)
            engine.particleEffectsEnabled = settings.particleEffects

            _uiState.update {
                it.copy(
                    score = engine.score,
                    round = engine.round,
                    totalBalls = engine.totalBalls,
                    coins = engine.coinsThisSession,
                    phase = engine.phase,
                    showTrajectory = settings.showTrajectory,
                    particleEffects = settings.particleEffects,
                    achievementAutoDismiss = settings.achievementAutoDismiss,
                    timeRemaining = if (mode == GameMode.TIME_ATTACK) engine.timeAttackRemaining else null
                )
            }

            running = true
        }
    }

    fun preloadRewardedAd(activity: Activity) {
        if (BuildConfig.ADS_ENABLED) {
            rewardedAdProvider.preloadRewardedAd(activity)
        }
    }

    fun setScreenSize(width: Float, height: Float) {
        _uiState.update { it.copy(screenWidth = width, screenHeight = height) }
    }

    /**
     * Advances the simulation by one frame. Driven by the composable's
     * withFrameNanos loop so updates are synced to the display refresh rate,
     * which removes the stutter caused by a fixed delay() loop.
     */
    fun tick(deltaSeconds: Float) {
        if (!running) return

        engine.update(deltaSeconds)

        val current = _uiState.value
        val timeRemaining = if (engine.config.mode == GameMode.TIME_ATTACK) {
            engine.timeAttackRemaining
        } else null
        val timeChanged = timeRemaining != null && (
            current.timeRemaining == null ||
                timeRemaining.toInt() != current.timeRemaining!!.toInt()
        )
        val phaseChanged = engine.phase != current.phase
        val aimingChanged = engine.isAiming != current.isAiming
        val ballsChanged = engine.totalBalls != current.totalBalls
        val simulating = engine.phase == GamePhase.SIMULATING ||
            engine.phase == GamePhase.LAUNCHING ||
            engine.phase == GamePhase.RECALLING
        val now = System.nanoTime()
        val hudDue = !simulating || now - lastHudUpdateNanos >= 250_000_000L

        if (phaseChanged || aimingChanged || ballsChanged || timeChanged ||
            (hudDue && (
                engine.score != current.score ||
                    engine.round != current.round ||
                    engine.coinsThisSession != current.coins
                ))
        ) {
            lastHudUpdateNanos = now
            _uiState.update {
                it.copy(
                    phase = engine.phase,
                    score = engine.score,
                    round = engine.round,
                    totalBalls = engine.totalBalls,
                    coins = engine.coinsThisSession,
                    timeRemaining = timeRemaining,
                    isAiming = engine.isAiming
                )
            }
        }

        if (engine.phase == GamePhase.GAME_OVER) {
            running = false
            if (BuildConfig.ADS_ENABLED && !continueUsedThisGame && !gameEndHandled) {
                if (!_uiState.value.showContinueOffer) {
                    _uiState.update {
                        it.copy(
                            showContinueOffer = true,
                            phase = GamePhase.GAME_OVER
                        )
                    }
                }
            } else if (!gameEndHandled) {
                viewModelScope.launch { handleGameEnd(isVictory = false) }
            }
        }
        if (engine.phase == GamePhase.VICTORY) {
            running = false
            viewModelScope.launch { handleGameEnd(isVictory = true) }
        }
    }

    fun onDragStart(x: Float, y: Float) {
        engine.startAim(x, y)
    }

    fun onDrag(x: Float, y: Float) = engine.updateAim(x, y)

    fun onDragEnd() {
        engine.releaseAim()
        _uiState.update { it.copy(isAiming = engine.isAiming) }
    }

    fun cancelShot() {
        if (!running) return
        if (!engine.cancelShot()) return
        _uiState.update {
            it.copy(
                phase = engine.phase,
                isAiming = false
            )
        }
    }

    fun pause() {
        running = false
        engine.pause()
        viewModelScope.launch { autoSave() }
    }

    fun resume() {
        if (!gameStarted) return
        engine.resume()
        running = true
    }

    fun saveAndQuit() {
        running = false
        viewModelScope.launch {
            autoSave()
            persistProgress()
            audioManager.stopMusic()
        }
    }

    private suspend fun autoSave() {
        if (engine.phase == GamePhase.GAME_OVER || engine.phase == GamePhase.VICTORY) return
        gameSaveRepository.saveGame(
            GameSaveState(
                round = engine.round,
                score = engine.score,
                totalBalls = engine.totalBalls,
                coinsThisSession = engine.coinsThisSession,
                mode = engine.config.mode,
                challengeLevel = engine.config.challengeLevel,
                timeAttackRemaining = engine.timeAttackRemaining,
                launcherX = engine.launcherX,
                nextLauncherX = engine.nextLauncherX,
                hasNextLauncher = engine.hasNextLauncher,
                bricksJson = GameStateSerializer.serializeBricks(engine.bricks),
                collectablesJson = GameStateSerializer.serializeCollectables(engine.collectables),
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private suspend fun handleGameEnd(isVictory: Boolean) {
        if (gameEndHandled) return
        gameEndHandled = true
        running = false
        gameStarted = false
        if (!isVictory) audioManager.play(SoundEffect.GAME_OVER)
        persistProgress()
        highScoreRepository.saveHighScore(engine.score, engine.round, engine.config.mode.name)
        gameSaveRepository.clearSave(engine.config.mode)
        if (isVictory && engine.config.mode == GameMode.CHALLENGE) {
            challengeRepository.completeLevel(engine.config.challengeLevel)
        }
        _uiState.update {
            it.copy(
                phase = if (isVictory) GamePhase.VICTORY else GamePhase.GAME_OVER,
                score = engine.score,
                round = engine.round
            )
        }
    }

    private suspend fun persistProgress() {
        val earned = engine.coinsThisSession.toLong()
        if (earned > 0) playerRepository.addCoins(earned)
        playerRepository.updateStatistics { stats ->
            stats.copy(
                highestRound = maxOf(stats.highestRound, engine.round),
                totalBricksDestroyed = stats.totalBricksDestroyed + engine.bricksDestroyedTotal,
                totalBallsLaunched = stats.totalBallsLaunched + engine.ballsLaunchedTotal,
                totalPlayTimeMs = stats.totalPlayTimeMs + engine.getPlayTimeMs(),
                totalCoinsEarned = stats.totalCoinsEarned + earned,
                totalGamesPlayed = stats.totalGamesPlayed + 1
            )
        }
        checkAchievements()
    }

    private suspend fun buildAchievementSnapshot(): AchievementSnapshot {
        val stats = playerRepository.getStatistics()
        return AchievementSnapshot(
            bricksDestroyed = stats.totalBricksDestroyed + engine.bricksDestroyedTotal,
            highestRound = maxOf(stats.highestRound.toLong(), engine.round.toLong()),
            coins = stats.totalCoinsEarned + engine.coinsThisSession.toLong(),
            ballsOwned = engine.totalBalls.toLong(),
            playTimeMs = stats.totalPlayTimeMs + engine.getPlayTimeMs(),
            gamesPlayed = stats.totalGamesPlayed.toLong()
        )
    }

    private suspend fun checkAchievements() {
        val unlocked = playerRepository.checkAchievements(buildAchievementSnapshot())
        if (unlocked.isNotEmpty()) {
            audioManager.play(SoundEffect.ACHIEVEMENT)
            _uiState.update { state ->
                val merged = (state.newAchievements + unlocked).distinctBy { it.id }
                state.copy(newAchievements = merged)
            }
        }
    }

    fun dismissAchievement() {
        _uiState.update { state ->
            state.copy(newAchievements = state.newAchievements.drop(1))
        }
    }

    fun watchAdToContinue(activity: Activity) {
        if (!BuildConfig.ADS_ENABLED || continueUsedThisGame) return
        _uiState.update { it.copy(continueAdLoading = true) }
        rewardedAdProvider.showRewardedAd(
            activity = activity,
            onRewarded = {
                continueUsedThisGame = true
                engine.continueAfterRewardedAd(3)
                running = true
                gameEndHandled = false
                _uiState.update {
                    it.copy(
                        showContinueOffer = false,
                        continueAdLoading = false,
                        phase = GamePhase.AIMING,
                        score = engine.score,
                        round = engine.round,
                        totalBalls = engine.totalBalls,
                        coins = engine.coinsThisSession
                    )
                }
            },
            onClosed = {
                _uiState.update { it.copy(continueAdLoading = false) }
            },
            onFailed = {
                _uiState.update { it.copy(continueAdLoading = false) }
            }
        )
    }

    fun declineContinueOffer() {
        if (gameEndHandled) return
        _uiState.update { it.copy(showContinueOffer = false, continueAdLoading = false) }
        viewModelScope.launch { handleGameEnd(isVictory = false) }
    }

    override fun onCleared() {
        super.onCleared()
        running = false
        audioManager.stopMusic()
    }
}
