package com.example.brickblast.domain.model

/**
 * Ball entity with position, velocity, and collision radius.
 */
data class Ball(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    var active: Boolean = true,
    var launched: Boolean = false
) {
    // Id of the brick currently in contact, so the ball deals exactly one hit of
    // damage per contact (not once per collision micro-step). Reset to -1 when the
    // ball separates from all bricks.
    @JvmField var lastHitBrickId: Long = -1L
}
