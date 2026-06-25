package com.mostafa.brickblast.domain.model

/**
 * Core brick entity used by the physics engine and renderer.
 */
data class Brick(
    val id: Long,
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    var hp: Int,
    val maxHp: Int = hp,
    var destroyAnimProgress: Float = 0f,
    var isDestroying: Boolean = false
) {
    // Transient marker used by the spatial grid for O(1) duplicate filtering
    // during a query. Not part of equality/copy semantics.
    @JvmField var collisionStamp: Int = 0
}
