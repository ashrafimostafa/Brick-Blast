package com.example.brickblast.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brickblast.ui.components.GameButton
import com.example.brickblast.ui.components.SecondaryButton

@Composable
fun MainMenuScreen(
    hasActiveSave: Boolean,
    onPlay: () -> Unit,
    onChallenge: () -> Unit,
    onTimeAttack: () -> Unit,
    onHardcore: () -> Unit,
    onSettings: () -> Unit,
    onStatistics: () -> Unit,
    onContinue: () -> Unit
) {
    val titleColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "BRICK BLAST",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor
        )
        Spacer(modifier = Modifier.height(48.dp))

        if (hasActiveSave) {
            GameButton(text = "Continue", onClick = onContinue)
            Spacer(modifier = Modifier.height(12.dp))
        }
        GameButton(text = "Play", onClick = onPlay)
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(text = "Challenge", onClick = onChallenge)
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(text = "Time Attack", onClick = onTimeAttack)
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(text = "Hardcore", onClick = onHardcore)
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(text = "Statistics", onClick = onStatistics)
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(text = "Settings", onClick = onSettings)
    }
}
