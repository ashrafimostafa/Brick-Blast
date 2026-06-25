package com.mostafa.brickblast.ui.screens.game

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameHud(
    score: Int,
    bestScore: Int,
    round: Int,
    totalBalls: Int,
    coins: Int,
    timeRemaining: Float?,
    modifier: Modifier = Modifier
) {
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = ink.copy(alpha = 0.6f)
    val isNewBest = score > bestScore
    val shownBest = maxOf(score, bestScore)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BEST  $shownBest",
                fontSize = 14.sp,
                color = if (isNewBest) androidx.compose.ui.graphics.Color(0xFFFFB300) else muted,
                fontWeight = if (isNewBest) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = score.toString(),
                fontSize = 34.sp,
                color = if (isNewBest) androidx.compose.ui.graphics.Color(0xFFFFB300) else ink,
                fontWeight = FontWeight.Bold
            )
            if (isNewBest) {
                Text(
                    text = "NEW BEST!",
                    fontSize = 13.sp,
                    color = androidx.compose.ui.graphics.Color(0xFFFFB300),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text("Round  $round", fontSize = 15.sp, color = ink.copy(alpha = 0.85f))
            Text("Balls  $totalBalls", fontSize = 15.sp, color = ink.copy(alpha = 0.85f))
            Text("Coins  $coins", fontSize = 15.sp, color = ink.copy(alpha = 0.85f))
            timeRemaining?.let {
                Text("Time  ${it.toInt()}s", fontSize = 15.sp, color = ink.copy(alpha = 0.85f))
            }
        }
    }
}
