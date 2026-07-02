package com.mostafa.brickblast.ui.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mostafa.brickblast.R
import com.mostafa.brickblast.domain.model.Achievement
import com.mostafa.brickblast.domain.model.GameMode

@Composable
fun gameModeLabel(mode: String): String = when (mode) {
    GameMode.CLASSIC.name -> stringResource(R.string.mode_classic)
    GameMode.CHALLENGE.name -> stringResource(R.string.mode_challenge)
    GameMode.TIME_ATTACK.name -> stringResource(R.string.mode_time_attack)
    GameMode.HARDCORE.name -> stringResource(R.string.mode_hardcore)
    else -> mode.replace('_', ' ')
}

fun gameModeLabel(context: Context, mode: String): String = when (mode) {
    GameMode.CLASSIC.name -> context.getString(R.string.mode_classic)
    GameMode.CHALLENGE.name -> context.getString(R.string.mode_challenge)
    GameMode.TIME_ATTACK.name -> context.getString(R.string.mode_time_attack)
    GameMode.HARDCORE.name -> context.getString(R.string.mode_hardcore)
    else -> mode.replace('_', ' ')
}

@StringRes
fun achievementTitleRes(id: String): Int = when (id) {
    "destroy_100_bricks" -> R.string.achievement_brick_breaker_title
    "reach_round_50" -> R.string.achievement_half_century_title
    "collect_500_coins" -> R.string.achievement_coin_collector_title
    "own_50_balls" -> R.string.achievement_ball_hoarder_title
    else -> R.string.achievement_brick_breaker_title
}

@StringRes
fun achievementDescriptionRes(id: String): Int = when (id) {
    "destroy_100_bricks" -> R.string.achievement_brick_breaker_desc
    "reach_round_50" -> R.string.achievement_half_century_desc
    "collect_500_coins" -> R.string.achievement_coin_collector_desc
    "own_50_balls" -> R.string.achievement_ball_hoarder_desc
    else -> R.string.achievement_brick_breaker_desc
}

@Composable
fun Achievement.localizedTitle(): String = stringResource(achievementTitleRes(id))

@Composable
fun Achievement.localizedDescription(): String = stringResource(achievementDescriptionRes(id))

fun Achievement.localizedTitle(context: Context): String =
    context.getString(achievementTitleRes(id))

fun Achievement.localizedDescription(context: Context): String =
    context.getString(achievementDescriptionRes(id))
