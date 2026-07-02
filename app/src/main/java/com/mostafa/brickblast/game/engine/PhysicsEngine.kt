package com.mostafa.brickblast.game.engine

import com.mostafa.brickblast.domain.model.Ball
import com.mostafa.brickblast.domain.model.Brick
import com.mostafa.brickblast.domain.model.Collectable
import kotlin.math.sqrt

/**
 * Fixed-timestep physics engine targeting 60 FPS simulation.
 * Uses delta-time accumulation and avoids allocations in the hot loop.
 */
class PhysicsEngine(
    private val spatialGrid: SpatialHashGrid = SpatialHashGrid(cellSize = 120f)
) {
    companion object {
        const val FIXED_DT = 1f / 60f
        const val BALL_SPEED = 1320f
        const val MAX_SUBSTEPS = 3

        val MIN_ANGLE = Math.toRadians(12.0).toFloat()
    }

    private val nearbyBuffer = ArrayList<Brick>(12)
    private val collisionScratch = CollisionScratch()
    private val bestCollision = CollisionScratch()
    private val wallNormalX = FloatArray(1)
    private val wallNormalY = FloatArray(1)
    private val trajectoryBuffer = FloatArray(80)

    var timeScale: Float = 1f

    private var accumulator = 0f
    private var lastBrickLayoutGeneration = -1

    fun resetAccumulator() {
        accumulator = 0f
    }

    fun update(
        balls: MutableList<Ball>,
        bricks: MutableList<Brick>,
        collectables: MutableList<Collectable>,
        bounds: GameBounds,
        brickLayoutGeneration: Int,
        activeBallCount: Int,
        onBrickHit: (Brick, Ball) -> Unit,
        onBrickDestroyed: (Brick) -> Unit,
        onBallBounce: (Ball) -> Unit,
        onCollectableHit: (Collectable, Ball) -> Unit,
        deltaTime: Float
    ): Boolean {
        accumulator += deltaTime

        if (brickLayoutGeneration != lastBrickLayoutGeneration) {
            rebuildSpatialGrid(bricks)
            lastBrickLayoutGeneration = brickLayoutGeneration
        }

        val activeAtStart = activeBallCount
        val maxSteps = when {
            activeAtStart > 100 -> 1
            activeAtStart > 60 -> 2
            activeAtStart > 30 -> 2
            else -> MAX_SUBSTEPS
        }

        var steps = 0
        while (accumulator >= FIXED_DT && steps < maxSteps) {
            accumulator -= FIXED_DT
            step(
                balls, collectables, bounds, activeAtStart,
                onBrickHit, onBrickDestroyed, onBallBounce, onCollectableHit
            )
            steps++
        }

        if (accumulator > FIXED_DT * maxSteps) accumulator = 0f

        return hasAnyActiveBall(balls)
    }

    private fun hasAnyActiveBall(balls: MutableList<Ball>): Boolean {
        for (i in balls.indices) {
            val b = balls[i]
            if (b.active && b.launched) return true
        }
        return false
    }

    private fun step(
        balls: MutableList<Ball>,
        collectables: MutableList<Collectable>,
        bounds: GameBounds,
        activeBallCount: Int,
        onBrickHit: (Brick, Ball) -> Unit,
        onBrickDestroyed: (Brick) -> Unit,
        onBallBounce: (Ball) -> Unit,
        onCollectableHit: (Collectable, Ball) -> Unit
    ) {
        val maxMicroSteps = when {
            activeBallCount > 80 -> 1
            activeBallCount > 40 -> 2
            else -> 3
        }

        for (i in balls.indices) {
            val ball = balls[i]
            if (!ball.active || !ball.launched) continue

            moveBallSwept(
                ball, bounds, maxMicroSteps,
                onBrickHit, onBrickDestroyed, onBallBounce
            )

            if (ball.active) {
                checkCollectables(ball, collectables, onCollectableHit)
            }
        }
    }

    private fun moveBallSwept(
        ball: Ball,
        bounds: GameBounds,
        maxMicroSteps: Int,
        onBrickHit: (Brick, Ball) -> Unit,
        onBrickDestroyed: (Brick) -> Unit,
        onBallBounce: (Ball) -> Unit
    ) {
        val dx = ball.vx * FIXED_DT
        val dy = ball.vy * FIXED_DT
        val dist = sqrt(dx * dx + dy * dy)
        val maxStep = ball.radius
        val subSteps = maxOf(1, kotlin.math.ceil(dist / maxStep).toInt().coerceAtMost(maxMicroSteps))
        val subDt = FIXED_DT / subSteps

        for (s in 0 until subSteps) {
            if (!ball.active) return

            ball.x += ball.vx * subDt
            ball.y += ball.vy * subDt

            if (ball.y + ball.radius >= bounds.bottom) {
                ball.y = bounds.bottom - ball.radius
                ball.vx = 0f
                ball.vy = 0f
                ball.active = false
                return
            }

            if (CollisionDetector.checkWallCollision(
                    ball, bounds.left, bounds.right, bounds.top, wallNormalX, wallNormalY
                )
            ) {
                CollisionDetector.reflectOffWall(ball, wallNormalX[0], wallNormalY[0])
                onBallBounce(ball)
            }

            var hasCollision = false
            spatialGrid.queryInto(ball.x, ball.y, ball.radius + 4f, nearbyBuffer)
            for (k in nearbyBuffer.indices) {
                if (!CollisionDetector.checkBrickCollision(ball, nearbyBuffer[k], collisionScratch)) {
                    continue
                }
                if (!hasCollision || collisionScratch.penetration > bestCollision.penetration) {
                    bestCollision.brick = collisionScratch.brick
                    bestCollision.normalX = collisionScratch.normalX
                    bestCollision.normalY = collisionScratch.normalY
                    bestCollision.penetration = collisionScratch.penetration
                    hasCollision = true
                }
            }
            if (hasCollision) {
                val hitBrick = bestCollision.brick!!
                CollisionDetector.resolveCollision(ball, bestCollision)
                if (ball.lastHitBrickId != hitBrick.id) {
                    ball.lastHitBrickId = hitBrick.id
                    onBrickHit(hitBrick, ball)
                    if (hitBrick.hp <= 0 && !hitBrick.isDestroying) {
                        onBrickDestroyed(hitBrick)
                    }
                }
                onBallBounce(ball)
            } else {
                ball.lastHitBrickId = -1L
            }
        }
    }

    private fun checkCollectables(
        ball: Ball,
        collectables: MutableList<Collectable>,
        onCollectableHit: (Collectable, Ball) -> Unit
    ) {
        for (j in collectables.indices) {
            val c = collectables[j]
            if (c.collected) continue
            val dx = ball.x - c.x
            val dy = ball.y - c.y
            val distSq = dx * dx + dy * dy
            val r = ball.radius + c.radius
            if (distSq <= r * r) {
                c.collected = true
                onCollectableHit(c, ball)
            }
        }
    }

    private fun rebuildSpatialGrid(bricks: MutableList<Brick>) {
        spatialGrid.clear()
        for (brick in bricks) {
            if (brick.hp > 0 && !brick.isDestroying) {
                spatialGrid.insert(brick)
            }
        }
    }

    fun launchBall(ball: Ball, angleRadians: Float, speed: Float = BALL_SPEED) {
        ball.vx = speed * kotlin.math.cos(angleRadians)
        ball.vy = speed * kotlin.math.sin(angleRadians)
        ball.launched = true
        ball.active = true
    }

    fun computeLaunchAngle(fromX: Float, fromY: Float, toX: Float, toY: Float): Float {
        val dx = toX - fromX
        var dy = toY - fromY
        if (dy > -1f) dy = -1f
        var angle = kotlin.math.atan2(dy, dx)
        val pi = Math.PI.toFloat()
        angle = angle.coerceIn(-pi + MIN_ANGLE, -MIN_ANGLE)
        return angle
    }

    fun predictTrajectory(
        startX: Float, startY: Float,
        angle: Float,
        bounds: GameBounds,
        steps: Int = 28
    ): FloatArray {
        val points = trajectoryBuffer
        val maxPoints = points.size / 2
        val limit = minOf(steps, maxPoints)
        var x = startX
        var y = startY
        val speed = BALL_SPEED
        var vx = speed * kotlin.math.cos(angle)
        var vy = speed * kotlin.math.sin(angle)
        val dt = 0.02f

        for (i in 0 until limit) {
            x += vx * dt
            y += vy * dt

            if (x - 8f < bounds.left) { x = bounds.left + 8f; vx = -vx }
            if (x + 8f > bounds.right) { x = bounds.right - 8f; vx = -vx }
            if (y - 8f < bounds.top) { y = bounds.top + 8f; vy = -vy }

            points[i * 2] = x
            points[i * 2 + 1] = y

            if (y > bounds.bottom) {
                for (j in (i + 1) until limit) {
                    points[j * 2] = 0f
                    points[j * 2 + 1] = 0f
                }
                break
            }
        }
        return points
    }
}

data class GameBounds(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float,
    val bottom: Float,
    val brickAreaTop: Float,
    val launcherY: Float
)
