package com.mostafa.brickblast.ui.screens.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mostafa.brickblast.domain.model.GameMode
import com.mostafa.brickblast.domain.model.GamePhase
import com.mostafa.brickblast.game.renderer.GameRenderer
import com.mostafa.brickblast.ui.accessibility.GameAccessibility
import com.mostafa.brickblast.ui.accessibility.LiveRegionAnnouncement
import com.mostafa.brickblast.ui.components.AchievementPopup
import com.mostafa.brickblast.ui.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    mode: GameMode,
    challengeLevel: Int,
    continueGame: Boolean,
    onPause: () -> Unit,
    onGameOver: (Int, Int) -> Unit,
    onVictory: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val engine = viewModel.gameEngine
    // Derive theme from the active Material color scheme so the Canvas (which is
    // not theme-aware on its own) matches the rest of the app.
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    // Large cache so brick HP labels (many repeated short strings) are not
    // re-measured every frame, which was a major source of slowdown on big boards.
    val textMeasurer = rememberTextMeasurer(cacheSize = 256)
    var trajectoryPoints by remember { mutableStateOf(FloatArray(0)) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    // Incremented every display frame; read inside the Canvas draw to invalidate
    // only the draw phase (not recompose the whole screen) for smooth rendering.
    var frameTick by remember { mutableIntStateOf(0) }

    // Start the game only once the canvas has a real measured size, so the
    // play area, bricks, and launcher are laid out against the true bounds.
    LaunchedEffect(canvasSize, mode, challengeLevel, continueGame) {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            viewModel.setScreenSize(canvasSize.width.toFloat(), canvasSize.height.toFloat())
            viewModel.startGame(mode, challengeLevel, continueGame)
        }
    }

    // Drive the simulation from the display's frame clock for smooth, vsync-aligned
    // updates instead of a fixed delay() loop (which stutters).
    LaunchedEffect(Unit) {
        var lastFrame = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastFrame != 0L) {
                    val dt = ((now - lastFrame) / 1_000_000_000f).coerceAtMost(0.05f)
                    viewModel.tick(dt)
                }
                lastFrame = now
                frameTick++
            }
        }
    }

    LaunchedEffect(engine.isAiming, uiState.phase) {
        if (engine.isAiming) {
            trajectoryPoints = engine.getTrajectoryPoints()
        }
    }

    LaunchedEffect(uiState.phase) {
        when (uiState.phase) {
            GamePhase.GAME_OVER -> onGameOver(engine.score, engine.round)
            GamePhase.VICTORY -> onVictory(engine.score, engine.round)
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // TalkBack reads game stats from this live region (Canvas HUD is not accessible).
        LiveRegionAnnouncement(
            text = GameAccessibility.statusDescription(
                score = engine.score,
                bestScore = engine.bestScore,
                round = engine.round,
                totalBalls = engine.totalBalls,
                coins = engine.coinsThisSession,
                phase = uiState.phase,
                isAiming = engine.isAiming,
                timeRemaining = if (engine.config.mode == GameMode.TIME_ATTACK)
                    engine.timeAttackRemaining else null
            ),
            modifier = Modifier.align(Alignment.TopStart)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = GameAccessibility.GAME_BOARD_DESCRIPTION }
                .onSizeChanged { size -> canvasSize = size }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            viewModel.onDragStart(offset.x, offset.y)
                            trajectoryPoints = engine.getTrajectoryPoints()
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            viewModel.onDrag(change.position.x, change.position.y)
                            trajectoryPoints = engine.getTrajectoryPoints()
                        },
                        onDragEnd = {
                            viewModel.onDragEnd()
                            trajectoryPoints = FloatArray(0)
                        },
                        onDragCancel = {
                            trajectoryPoints = FloatArray(0)
                        }
                    )
                }
        ) {
            // Subscribe the draw phase to the frame clock so it redraws each frame.
            @Suppress("UNUSED_EXPRESSION")
            frameTick
            with(GameRenderer) {
                renderGame(
                    engine = engine,
                    trajectoryPoints = trajectoryPoints,
                    showTrajectory = uiState.showTrajectory,
                    isDark = isDarkTheme,
                    textMeasurer = textMeasurer
                )
                renderHud(
                    score = engine.score,
                    bestScore = engine.bestScore,
                    round = engine.round,
                    totalBalls = engine.totalBalls,
                    coins = engine.coinsThisSession,
                    timeRemaining = if (engine.config.mode == GameMode.TIME_ATTACK)
                        engine.timeAttackRemaining else null,
                    isDark = isDarkTheme,
                    textMeasurer = textMeasurer
                )
            }
        }

        FloatingActionButton(
            onClick = {
                viewModel.pause()
                onPause()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .size(56.dp)
                .semantics { contentDescription = "Pause game" }
        ) {
            Icon(Icons.Default.Pause, contentDescription = null)
        }

        if (uiState.newAchievements.isNotEmpty()) {
            AchievementPopup(
                achievements = uiState.newAchievements,
                onDismiss = { viewModel.dismissAchievement() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
            )
        }
    }
}
