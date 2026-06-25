package com.mostafa.brickblast.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostafa.brickblast.domain.model.Achievement
import com.mostafa.brickblast.domain.model.AppSettings
import com.mostafa.brickblast.domain.model.GameConfig
import com.mostafa.brickblast.domain.model.GameMode
import com.mostafa.brickblast.domain.model.GamePhase
import com.mostafa.brickblast.domain.model.GameSaveState
import com.mostafa.brickblast.domain.model.PlayerStatistics
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
    val newAchievements: List<Achievement> = emptyList(),
    val screenWidth: Float = 1080f,
    val screenHeight: Float = 1920f,
    // Monotonic per-frame counter. Forces StateFlow to emit every frame so the
    // Canvas redraws even when score/phase are unchanged (e.g. a ball flying
    // through empty space). Without this the render would appear frozen.
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
    private val audioManager: AudioManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val gameEngine: GameEngine get() = engine

    private var gameStarted = false
    @Volatile private var running = false
    private var gameEndHandled = false
    private var totalCoins = 0L
    private var lastBounceSoundMs = 0L

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
        running = false
        // Clear end-state immediately so GameScreen does not bounce back to GameOver
        // before the async init finishes (reused ViewModel + singleton engine).
        _uiState.update { it.copy(phase = GamePhase.AIMING) }

        viewModelScope.launch {
            val upgrades = playerRepository.getUpgrades()
            totalCoins = playerRepository.getCoins()
            engine.bestScore = highScoreRepository.getBestScore()
            val settings = settingsRepository.settings.first()
            audioManager.setSoundEnabled(settings.soundEnabled)
            audioManager.setMusicEnabled(settings.musicEnabled)
            if (settings.musicEnabled) audioManager.startMusic()

            var restoredRound = 1
            var restoredBalls = -1
            var restoredScore = 0
            if (continueGame) {
                gameSaveRepository.loadGame()?.let { save ->
                    restoredRound = save.round
                    restoredBalls = save.totalBalls
                    restoredScore = save.score
                }
            } else {
                gameSaveRepository.clearSave()
            }

            val config = GameConfig(
                mode = mode,
                challengeLevel = challengeLevel,
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
            engine.onGameOver = {
                viewModelScope.launch { handleGameEnd(isVictory = false) }
            }
            engine.onRoundComplete = { r ->
                viewModelScope.launch {
                    autoSave()
                    checkAchievements()
                }
            }
            engine.onPowerUpActivated = { audioManager.play(SoundEffect.POWER_UP) }

            val w = _uiState.value.screenWidth
            val h = _uiState.value.screenHeight
            engine.initGame(w, h, config, upgrades, restoredRound, restoredBalls, restoredScore)
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
                    timeRemaining = if (mode == GameMode.TIME_ATTACK) engine.timeAttackRemaining else null
                )
            }

            running = true
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
        if (engine.phase != current.phase ||
            engine.score != current.score ||
            engine.round != current.round ||
            engine.totalBalls != current.totalBalls ||
            engine.coinsThisSession != current.coins ||
            timeChanged
        ) {
            _uiState.update {
                it.copy(
                    phase = engine.phase,
                    score = engine.score,
                    round = engine.round,
                    totalBalls = engine.totalBalls,
                    coins = engine.coinsThisSession,
                    timeRemaining = timeRemaining
                )
            }
        }

        if (engine.phase == GamePhase.GAME_OVER) {
            running = false
            viewModelScope.launch { handleGameEnd(isVictory = false) }
        }
        if (engine.phase == GamePhase.VICTORY) {
            running = false
            viewModelScope.launch { handleGameEnd(isVictory = true) }
        }
    }

    fun onDragStart(x: Float, y: Float) = engine.startAim(x, y)
    fun onDrag(x: Float, y: Float) = engine.updateAim(x, y)
    fun onDragEnd() = engine.releaseAim()

    fun pause() {
        running = false
        engine.pause()
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
        gameSaveRepository.saveGame(
            GameSaveState(
                round = engine.round,
                score = engine.score,
                totalBalls = engine.totalBalls,
                coinsThisSession = engine.coinsThisSession,
                mode = engine.config.mode,
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
        gameSaveRepository.clearSave()
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

    private suspend fun checkAchievements() {
        val unlocked = playerRepository.checkAchievements(
            bricksDestroyed = engine.bricksDestroyedTotal,
            round = engine.round,
            coins = playerRepository.getCoins(),
            balls = engine.totalBalls
        )
        if (unlocked.isNotEmpty()) {
            _uiState.update { it.copy(newAchievements = unlocked) }
        }
    }

    fun dismissAchievement() {
        _uiState.update { it.copy(newAchievements = emptyList()) }
    }

    override fun onCleared() {
        super.onCleared()
        running = false
        audioManager.stopMusic()
    }
}
