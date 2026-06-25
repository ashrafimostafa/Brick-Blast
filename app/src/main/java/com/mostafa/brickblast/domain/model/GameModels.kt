package com.mostafa.brickblast.domain.model

enum class GameMode {
    CLASSIC,
    CHALLENGE,
    TIME_ATTACK,
    HARDCORE
}

enum class GamePhase {
    AIMING,
    LAUNCHING,
    SIMULATING,
    ROUND_END,
    PAUSED,
    GAME_OVER,
    VICTORY
}

data class GameConfig(
    val mode: GameMode = GameMode.CLASSIC,
    val challengeLevel: Int = 1,
    val timeLimitSeconds: Int = 60
)

data class ActivePowerUp(
    val type: PowerUpType,
    var remainingSeconds: Float = 0f,
    var active: Boolean = true
)

data class FloatingText(
    val id: Long,
    val x: Float,
    val y: Float,
    val text: String,
    var alpha: Float = 1f,
    var offsetY: Float = 0f
)
