package com.mostafa.brickblast.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mostafa.brickblast.R
import com.mostafa.brickblast.ui.accessibility.screenHeading
import com.mostafa.brickblast.ui.util.gameModeLabel
import com.mostafa.brickblast.ui.components.SecondaryButton
import com.mostafa.brickblast.ui.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    onAchievements: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val stats by viewModel.statistics.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val earnedCount = achievements.count { it.unlocked }
    val topScores by viewModel.topScores.collectAsState()
    val navigateBackLabel = stringResource(R.string.navigate_back)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statistics_title), modifier = Modifier.screenHeading()) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = navigateBackLabel
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0D1B2A))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.player_stats),
                color = Color(0xFF448AFF),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.screenHeading()
            )
            StatLine(stringResource(R.string.stat_highest_round), stats.highestRound.toString())
            StatLine(stringResource(R.string.stat_bricks_destroyed), stats.totalBricksDestroyed.toString())
            StatLine(stringResource(R.string.stat_balls_launched), stats.totalBallsLaunched.toString())
            StatLine(stringResource(R.string.stat_play_time), formatPlayTime(stats.totalPlayTimeMs))
            StatLine(stringResource(R.string.stat_coins_earned), stats.totalCoinsEarned.toString())
            StatLine(stringResource(R.string.stat_games_played), stats.totalGamesPlayed.toString())

            Text(
                stringResource(R.string.high_scores),
                color = Color(0xFF448AFF),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .screenHeading()
            )
            topScores.forEachIndexed { i, (score, round, mode) ->
                StatLine(
                    stringResource(R.string.high_score_entry, i + 1, gameModeLabel(mode)),
                    stringResource(R.string.high_score_value, score, round)
                )
            }

            Text(
                stringResource(R.string.achievements),
                color = Color(0xFF448AFF),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .screenHeading()
            )
            StatLine(
                stringResource(R.string.achievements_summary, earnedCount, achievements.size),
                stringResource(R.string.achievements_earned_short, earnedCount)
            )
            SecondaryButton(
                text = stringResource(R.string.view_all_achievements),
                onClick = onAchievements,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    val line = stringResource(R.string.stat_line, label, value)
    val a11y = stringResource(R.string.stat_line_a11y, label, value)
    Text(
        line,
        color = Color.White,
        fontSize = 15.sp,
        modifier = Modifier.semantics { contentDescription = a11y }
    )
}

@Composable
private fun formatPlayTime(ms: Long): String {
    val minutes = ms / 60000
    val hours = minutes / 60
    return if (hours > 0) {
        stringResource(R.string.play_time_hours_minutes, hours, minutes % 60)
    } else {
        stringResource(R.string.play_time_minutes, minutes)
    }
}
