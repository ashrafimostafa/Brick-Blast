package com.mostafa.brickblast.game.engine

import com.mostafa.brickblast.domain.model.Ball
import com.mostafa.brickblast.domain.model.Brick
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Result of a ball-brick collision including the surface normal for reflection.
 */
data class CollisionResult(
    val brick: Brick,
    val normalX: Float,
    val normalY: Float,
    val penetration: Float
)

/**
 * Spatial hash grid for efficient broad-phase collision detection.
 * Supports 500+ bricks and 300+ balls at 60 FPS.
 */
class SpatialHashGrid(
    private val cellSize: Float = 80f
) {
    private val grid = HashMap<Long, MutableList<Brick>>()
    private var queryStamp = 0

    fun clear() = grid.clear()

    fun insert(brick: Brick) {
        // Insert the brick into EVERY cell its rectangle overlaps. A brick can be
        // wider/taller than one cell, so registering only its center cell would
        // make edge collisions miss (ball appears to pass through the brick).
        val minCx = (brick.x / cellSize).toInt()
        val maxCx = ((brick.x + brick.width) / cellSize).toInt()
        val minCy = (brick.y / cellSize).toInt()
        val maxCy = ((brick.y + brick.height) / cellSize).toInt()
        for (cx in minCx..maxCx) {
            for (cy in minCy..maxCy) {
                grid.getOrPut(packCell(cx, cy)) { ArrayList(4) }.add(brick)
            }
        }
    }

    /**
     * Fills [out] with bricks whose cells overlap the query area. Uses a per-query
     * stamp on each brick for O(1) duplicate filtering (a brick can live in many
     * cells), avoiding the O(n^2) contains() scan that bogged down large boards.
     */
    fun queryInto(x: Float, y: Float, radius: Float, out: MutableList<Brick>) {
        out.clear()
        queryStamp++
        val minCx = ((x - radius) / cellSize).toInt()
        val maxCx = ((x + radius) / cellSize).toInt()
        val minCy = ((y - radius) / cellSize).toInt()
        val maxCy = ((y + radius) / cellSize).toInt()
        for (cx in minCx..maxCx) {
            for (cy in minCy..maxCy) {
                val cell = grid[packCell(cx, cy)] ?: continue
                for (brick in cell) {
                    if (brick.collisionStamp != queryStamp) {
                        brick.collisionStamp = queryStamp
                        out.add(brick)
                    }
                }
            }
        }
    }

    private fun cellKey(x: Float, y: Float): Long = packCell(
        (x / cellSize).toInt(),
        (y / cellSize).toInt()
    )

    private fun packCell(cx: Int, cy: Int): Long =
        (cx.toLong() shl 32) or (cy.toLong() and 0xFFFFFFFFL)
}

/**
 * Handles precise ball-brick and ball-wall collision detection and response.
 */
object CollisionDetector {

    fun checkWallCollision(
        ball: Ball,
        left: Float,
        right: Float,
        top: Float
    ): Pair<Float, Float>? {
        var nx = 0f
        var ny = 0f

        if (ball.x - ball.radius < left) {
            ball.x = left + ball.radius
            nx = 1f
        } else if (ball.x + ball.radius > right) {
            ball.x = right - ball.radius
            nx = -1f
        }

        if (ball.y - ball.radius < top) {
            ball.y = top + ball.radius
            ny = 1f
        }

        return if (nx != 0f || ny != 0f) Pair(nx, ny) else null
    }

    fun checkBrickCollision(ball: Ball, brick: Brick): CollisionResult? {
        if (brick.hp <= 0 || brick.isDestroying) return null

        val closestX = ball.x.coerceIn(brick.x, brick.x + brick.width)
        val closestY = ball.y.coerceIn(brick.y, brick.y + brick.height)

        val dx = ball.x - closestX
        val dy = ball.y - closestY
        val distSq = dx * dx + dy * dy

        if (distSq >= ball.radius * ball.radius) return null

        val dist = sqrt(distSq)
        val normalX: Float
        val normalY: Float
        val penetration: Float

        if (dist < 0.001f) {
            // Ball center inside brick - resolve by smallest overlap axis
            val overlapLeft = ball.x + ball.radius - brick.x
            val overlapRight = brick.x + brick.width - (ball.x - ball.radius)
            val overlapTop = ball.y + ball.radius - brick.y
            val overlapBottom = brick.y + brick.height - (ball.y - ball.radius)
            val minOverlap = minOf(overlapLeft, overlapRight, overlapTop, overlapBottom)
            when (minOverlap) {
                overlapLeft -> { normalX = -1f; normalY = 0f; penetration = overlapLeft }
                overlapRight -> { normalX = 1f; normalY = 0f; penetration = overlapRight }
                overlapTop -> { normalX = 0f; normalY = -1f; penetration = overlapTop }
                else -> { normalX = 0f; normalY = 1f; penetration = overlapBottom }
            }
        } else {
            normalX = dx / dist
            normalY = dy / dist
            penetration = ball.radius - dist
        }

        return CollisionResult(brick, normalX, normalY, penetration)
    }

    /**
     * Reflect velocity off the collision normal. Prevents sticking by pushing ball out.
     */
    fun resolveCollision(ball: Ball, result: CollisionResult) {
        ball.x += result.normalX * result.penetration + result.normalX * 0.5f
        ball.y += result.normalY * result.penetration + result.normalY * 0.5f

        val dot = ball.vx * result.normalX + ball.vy * result.normalY
        if (dot < 0) {
            ball.vx -= 2 * dot * result.normalX
            ball.vy -= 2 * dot * result.normalY
        }

        // Minimum velocity to prevent horizontal/vertical sticking
        val speed = sqrt(ball.vx * ball.vx + ball.vy * ball.vy)
        if (speed > 0.01f) {
            if (abs(ball.vx) < 30f) ball.vx = if (ball.vx >= 0) 30f else -30f
            if (abs(ball.vy) < 30f) ball.vy = if (ball.vy >= 0) 30f else -30f
        }
    }

    fun reflectOffWall(ball: Ball, normalX: Float, normalY: Float) {
        if (normalX != 0f) ball.vx = abs(ball.vx) * normalX
        if (normalY != 0f) ball.vy = abs(ball.vy) * normalY
    }
}
