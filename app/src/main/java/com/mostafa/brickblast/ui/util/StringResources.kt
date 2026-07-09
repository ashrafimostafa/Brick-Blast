package com.mostafa.brickblast.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mostafa.brickblast.R
import com.mostafa.brickblast.domain.model.Achievement
import com.mostafa.brickblast.domain.model.AchievementMetric
import com.mostafa.brickblast.domain.model.GameMode
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

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

fun formatAchievementCount(value: Long): String = when {
    value >= 1_000_000L -> {
        val millions = value / 1_000_000.0
        if (millions == millions.roundToInt().toDouble()) "${millions.roundToInt()}M"
        else String.format(Locale.US, "%.1fM", millions)
    }
    value >= 1_000L -> {
        val thousands = value / 1_000.0
        if (thousands == thousands.roundToInt().toDouble()) "${thousands.roundToInt()}K"
        else String.format(Locale.US, "%.1fK", thousands)
    }
    else -> NumberFormat.getIntegerInstance().format(value)
}

@Composable
fun formatPlayTimeShort(ms: Long): String {
    val totalMinutes = (ms / 60_000L).coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours >= 24 -> stringResource(R.string.play_time_days_hours, hours / 24, hours % 24)
        hours > 0 -> stringResource(R.string.play_time_hours_minutes, hours, minutes)
        else -> stringResource(R.string.play_time_minutes, minutes.coerceAtLeast(1))
    }
}

fun formatPlayTimeShort(context: Context, ms: Long): String {
    val totalMinutes = (ms / 60_000L).coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours >= 24 -> context.getString(R.string.play_time_days_hours, hours / 24, hours % 24)
        hours > 0 -> context.getString(R.string.play_time_hours_minutes, hours, minutes)
        else -> context.getString(R.string.play_time_minutes, minutes.coerceAtLeast(1))
    }
}

@Composable
fun Achievement.localizedTitle(): String = localizedTitle(metric, target)

@Composable
fun Achievement.localizedDescription(): String = localizedDescription(metric, target)

fun Achievement.localizedTitle(context: Context): String =
    localizedTitle(context, metric, target)

fun Achievement.localizedDescription(context: Context): String =
    localizedDescription(context, metric, target)

@Composable
private fun localizedTitle(metric: AchievementMetric, target: Long): String = when (metric) {
    AchievementMetric.ROUND -> when (target) {
        1_000_000L -> stringResource(R.string.achievement_round_million_title)
        else -> stringResource(R.string.achievement_round_title, formatAchievementCount(target))
    }
    AchievementMetric.BRICKS -> stringResource(
        R.string.achievement_bricks_title,
        formatAchievementCount(target)
    )
    AchievementMetric.COINS -> stringResource(
        R.string.achievement_coins_title,
        formatAchievementCount(target)
    )
    AchievementMetric.BALLS -> stringResource(
        R.string.achievement_balls_title,
        formatAchievementCount(target)
    )
    AchievementMetric.PLAY_TIME -> stringResource(
        R.string.achievement_playtime_title,
        formatPlayTimeShort(target)
    )
    AchievementMetric.GAMES -> stringResource(
        R.string.achievement_games_title,
        formatAchievementCount(target)
    )
}

@Composable
private fun localizedDescription(metric: AchievementMetric, target: Long): String = when (metric) {
    AchievementMetric.ROUND -> when (target) {
        1_000_000L -> stringResource(R.string.achievement_round_million_desc)
        else -> stringResource(R.string.achievement_round_desc, formatAchievementCount(target))
    }
    AchievementMetric.BRICKS -> stringResource(
        R.string.achievement_bricks_desc,
        formatAchievementCount(target)
    )
    AchievementMetric.COINS -> stringResource(
        R.string.achievement_coins_desc,
        formatAchievementCount(target)
    )
    AchievementMetric.BALLS -> stringResource(
        R.string.achievement_balls_desc,
        formatAchievementCount(target)
    )
    AchievementMetric.PLAY_TIME -> stringResource(
        R.string.achievement_playtime_desc,
        formatPlayTimeShort(target)
    )
    AchievementMetric.GAMES -> stringResource(
        R.string.achievement_games_desc,
        formatAchievementCount(target)
    )
}

private fun localizedTitle(context: Context, metric: AchievementMetric, target: Long): String =
    when (metric) {
        AchievementMetric.ROUND -> when (target) {
            1_000_000L -> context.getString(R.string.achievement_round_million_title)
            else -> context.getString(R.string.achievement_round_title, formatAchievementCount(target))
        }
        AchievementMetric.BRICKS -> context.getString(
            R.string.achievement_bricks_title,
            formatAchievementCount(target)
        )
        AchievementMetric.COINS -> context.getString(
            R.string.achievement_coins_title,
            formatAchievementCount(target)
        )
        AchievementMetric.BALLS -> context.getString(
            R.string.achievement_balls_title,
            formatAchievementCount(target)
        )
        AchievementMetric.PLAY_TIME -> context.getString(
            R.string.achievement_playtime_title,
            formatPlayTimeShort(context, target)
        )
        AchievementMetric.GAMES -> context.getString(
            R.string.achievement_games_title,
            formatAchievementCount(target)
        )
    }

private fun localizedDescription(context: Context, metric: AchievementMetric, target: Long): String =
    when (metric) {
        AchievementMetric.ROUND -> when (target) {
            1_000_000L -> context.getString(R.string.achievement_round_million_desc)
            else -> context.getString(R.string.achievement_round_desc, formatAchievementCount(target))
        }
        AchievementMetric.BRICKS -> context.getString(
            R.string.achievement_bricks_desc,
            formatAchievementCount(target)
        )
        AchievementMetric.COINS -> context.getString(
            R.string.achievement_coins_desc,
            formatAchievementCount(target)
        )
        AchievementMetric.BALLS -> context.getString(
            R.string.achievement_balls_desc,
            formatAchievementCount(target)
        )
        AchievementMetric.PLAY_TIME -> context.getString(
            R.string.achievement_playtime_desc,
            formatPlayTimeShort(context, target)
        )
        AchievementMetric.GAMES -> context.getString(
            R.string.achievement_games_desc,
            formatAchievementCount(target)
        )
    }

fun achievementMetricEmoji(metric: AchievementMetric): String = when (metric) {
    AchievementMetric.ROUND -> "🏁"
    AchievementMetric.BRICKS -> "🧱"
    AchievementMetric.COINS -> "🪙"
    AchievementMetric.BALLS -> "⚪"
    AchievementMetric.PLAY_TIME -> "⏱️"
    AchievementMetric.GAMES -> "🎮"
}
