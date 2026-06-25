package com.example.brickblast.domain.model

/**
 * Collectable items that spawn from destroyed bricks or rounds.
 */
sealed class Collectable {
    abstract val id: Long
    abstract var x: Float
    abstract var y: Float
    abstract val radius: Float
    abstract var collected: Boolean

    data class ExtraBall(
        override val id: Long,
        override var x: Float,
        override var y: Float,
        override val radius: Float = 14f,
        override var collected: Boolean = false
    ) : Collectable()

    data class Coin(
        override val id: Long,
        override var x: Float,
        override var y: Float,
        override val radius: Float = 12f,
        override var collected: Boolean = false,
        val amount: Int = 1
    ) : Collectable()

    data class PowerUpCollectable(
        override val id: Long,
        override var x: Float,
        override var y: Float,
        override val radius: Float = 16f,
        override var collected: Boolean = false,
        val powerUpType: PowerUpType
    ) : Collectable()
}

enum class PowerUpType {
    MULTI_BALL,
    BOMB,
    LASER,
    SLOW_MOTION,
    DOUBLE_DAMAGE
}
