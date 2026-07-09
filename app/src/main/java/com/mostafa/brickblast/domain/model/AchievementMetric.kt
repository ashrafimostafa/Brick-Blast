package com.mostafa.brickblast.domain.model

enum class AchievementMetric {
    BRICKS,
    ROUND,
    COINS,
    BALLS,
    PLAY_TIME,
    GAMES
}

data class AchievementSnapshot(
    val bricksDestroyed: Long = 0,
    val highestRound: Long = 0,
    val coins: Long = 0,
    val ballsOwned: Long = 0,
    val playTimeMs: Long = 0,
    val gamesPlayed: Long = 0
)

data class AchievementDefinition(
    val id: String,
    val metric: AchievementMetric,
    val target: Long
)

object AchievementDefinitions {
    private fun rounds() = longArrayOf(
        10, 25, 50, 100, 200, 500, 1_000, 2_500, 5_000, 10_000,
        25_000, 50_000, 100_000, 250_000, 500_000, 1_000_000
    ).map { AchievementDefinition("round_$it", AchievementMetric.ROUND, it) }

    private fun bricks() = longArrayOf(
        100, 500, 1_000, 2_500, 5_000, 10_000, 25_000, 50_000, 100_000, 250_000, 500_000, 1_000_000
    ).map { AchievementDefinition("bricks_$it", AchievementMetric.BRICKS, it) }

    private fun coins() = longArrayOf(
        100, 500, 1_000, 5_000, 10_000, 50_000, 100_000
    ).map { AchievementDefinition("coins_$it", AchievementMetric.COINS, it) }

    private fun balls() = longArrayOf(
        25, 50, 100, 200, 500
    ).map { AchievementDefinition("balls_$it", AchievementMetric.BALLS, it) }

    /** Targets in minutes of total play time. */
    private fun playTime() = longArrayOf(
        15, 30, 60, 180, 300, 600, 1_440
    ).map { minutes ->
        AchievementDefinition("playtime_$minutes", AchievementMetric.PLAY_TIME, minutes * 60_000L)
    }

    private fun games() = longArrayOf(
        5, 10, 25, 50, 100, 500, 1_000
    ).map { AchievementDefinition("games_$it", AchievementMetric.GAMES, it) }

    val ALL: List<AchievementDefinition> =
        rounds() + bricks() + coins() + balls() + playTime() + games()

    private val LEGACY_ID_MAP = mapOf(
        "destroy_100_bricks" to "bricks_100",
        "reach_round_50" to "round_50",
        "collect_500_coins" to "coins_500",
        "own_50_balls" to "balls_50"
    )

    fun metricFor(id: String): AchievementMetric = when {
        id.startsWith("round_") || id == "reach_round_50" -> AchievementMetric.ROUND
        id.startsWith("bricks_") || id == "destroy_100_bricks" -> AchievementMetric.BRICKS
        id.startsWith("coins_") || id == "collect_500_coins" -> AchievementMetric.COINS
        id.startsWith("balls_") || id == "own_50_balls" -> AchievementMetric.BALLS
        id.startsWith("playtime_") -> AchievementMetric.PLAY_TIME
        id.startsWith("games_") -> AchievementMetric.GAMES
        else -> AchievementMetric.ROUND
    }

    fun canonicalId(id: String): String = LEGACY_ID_MAP[id] ?: id

    fun progressFor(metric: AchievementMetric, snapshot: AchievementSnapshot): Long = when (metric) {
        AchievementMetric.BRICKS -> snapshot.bricksDestroyed
        AchievementMetric.ROUND -> snapshot.highestRound
        AchievementMetric.COINS -> snapshot.coins
        AchievementMetric.BALLS -> snapshot.ballsOwned
        AchievementMetric.PLAY_TIME -> snapshot.playTimeMs
        AchievementMetric.GAMES -> snapshot.gamesPlayed
    }

    fun toAchievement(def: AchievementDefinition): Achievement = Achievement(
        id = def.id,
        metric = def.metric,
        title = "",
        description = "",
        target = def.target
    )
}
