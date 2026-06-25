package com.example.brickblast.game.engine

import com.example.brickblast.domain.model.Ball
import com.example.brickblast.domain.model.Brick
import com.example.brickblast.domain.model.Collectable
import com.example.brickblast.domain.model.PowerUpType
import kotlin.math.sqrt

/**
 * Fixed-timestep physics engine targeting 60 FPS simulation.
 * Uses delta-time accumulation and avoids allocations in the hot loop.
 */
class PhysicsEngine(
    private val spatialGrid: SpatialHashGrid = SpatialHashGrid()
) {
    companion object {
        const val FIXED_DT = 1f / 60f
        const val BALL_SPEED = 1200f
        const val MAX_SUBSTEPS = 3

        // Minimum angle above horizontal (~12 degrees) for any launch.
        val MIN_ANGLE = Math.toRadians(12.0).toFloat()
    }

    // Reusable buffers to avoid per-frame allocations in the hot loop.
    private val collisionBuffer = ArrayList<CollisionResult>(4)
    private val nearbyBuffer = ArrayList<Brick>(16)

    var timeScale: Float = 1f

    // Persistent across frames so leftover sub-frame time carries over. A local
    // accumulator would stall when a frame's deltaTime is below FIXED_DT (e.g.
    // a ~16ms frame vs a 16.667ms fixed step), leaving the ball frozen.
    private var accumulator = 0f

    /** Clear leftover simulation time, e.g. when a new shot begins. */
    fun resetAccumulator() {
        accumulator = 0f
    }

    /**
     * Advance simulation by deltaTime seconds using fixed substeps.
     * Returns true while at least one launched ball is still in flight.
     */
    fun update(
        balls: MutableList<Ball>,
        bricks: MutableList<Brick>,
        collectables: MutableList<Collectable>,
        bounds: GameBounds,
        onBrickHit: (Brick, Ball) -> Unit,
        onBrickDestroyed: (Brick) -> Unit,
        onBallBounce: (Ball) -> Unit,
        onCollectableHit: (Collectable, Ball) -> Unit,
        deltaTime: Float
    ): Boolean {
        accumulator += deltaTime

        // Rebuild once per frame, not once per sub-step.
        rebuildSpatialGrid(bricks)

        var steps = 0
        while (accumulator >= FIXED_DT && steps < MAX_SUBSTEPS) {
            accumulator -= FIXED_DT
            step(
                balls, bricks, collectables, bounds,
                onBrickHit, onBrickDestroyed, onBallBounce, onCollectableHit
            )
            steps++
        }

        // Drop excess time to prevent a spiral of death after a long stall.
        if (accumulator > FIXED_DT * MAX_SUBSTEPS) accumulator = 0f

        // Report activity from the actual ball state (not whether a step ran),
        // so the round does not end during a sub-FIXED_DT gap frame.
        var anyBallActive = false
        for (i in balls.indices) {
            val b = balls[i]
            if (b.active && b.launched) {
                anyBallActive = true
                break
            }
        }
        return anyBallActive
    }

    private fun step(
        balls: MutableList<Ball>,
        bricks: MutableList<Brick>,
        collectables: MutableList<Collectable>,
        bounds: GameBounds,
        onBrickHit: (Brick, Ball) -> Unit,
        onBrickDestroyed: (Brick) -> Unit,
        onBallBounce: (Ball) -> Unit,
        onCollectableHit: (Collectable, Ball) -> Unit
    ): Boolean {
        var anyActive = false

        for (i in balls.indices) {
            val ball = balls[i]
            if (!ball.active || !ball.launched) continue

            moveBallSwept(
                ball, bounds,
                onBrickHit, onBrickDestroyed, onBallBounce
            )

            if (ball.active) {
                anyActive = true
                checkCollectables(ball, collectables, onCollectableHit)
            }
        }
        return anyActive
    }

    /**
     * Moves the ball over one fixed step, but split into micro-steps no larger
     * than half the ball radius. This prevents the ball from tunnelling through
     * or between bricks at high speed (which previously made it slip through a
     * gap and then get destroyed at the bottom).
     */
    private fun moveBallSwept(
        ball: Ball,
        bounds: GameBounds,
        onBrickHit: (Brick, Ball) -> Unit,
        onBrickDestroyed: (Brick) -> Unit,
        onBallBounce: (Ball) -> Unit
    ) {
        val dx = ball.vx * FIXED_DT
        val dy = ball.vy * FIXED_DT
        val dist = sqrt(dx * dx + dy * dy)
        val maxStep = ball.radius
        val subSteps = maxOf(1, kotlin.math.ceil(dist / maxStep).toInt().coerceAtMost(3))
        val subDt = FIXED_DT / subSteps

        for (s in 0 until subSteps) {
            if (!ball.active) return

            ball.x += ball.vx * subDt
            ball.y += ball.vy * subDt

            // Bottom boundary - ball is done and returns to the launcher row.
            if (ball.y + ball.radius >= bounds.bottom) {
                ball.y = bounds.bottom - ball.radius
                ball.vx = 0f
                ball.vy = 0f
                ball.active = false
                return
            }

            val wallNormal = CollisionDetector.checkWallCollision(
                ball, bounds.left, bounds.right, bounds.top
            )
            if (wallNormal != null) {
                CollisionDetector.reflectOffWall(ball, wallNormal.first, wallNormal.second)
                onBallBounce(ball)
            }

            collisionBuffer.clear()
            spatialGrid.queryInto(ball.x, ball.y, ball.radius + 4f, nearbyBuffer)
            for (k in nearbyBuffer.indices) {
                CollisionDetector.checkBrickCollision(ball, nearbyBuffer[k])?.let {
                    collisionBuffer.add(it)
                }
            }
            if (collisionBuffer.isNotEmpty()) {
                var best = collisionBuffer[0]
                for (j in 1 until collisionBuffer.size) {
                    if (collisionBuffer[j].penetration > best.penetration) {
                        best = collisionBuffer[j]
                    }
                }
                CollisionDetector.resolveCollision(ball, best)
                // Apply damage only on a NEW contact so each ball removes exactly
                // 1 HP per touch (one ball -> -1), instead of multiple hits while
                // it overlaps across micro-steps.
                if (ball.lastHitBrickId != best.brick.id) {
                    ball.lastHitBrickId = best.brick.id
                    onBrickHit(best.brick, ball)
                    if (best.brick.hp <= 0 && !best.brick.isDestroying) {
                        onBrickDestroyed(best.brick)
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

    /**
     * Returns a launch angle (radians) that always points upward (negative Y in
     * screen coords) toward the aim point. The angle is clamped so it stays at
     * least MIN_ANGLE above horizontal, guaranteeing the ball travels up toward
     * the bricks instead of skimming sideways.
     */
    fun computeLaunchAngle(fromX: Float, fromY: Float, toX: Float, toY: Float): Float {
        val dx = toX - fromX
        var dy = toY - fromY
        // Force the vertical component upward.
        if (dy > -1f) dy = -1f
        var angle = kotlin.math.atan2(dy, dx) // in (-PI, 0)
        val pi = Math.PI.toFloat()
        // Keep the shot within [-(PI - MIN_ANGLE), -MIN_ANGLE] so it always has a
        // real upward component and never goes flat or downward.
        angle = angle.coerceIn(-pi + MIN_ANGLE, -MIN_ANGLE)
        return angle
    }

    fun predictTrajectory(
        startX: Float, startY: Float,
        angle: Float,
        bounds: GameBounds,
        bricks: List<Brick>,
        steps: Int = 60
    ): FloatArray {
        val points = FloatArray(steps * 2)
        var x = startX
        var y = startY
        val speed = BALL_SPEED
        var vx = speed * kotlin.math.cos(angle)
        var vy = speed * kotlin.math.sin(angle)
        val dt = 0.016f

        for (i in 0 until steps) {
            x += vx * dt
            y += vy * dt

            if (x - 8f < bounds.left) { x = bounds.left + 8f; vx = -vx }
            if (x + 8f > bounds.right) { x = bounds.right - 8f; vx = -vx }
            if (y - 8f < bounds.top) { y = bounds.top + 8f; vy = -vy }

            points[i * 2] = x
            points[i * 2 + 1] = y

            if (y > bounds.bottom) break
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
