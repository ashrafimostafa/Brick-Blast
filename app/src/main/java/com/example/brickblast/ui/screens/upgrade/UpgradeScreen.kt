package com.example.brickblast.ui.screens.upgrade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brickblast.domain.model.UpgradeType
import com.example.brickblast.ui.components.GameButton
import com.example.brickblast.ui.viewmodel.UpgradeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeScreen(
    onBack: () -> Unit,
    viewModel: UpgradeViewModel = hiltViewModel()
) {
    val upgrades by viewModel.upgrades.collectAsState()
    val coins by viewModel.coins.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upgrades") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Coins: $coins", color = Color(0xFFFFD600), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            UpgradeRow("Ball Damage", "Lv ${upgrades.ballDamageLevel}", upgrades.upgradeCost(UpgradeType.BALL_DAMAGE)) {
                viewModel.upgrade(UpgradeType.BALL_DAMAGE)
            }
            UpgradeRow("Starting Balls", "Lv ${upgrades.startingBallsLevel}", upgrades.upgradeCost(UpgradeType.STARTING_BALLS)) {
                viewModel.upgrade(UpgradeType.STARTING_BALLS)
            }
            UpgradeRow("Coin Multiplier", "Lv ${upgrades.coinMultiplierLevel}", upgrades.upgradeCost(UpgradeType.COIN_MULTIPLIER)) {
                viewModel.upgrade(UpgradeType.COIN_MULTIPLIER)
            }
            UpgradeRow("Critical Hit", "Lv ${upgrades.criticalHitLevel}", upgrades.upgradeCost(UpgradeType.CRITICAL_HIT)) {
                viewModel.upgrade(UpgradeType.CRITICAL_HIT)
            }
        }
    }
}

@Composable
private fun UpgradeRow(label: String, level: String, cost: Int, onUpgrade: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, color = Color.White, fontWeight = FontWeight.Medium)
            Text(level, color = Color.White.copy(0.6f), fontSize = 13.sp)
        }
        GameButton(
            text = "$cost coins",
            onClick = onUpgrade,
            modifier = Modifier.fillMaxWidth(0.4f)
        )
    }
}
