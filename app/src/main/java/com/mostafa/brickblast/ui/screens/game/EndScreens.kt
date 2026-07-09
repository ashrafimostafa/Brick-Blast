package com.mostafa.brickblast.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mostafa.brickblast.R
import com.mostafa.brickblast.domain.model.ChallengeConfig
import com.mostafa.brickblast.domain.model.GameMode
import com.mostafa.brickblast.ui.accessibility.LiveRegionAnnouncement
import com.mostafa.brickblast.ui.accessibility.screenHeading
import com.mostafa.brickblast.ui.components.GameButton
import com.mostafa.brickblast.ui.components.SecondaryButton
import com.mostafa.brickblast.ui.util.ScoreShareCard
import com.mostafa.brickblast.ui.util.ScoreShareHelper
import com.mostafa.brickblast.ui.util.gameModeLabel
import kotlinx.coroutines.delay

@Composable
fun PauseScreen(
    onResume: () -> Unit,
    onQuit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.85f))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.paused_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.screenHeading()
        )
        Text(
            stringResource(R.string.paused_subtitle),
            fontSize = 16.sp,
            color = Color.White.copy(0.6f),
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        GameButton(text = stringResource(R.string.resume), onClick = onResume)
        SecondaryButton(
            text = stringResource(R.string.save_and_quit),
            onClick = onQuit,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    round: Int,
    mode: String,
    coinsEarned: Int = 0,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    EndGameScreen(
        title = stringResource(R.string.game_over),
        subtitle = stringResource(R.string.game_over_subtitle, round),
        score = score,
        roundReached = round,
        mode = mode,
        coinsEarned = coinsEarned,
        onRetry = onRetry,
        onBack = onBack,
        showShare = true,
        titleColor = Color(0xFFFF5252)
    )
}

@Composable
fun VictoryScreen(
    score: Int,
    round: Int,
    mode: String,
    challengeLevel: Int = 1,
    coinsEarned: Int = 0,
    onRetry: () -> Unit,
    onNextLevel: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val isChallenge = mode == GameMode.CHALLENGE.name
    val hasNextLevel = isChallenge && challengeLevel < ChallengeConfig.TOTAL_LEVELS

    EndGameScreen(
        title = stringResource(R.string.victory),
        subtitle = if (isChallenge) {
            stringResource(R.string.victory_challenge_subtitle, challengeLevel)
        } else {
            stringResource(R.string.victory_round_subtitle, round)
        },
        score = score,
        roundReached = round,
        mode = mode,
        coinsEarned = coinsEarned,
        onRetry = onRetry,
        onBack = onBack,
        showShare = false,
        titleColor = Color(0xFF69F0AE),
        nextButtonText = if (hasNextLevel) stringResource(R.string.next_level) else null,
        onNextLevel = if (hasNextLevel) onNextLevel else null
    )
}

@Composable
private fun EndGameScreen(
    title: String,
    subtitle: String,
    score: Int,
    roundReached: Int,
    mode: String,
    coinsEarned: Int,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    showShare: Boolean,
    titleColor: Color,
    nextButtonText: String? = null,
    onNextLevel: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val localizedMode = gameModeLabel(mode)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = ink.copy(alpha = 0.65f)
    val graphicsLayer = rememberGraphicsLayer()
    var sharing by remember { mutableStateOf(false) }

    LaunchedEffect(sharing) {
        if (!sharing) return@LaunchedEffect
        delay(50)
        val bitmap = runCatching { graphicsLayer.toImageBitmap() }.getOrNull()
        sharing = false
        if (bitmap != null) {
            ScoreShareHelper.shareScoreImage(context, bitmap, score, roundReached, mode)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LiveRegionAnnouncement(
            text = stringResource(
                R.string.end_screen_announcement,
                title,
                score,
                subtitle,
                localizedMode
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                title,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                modifier = Modifier.screenHeading()
            )
            Text(
                subtitle,
                fontSize = 16.sp,
                color = muted,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = score.toString(),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = ink,
                modifier = Modifier.semantics {
                    contentDescription = context.getString(R.string.score_points, score)
                }
            )
            Text(
                text = stringResource(R.string.score_label),
                fontSize = 14.sp,
                color = muted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (coinsEarned > 0) {
                Text(
                    text = stringResource(R.string.end_coins_earned, coinsEarned),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFD600),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Text(
                text = stringResource(R.string.mode_label, localizedMode),
                fontSize = 14.sp,
                color = muted.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 40.dp)
            )

            if (nextButtonText != null && onNextLevel != null) {
                GameButton(text = nextButtonText, onClick = onNextLevel)
                SecondaryButton(
                    text = stringResource(R.string.play_again),
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                GameButton(text = stringResource(R.string.play_again), onClick = onRetry)
            }
            SecondaryButton(
                text = stringResource(R.string.back),
                onClick = onBack,
                modifier = Modifier.padding(top = 12.dp)
            )
            if (showShare) {
                SecondaryButton(
                    text = stringResource(R.string.share_score),
                    onClick = { sharing = true },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        if (showShare) {
            Box(
                modifier = Modifier
                    .size(600.dp, 800.dp)
                    .alpha(0f)
                    .drawWithContent {
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    }
            ) {
                ScoreShareCard(
                    score = score,
                    round = roundReached,
                    mode = mode,
                    isDark = isDark
                )
            }
        }
    }
}
