package com.mostafa.brickblast.game.engine

import com.mostafa.brickblast.domain.model.Brick

/** Reusable collision result to avoid allocations in the physics hot loop. */
class CollisionScratch {
    var brick: Brick? = null
    var normalX: Float = 0f
    var normalY: Float = 0f
    var penetration: Float = 0f

    fun clear() {
        brick = null
        normalX = 0f
        normalY = 0f
        penetration = 0f
    }
}
