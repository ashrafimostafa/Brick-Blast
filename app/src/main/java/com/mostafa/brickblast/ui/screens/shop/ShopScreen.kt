package com.mostafa.brickblast.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mostafa.brickblast.R
import com.mostafa.brickblast.domain.model.BoardVisualTheme
import com.mostafa.brickblast.domain.model.ColorPackDefinition
import com.mostafa.brickblast.domain.model.ColorPackDefinitions
import com.mostafa.brickblast.domain.model.ColorPackIds
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
    val settings by viewModel.settings.collectAsState()
    val easterEggUnlocked by viewModel.easterEggUnlocked.collectAsState()
    val coinGold = Color(0xFFFFD600)
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = ink.copy(alpha = 0.6f)

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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.shop_your_coins, coins),
                color = coinGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.shop_subtitle),
                color = muted,
                fontSize = 14.sp
            )

            Text(
                stringResource(R.string.shop_upgrades_title),
                color = ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            ShopUpgradeItem(
                name = stringResource(R.string.upgrade_ball_damage_plus),
                level = stringResource(R.string.upgrade_level, upgrades.ballDamageLevel),
                cost = upgrades.upgradeCost(UpgradeType.BALL_DAMAGE),
                coins = coins,
                onBuy = { viewModel.purchaseUpgrade(UpgradeType.BALL_DAMAGE) }
            )
            ShopUpgradeItem(
                name = stringResource(R.string.upgrade_starting_balls_plus),
                level = stringResource(R.string.upgrade_level, upgrades.startingBallsLevel),
                cost = upgrades.upgradeCost(UpgradeType.STARTING_BALLS),
                coins = coins,
                onBuy = { viewModel.purchaseUpgrade(UpgradeType.STARTING_BALLS) }
            )
            ShopUpgradeItem(
                name = stringResource(R.string.upgrade_coin_multiplier),
                level = stringResource(R.string.upgrade_level, upgrades.coinMultiplierLevel),
                cost = upgrades.upgradeCost(UpgradeType.COIN_MULTIPLIER),
                coins = coins,
                onBuy = { viewModel.purchaseUpgrade(UpgradeType.COIN_MULTIPLIER) }
            )
            ShopUpgradeItem(
                name = stringResource(R.string.upgrade_critical_hit_chance),
                level = stringResource(R.string.upgrade_level, upgrades.criticalHitLevel),
                cost = upgrades.upgradeCost(UpgradeType.CRITICAL_HIT),
                coins = coins,
                onBuy = { viewModel.purchaseUpgrade(UpgradeType.CRITICAL_HIT) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            var colorPackTitleTaps by remember { mutableIntStateOf(0) }
            var lastColorPackTitleTap by remember { mutableLongStateOf(0L) }

            Text(
                stringResource(R.string.shop_color_packs_title),
                color = ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    val now = System.currentTimeMillis()
                    if (now - lastColorPackTitleTap > 2000L) colorPackTitleTaps = 0
                    lastColorPackTitleTap = now
                    colorPackTitleTaps++
                    if (colorPackTitleTaps >= 7) {
                        colorPackTitleTaps = 0
                        viewModel.unlockAllColorPacksEasterEgg()
                    }
                }
            )
            Text(
                stringResource(R.string.shop_color_packs_subtitle),
                color = muted,
                fontSize = 14.sp
            )

            ColorPackDefinitions.ALL.forEach { pack ->
                    val owned = pack.id in settings.ownedColorPackIds || pack.price == 0
                    ColorPackCard(
                        pack = pack,
                        coins = coins,
                        owned = owned,
                        equipped = pack.id == settings.selectedColorPackId,
                        onBuy = { viewModel.purchaseColorPack(pack.id) },
                        onEquip = { viewModel.equipColorPack(pack.id) }
                    )
                }

            if (easterEggUnlocked) {
                Text(
                    text = stringResource(R.string.shop_easter_egg_unlocked),
                    color = coinGold,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ShopUpgradeItem(
    name: String,
    level: String,
    cost: Int,
    coins: Long,
    onBuy: () -> Unit
) {
    val ink = MaterialTheme.colorScheme.onBackground
    val canAfford = coins >= cost

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = ink, fontWeight = FontWeight.Medium)
            Text(level, color = ink.copy(alpha = 0.5f), fontSize = 13.sp)
        }
        GameButton(
            text = stringResource(R.string.shop_buy_cost, cost),
            onClick = onBuy,
            enabled = canAfford,
            modifier = Modifier.fillMaxWidth(0.38f)
        )
    }
}

@Composable
private fun ColorPackCard(
    pack: ColorPackDefinition,
    coins: Long,
    owned: Boolean,
    equipped: Boolean,
    onBuy: () -> Unit,
    onEquip: () -> Unit
) {
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = ink.copy(alpha = 0.55f)
    val theme = BoardVisualTheme.fromDefinition(pack)
    val previewHps = listOf(5, 25, 45)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(12.dp)
    ) {
        Text(colorPackName(pack.id), color = ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(colorPackFeature(pack), color = muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))

        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            previewHps.forEach { hp ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(theme.brickColorIntForHp(hp)))
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(theme.ballColor))
            )
        }

        when {
            equipped -> {
                Text(
                    text = stringResource(R.string.shop_equipped),
                    color = muted,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                )
            }
            owned -> {
                GameButton(
                    text = stringResource(R.string.shop_equip),
                    onClick = onEquip,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {
                GameButton(
                    text = stringResource(R.string.shop_buy_cost, pack.price),
                    onClick = onBuy,
                    enabled = coins >= pack.price,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun colorPackName(id: String): String = when (id) {
    ColorPackIds.CLASSIC -> stringResource(R.string.color_pack_classic)
    ColorPackIds.NEON_NIGHT -> stringResource(R.string.color_pack_neon_night)
    ColorPackIds.SUNSET_BLAZE -> stringResource(R.string.color_pack_sunset_blaze)
    ColorPackIds.OCEAN_DEPTH -> stringResource(R.string.color_pack_ocean_depth)
    ColorPackIds.ROYAL_GOLD -> stringResource(R.string.color_pack_royal_gold)
    ColorPackIds.GALAXY -> stringResource(R.string.color_pack_galaxy)
    ColorPackIds.BLOCK_WORLD -> stringResource(R.string.color_pack_block_world)
    else -> id
}

@Composable
private fun colorPackFeature(pack: ColorPackDefinition): String = when (pack.id) {
    ColorPackIds.CLASSIC -> stringResource(R.string.color_pack_desc_classic)
    ColorPackIds.NEON_NIGHT -> stringResource(R.string.color_pack_desc_neon_night)
    ColorPackIds.SUNSET_BLAZE -> stringResource(R.string.color_pack_desc_sunset_blaze)
    ColorPackIds.OCEAN_DEPTH -> stringResource(R.string.color_pack_desc_ocean_depth)
    ColorPackIds.ROYAL_GOLD -> stringResource(R.string.color_pack_desc_royal_gold)
    ColorPackIds.GALAXY -> stringResource(R.string.color_pack_desc_galaxy)
    ColorPackIds.BLOCK_WORLD -> stringResource(R.string.color_pack_desc_block_world)
    else -> stringResource(R.string.color_pack_desc_classic)
}
