package com.mostafa.brickblast.ui.screens.menu

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mostafa.brickblast.R
import com.mostafa.brickblast.ui.accessibility.screenHeading
import com.mostafa.brickblast.ui.components.GameButton
import com.mostafa.brickblast.ui.components.SecondaryButton

@Composable
fun MainMenuScreen(
    hasActiveSave: Boolean,
    onPlay: () -> Unit,
    onChallenge: () -> Unit,
    onTimeAttack: () -> Unit,
    onHardcore: () -> Unit,
    onSettings: () -> Unit,
    onStatistics: () -> Unit,
    onAchievements: () -> Unit,
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
            text = stringResource(R.string.title_brick_blast),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            modifier = Modifier.screenHeading()
        )
        Spacer(modifier = Modifier.height(48.dp))

        if (hasActiveSave) {
            GameButton(text = stringResource(R.string.menu_continue), onClick = onContinue)
            Spacer(modifier = Modifier.height(12.dp))
        }
        GameButton(text = stringResource(R.string.menu_play), onClick = onPlay)
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(text = stringResource(R.string.menu_challenge), onClick = onChallenge)
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(text = stringResource(R.string.menu_time_attack), onClick = onTimeAttack)
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(text = stringResource(R.string.menu_hardcore), onClick = onHardcore)
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(text = stringResource(R.string.menu_statistics), onClick = onStatistics)
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(text = stringResource(R.string.menu_achievements), onClick = onAchievements)
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(text = stringResource(R.string.menu_settings), onClick = onSettings)
    }
}
