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
    val metric: AchievementMetric,
    val title: String,
    val description: String,
    val target: Long,
    var progress: Long = 0,
    var unlocked: Boolean = false,
    val unlockedAt: Long? = null
)

data class GameSaveState(
    val round: Int = 1,
    val score: Int = 0,
    val totalBalls: Int = 1,
    val coinsThisSession: Int = 0,
    val mode: GameMode = GameMode.CLASSIC,
    val challengeLevel: Int = 1,
    val timeAttackRemaining: Float = 0f,
    val launcherX: Float = 0f,
    val nextLauncherX: Float = 0f,
    val hasNextLauncher: Boolean = false,
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
    val achievementAutoDismiss: Boolean = true,
    val darkTheme: Boolean = true,
    /** null = follow system language; otherwise BCP-47 tag such as "en" or "fa". */
    val languageTag: String? = null,
    val selectedColorPackId: String = ColorPackIds.CLASSIC,
    val ownedColorPackIds: Set<String> = setOf(ColorPackIds.CLASSIC)
)
