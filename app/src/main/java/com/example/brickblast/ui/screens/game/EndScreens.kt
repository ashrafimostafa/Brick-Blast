package com.example.brickblast.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brickblast.ui.components.GameButton
import com.example.brickblast.ui.components.SecondaryButton
import com.example.brickblast.ui.util.ScoreShareCard
import com.example.brickblast.ui.util.ScoreShareHelper
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
        Text("Paused", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(
            "Take a break",
            fontSize = 16.sp,
            color = Color.White.copy(0.6f),
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        GameButton(text = "Resume", onClick = onResume)
        SecondaryButton(text = "Save & Quit", onClick = onQuit, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    round: Int,
    mode: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    EndGameScreen(
        title = "Game Over",
        subtitle = "You reached round $round",
        score = score,
        roundReached = round,
        mode = mode,
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
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    EndGameScreen(
        title = "Victory!",
        subtitle = "Completed round $round",
        score = score,
        roundReached = round,
        mode = mode,
        onRetry = onRetry,
        onBack = onBack,
        showShare = false,
        titleColor = Color(0xFF69F0AE)
    )
}

@Composable
private fun EndGameScreen(
    title: String,
    subtitle: String,
    score: Int,
    roundReached: Int,
    mode: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    showShare: Boolean,
    titleColor: Color
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = ink.copy(alpha = 0.65f)
    val graphicsLayer = rememberGraphicsLayer()
    var sharing by remember { mutableStateOf(false) }

    LaunchedEffect(sharing) {
        if (!sharing) return@LaunchedEffect
        delay(50) // one frame so the share card is recorded into the graphics layer
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = titleColor)
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
                color = ink
            )
            Text(
                text = "SCORE",
                fontSize = 14.sp,
                color = muted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Mode: ${mode.replace('_', ' ')}",
                fontSize = 14.sp,
                color = muted.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 40.dp)
            )

            GameButton(text = "Play Again", onClick = onRetry)
            SecondaryButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.padding(top = 12.dp)
            )
            if (showShare) {
                SecondaryButton(
                    text = "Share Score",
                    onClick = { sharing = true },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        // Off-screen card used to render the share image.
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
