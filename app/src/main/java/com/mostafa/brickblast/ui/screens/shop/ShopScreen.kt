package com.mostafa.brickblast.ui.screens.shop

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mostafa.brickblast.R
import com.mostafa.brickblast.domain.model.UpgradeType
import com.mostafa.brickblast.ui.components.GameButton
import com.mostafa.brickblast.ui.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onBack: () -> Unit,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val coins by viewModel.coins.collectAsState()
    val upgrades by viewModel.upgrades.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shop_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            Text(
                stringResource(R.string.shop_your_coins, coins),
                color = Color(0xFFFFD600),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.shop_subtitle),
                color = Color.White.copy(0.6f),
                fontSize = 14.sp
            )

            ShopItem(
                stringResource(R.string.upgrade_ball_damage_plus),
                stringResource(R.string.upgrade_level, upgrades.ballDamageLevel),
                upgrades.upgradeCost(UpgradeType.BALL_DAMAGE)
            ) {
                viewModel.purchaseUpgrade(UpgradeType.BALL_DAMAGE)
            }
            ShopItem(
                stringResource(R.string.upgrade_starting_balls_plus),
                stringResource(R.string.upgrade_level, upgrades.startingBallsLevel),
                upgrades.upgradeCost(UpgradeType.STARTING_BALLS)
            ) {
                viewModel.purchaseUpgrade(UpgradeType.STARTING_BALLS)
            }
            ShopItem(
                stringResource(R.string.upgrade_coin_multiplier),
                stringResource(R.string.upgrade_level, upgrades.coinMultiplierLevel),
                upgrades.upgradeCost(UpgradeType.COIN_MULTIPLIER)
            ) {
                viewModel.purchaseUpgrade(UpgradeType.COIN_MULTIPLIER)
            }
            ShopItem(
                stringResource(R.string.upgrade_critical_hit_chance),
                stringResource(R.string.upgrade_level, upgrades.criticalHitLevel),
                upgrades.upgradeCost(UpgradeType.CRITICAL_HIT)
            ) {
                viewModel.purchaseUpgrade(UpgradeType.CRITICAL_HIT)
            }
        }
    }
}

@Composable
private fun ShopItem(name: String, level: String, cost: Int, onBuy: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = Color.White, fontWeight = FontWeight.Medium)
            Text(level, color = Color.White.copy(0.5f), fontSize = 13.sp)
        }
        GameButton(text = "$cost", onClick = onBuy, modifier = Modifier.fillMaxWidth(0.35f))
    }
}
