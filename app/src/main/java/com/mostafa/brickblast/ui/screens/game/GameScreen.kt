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
import androidx.compose.ui.platform.LocalContext
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
    val activity = LocalContext.current as? android.app.Activity
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val textMeasurer = rememberTextMeasurer(cacheSize = 128)
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var frameTick by remember { mutableIntStateOf(0) }
    var lastReportedPhase by remember(mode, challengeLevel, continueGame) {
        mutableStateOf<GamePhase?>(null)
    }

    LaunchedEffect(uiState.showContinueOffer, activity) {
        if (uiState.showContinueOffer) {
            activity?.let { viewModel.preloadRewardedAd(it) }
        }
    }

    LaunchedEffect(activity) {
        activity?.let { viewModel.preloadRewardedAd(it) }
    }

    LaunchedEffect(canvasSize, mode, challengeLevel, continueGame) {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            viewModel.setScreenSize(canvasSize.width.toFloat(), canvasSize.height.toFloat())
            viewModel.startGame(mode, challengeLevel, continueGame)
            lastReportedPhase = GamePhase.AIMING
        }
    }

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

    LaunchedEffect(uiState.phase, uiState.showContinueOffer) {
        if (uiState.showContinueOffer) return@LaunchedEffect
        val previous = lastReportedPhase
        lastReportedPhase = uiState.phase
        if (previous == null) return@LaunchedEffect
        if (previous == uiState.phase) return@LaunchedEffect
        when (uiState.phase) {
            GamePhase.GAME_OVER -> onGameOver(engine.score, engine.round)
            GamePhase.VICTORY -> onVictory(engine.score, engine.round)
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LiveRegionAnnouncement(
            text = GameAccessibility.statusDescription(
                score = uiState.score,
                bestScore = engine.bestScore,
                round = uiState.round,
                totalBalls = uiState.totalBalls,
                coins = uiState.coins,
                phase = uiState.phase,
                isAiming = engine.isAiming,
                timeRemaining = uiState.timeRemaining
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
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            viewModel.onDrag(change.position.x, change.position.y)
                        },
                        onDragEnd = { viewModel.onDragEnd() },
                        onDragCancel = { }
                    )
                }
        ) {
            @Suppress("UNUSED_EXPRESSION")
            frameTick
            with(GameRenderer) {
                renderGame(
                    engine = engine,
                    trajectoryPoints = engine.getTrajectoryPoints(),
                    showTrajectory = uiState.showTrajectory,
                    isDark = isDarkTheme,
                    textMeasurer = textMeasurer
                )
            }
        }

        GameHud(
            score = uiState.score,
            bestScore = engine.bestScore,
            round = uiState.round,
            totalBalls = uiState.totalBalls,
            coins = uiState.coins,
            timeRemaining = uiState.timeRemaining,
            modifier = Modifier.align(Alignment.TopStart)
        )

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

        if (uiState.showContinueOffer) {
            ContinueOfferOverlay(
                loading = uiState.continueAdLoading,
                onWatchAd = {
                    activity?.let { viewModel.watchAdToContinue(it) }
                },
                onGiveUp = { viewModel.declineContinueOffer() },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
