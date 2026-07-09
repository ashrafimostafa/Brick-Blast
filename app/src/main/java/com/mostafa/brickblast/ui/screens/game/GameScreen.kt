package com.mostafa.brickblast.ui.screens.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.mostafa.brickblast.R
import com.mostafa.brickblast.domain.model.GameMode
import com.mostafa.brickblast.domain.model.GamePhase
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
    val context = LocalContext.current
    val persianUi = LocalConfiguration.current.locales[0].language == "fa"
    val pauseLabel = stringResource(R.string.pause_game)
    val cancelShotLabel = stringResource(R.string.cancel_shot)
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
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
        val a11yText = remember(
            uiState.score,
            uiState.round,
            uiState.phase,
            uiState.totalBalls,
            uiState.coins,
            uiState.isAiming,
            uiState.timeRemaining
        ) {
            GameAccessibility.statusDescription(
                context = context,
                score = uiState.score,
                bestScore = engine.bestScore,
                round = uiState.round,
                totalBalls = uiState.totalBalls,
                coins = uiState.coins,
                phase = uiState.phase,
                isAiming = uiState.isAiming,
                timeRemaining = uiState.timeRemaining
            )
        }
        LiveRegionAnnouncement(
            text = a11yText,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Isolated layer: only this subtree recomposes every frame, not the HUD/overlays.
        GameWorldCanvas(
            viewModel = viewModel,
            showTrajectory = uiState.showTrajectory,
            isDarkTheme = isDarkTheme,
            persianUi = persianUi,
            onCanvasSizeChanged = { canvasSize = it },
            modifier = Modifier.fillMaxSize()
        )

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
                .semantics { contentDescription = pauseLabel }
        ) {
            Icon(Icons.Default.Pause, contentDescription = null)
        }

        if (uiState.phase == GamePhase.LAUNCHING || uiState.phase == GamePhase.SIMULATING) {
            FloatingActionButton(
                onClick = { viewModel.cancelShot() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp)
                    .semantics { contentDescription = cancelShotLabel }
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = null)
            }
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

/** Draws and simulates the game world on a Choreographer-driven surface (no per-frame Compose). */
@Composable
private fun GameWorldCanvas(
    viewModel: GameViewModel,
    showTrajectory: Boolean,
    isDarkTheme: Boolean,
    persianUi: Boolean,
    onCanvasSizeChanged: (IntSize) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val boardDescription = GameAccessibility.gameBoardDescription(context)

    AndroidView(
        factory = { ctx -> GameCanvasView(ctx) },
        update = { view ->
            view.viewModel = viewModel
            view.showTrajectory = showTrajectory
            view.isDarkTheme = isDarkTheme
            view.persianUi = persianUi
            view.onCanvasSizeChanged = { w, h -> onCanvasSizeChanged(IntSize(w, h)) }
            view.contentDescription = boardDescription
        },
        modifier = modifier.semantics { contentDescription = boardDescription }
    )
}
