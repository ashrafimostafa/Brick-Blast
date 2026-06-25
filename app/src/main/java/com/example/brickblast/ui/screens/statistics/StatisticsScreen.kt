package com.example.brickblast.ui.screens.statistics

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brickblast.ui.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val stats by viewModel.statistics.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val topScores by viewModel.topScores.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Text("Player Stats", color = Color(0xFF448AFF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            StatLine("Highest Round", stats.highestRound.toString())
            StatLine("Bricks Destroyed", stats.totalBricksDestroyed.toString())
            StatLine("Balls Launched", stats.totalBallsLaunched.toString())
            StatLine("Play Time", formatPlayTime(stats.totalPlayTimeMs))
            StatLine("Coins Earned", stats.totalCoinsEarned.toString())
            StatLine("Games Played", stats.totalGamesPlayed.toString())

            Text("High Scores", color = Color(0xFF448AFF), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp))
            topScores.forEachIndexed { i, (score, round, mode) ->
                StatLine("#${i + 1} $mode", "$score (R$round)")
            }

            Text("Achievements", color = Color(0xFF448AFF), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp))
            achievements.forEach { a ->
                val status = if (a.unlocked) "✓" else "${a.progress}/${a.target}"
                StatLine(a.title, status)
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Text("$label: $value", color = Color.White, fontSize = 15.sp)
}

private fun formatPlayTime(ms: Long): String {
    val minutes = ms / 60000
    val hours = minutes / 60
    return if (hours > 0) "${hours}h ${minutes % 60}m" else "${minutes}m"
}
