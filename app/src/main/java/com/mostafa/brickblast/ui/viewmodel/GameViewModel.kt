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
        if (gameStarted) {
            resume()
            return
        }
        gameStarted = true
        gameEndHandled = false

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
                if (now - lastBounceSoundMs > 100L) {
                    audioManager.play(SoundEffect.BOUNCE)
                    lastBounceSoundMs = now
                }
                if (_uiState.value.particleEffects) {
                    var activeCount = 0
                    for (b in engine.balls) if (b.active) activeCount++
                    if (activeCount <= 4) {
                        engine.balls.firstOrNull { it.active }?.let { ball ->
                            engine.particles.emitSpark(ball.x, ball.y)
                        }
                    }
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

        // Only push to StateFlow when the phase actually changes. The live HUD and
        // world are rendered directly from the engine each frame (driven by the
        // composable's frame clock), so we avoid a full recomposition per frame.
        if (engine.phase != _uiState.value.phase) {
            _uiState.update {
                it.copy(
                    phase = engine.phase,
                    score = engine.score,
                    round = engine.round
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
        if (!isVictory) audioManager.play(SoundEffect.GAME_OVER)
        persistProgress()
        highScoreRepository.saveHighScore(engine.score, engine.round, engine.config.mode.name)
        gameSaveRepository.clearSave()
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
