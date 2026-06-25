package com.mostafa.brickblast.domain.model

data class PlayerUpgrades(
    val ballDamageLevel: Int = 1,
    val startingBallsLevel: Int = 1,
    val coinMultiplierLevel: Int = 1,
    val criticalHitLevel: Int = 1
) {
    fun ballDamage(): Int = ballDamageLevel
    fun startingBalls(): Int = 1 + (startingBallsLevel - 1)
    fun coinMultiplier(): Float = 1f + (coinMultiplierLevel - 1) * 0.25f
    fun criticalHitChance(): Float = (criticalHitLevel - 1) * 0.05f

    fun upgradeCost(type: UpgradeType): Int = when (type) {
        UpgradeType.BALL_DAMAGE -> ballDamageLevel * 100
        UpgradeType.STARTING_BALLS -> startingBallsLevel * 150
        UpgradeType.COIN_MULTIPLIER -> coinMultiplierLevel * 200
        UpgradeType.CRITICAL_HIT -> criticalHitLevel * 250
    }
}

enum class UpgradeType {
    BALL_DAMAGE,
    STARTING_BALLS,
    COIN_MULTIPLIER,
    CRITICAL_HIT
}

data class PlayerStatistics(
    val highestRound: Int = 0,
    val totalBricksDestroyed: Long = 0,
    val totalBallsLaunched: Long = 0,
    val totalPlayTimeMs: Long = 0,
    val totalCoinsEarned: Long = 0,
    val totalGamesPlayed: Int = 0
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val target: Long,
    var progress: Long = 0,
    var unlocked: Boolean = false,
    val unlockedAt: Long? = null
)

object AchievementDefinitions {
    val ALL = listOf(
        Achievement("destroy_100_bricks", "Brick Breaker", "Destroy 100 bricks", 100),
        Achievement("reach_round_50", "Half Century", "Reach Round 50", 50),
        Achievement("collect_500_coins", "Coin Collector", "Collect 500 coins", 500),
        Achievement("own_50_balls", "Ball Hoarder", "Own 50 balls", 50)
    )
}

data class GameSaveState(
    val round: Int = 1,
    val score: Int = 0,
    val totalBalls: Int = 1,
    val coinsThisSession: Int = 0,
    val mode: GameMode = GameMode.CLASSIC,
    val bricksJson: String = "",
    val collectablesJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class AppSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val showTrajectory: Boolean = true,
    val particleEffects: Boolean = true,
    val darkTheme: Boolean = true
)
