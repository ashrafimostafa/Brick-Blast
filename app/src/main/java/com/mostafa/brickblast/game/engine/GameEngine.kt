package com.mostafa.brickblast.game.engine

import com.mostafa.brickblast.domain.model.ActivePowerUp
import com.mostafa.brickblast.domain.model.Ball
import com.mostafa.brickblast.domain.model.Brick
import com.mostafa.brickblast.domain.model.Collectable
import com.mostafa.brickblast.domain.model.FloatingText
import com.mostafa.brickblast.domain.model.GameConfig
import com.mostafa.brickblast.domain.model.GameMode
import com.mostafa.brickblast.domain.model.GamePhase
import com.mostafa.brickblast.domain.model.PlayerUpgrades
import com.mostafa.brickblast.domain.model.PowerUpType
import com.mostafa.brickblast.game.particle.ParticleSystem
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Central game engine orchestrating turn-based gameplay, physics, powerups, and progression.
 * Designed for 300+ balls and 500+ bricks at 60 FPS with minimal allocations.
 */
class GameEngine {

    val physics = PhysicsEngine()
    val particles = ParticleSystem(256)

    var particleEffectsEnabled = true

    val bricks = ArrayList<Brick>(64)
    val balls = ArrayList<Ball>(32)
    val collectables = ArrayList<Collectable>(16)
    val floatingTexts = ArrayList<FloatingText>(8)
    val activePowerUps = ArrayList<ActivePowerUp>(4)

    var bounds = GameBounds(right = 1080f, bottom = 1920f, brickAreaTop = 200f, launcherY = 1800f)

    var phase = GamePhase.AIMING
    var round = 1
    var score = 0
    var totalBalls = 1
    var bestScore = 0
    var coinsThisSession = 0
    var bricksDestroyedThisRound = 0
    var bricksDestroyedTotal = 0L
    var ballsLaunchedTotal = 0L
    var config = GameConfig()
    var upgrades = PlayerUpgrades()
    var coinMultiplier: Float = 1f

    var aimStartX = 0f
    var aimStartY = 0f
    var aimEndX = 0f
    var aimEndY = 0f
    var isAiming = false

    var launcherX = 540f
    var launcherY = 1800f
    // Where the launcher will move to next round = where the first ball returns.
    var nextLauncherX = 540f
    var hasNextLauncher = false
    private var launcherAnimFrom = 540f
    private var launcherAnimTo = 540f
    private var launcherAnimT = 1f
    private var launcherAnimating = false
  private var nextBallId = 0
  private var nextBrickId = 1L
  private var nextCollectableId = 1L
  private var nextFloatingTextId = 1L
  private var launchQueueIndex = 0
  private var launchTimer = 0f
    var timeAttackRemaining = 60f
        private set
  private var sessionStartTime = System.currentTimeMillis()
  private var doubleDamageActive = false
  private var damageMultiplier = 1

    // Layout constants
    var brickWidth = 0f
    var brickHeight = 0f
    var cols = 6
    var rowsVisible = 10
    // One ball-diameter of travel between launches so the stream reads as
    // separate balls (like Swipe Brick Breaker). Physics must run during
    // LAUNCHING so earlier balls move ahead before the next one fires.
    private val launchDelay: Float
        get() = (26f / PhysicsEngine.BALL_SPEED).coerceIn(0.016f, 0.024f)

    var onBrickDestroyed: ((Brick) -> Unit)? = null
    var onBallBounce: (() -> Unit)? = null
    var onCollect: ((Collectable) -> Unit)? = null
    var onShoot: (() -> Unit)? = null
    var onGameOver: (() -> Unit)? = null
    var onRoundComplete: ((Int) -> Unit)? = null
    var onPowerUpActivated: ((PowerUpType) -> Unit)? = null

    fun initGame(
        screenWidth: Float,
        screenHeight: Float,
        gameConfig: GameConfig,
        playerUpgrades: PlayerUpgrades,
        restoredRound: Int = 1,
        restoredBalls: Int = -1,
        restoredScore: Int = 0
    ) {
        config = gameConfig
        upgrades = playerUpgrades
        coinMultiplier = playerUpgrades.coinMultiplier()
        // Centered play field: equal vertical margins (top margin also hosts the
        // HUD) and equal horizontal margins, so the board sits in the page center.
        val fieldTop = screenHeight * 0.19f
        val fieldBottom = screenHeight * 0.81f
        bounds = GameBounds(
            left = screenWidth * 0.06f,
            top = fieldTop,
            right = screenWidth * 0.94f,
            bottom = fieldBottom,
            brickAreaTop = fieldTop + 64f,
            launcherY = fieldBottom - 32f
        )
        launcherX = screenWidth / 2f
        launcherY = bounds.launcherY
        nextLauncherX = launcherX
        hasNextLauncher = false
        launcherAnimating = false
        launcherAnimT = 1f
        brickWidth = (bounds.right - bounds.left - (cols - 1) * 6f) / cols
        brickHeight = brickWidth * 0.7f

        round = if (gameConfig.mode == GameMode.CHALLENGE) gameConfig.challengeLevel else restoredRound
        score = restoredScore
        totalBalls = if (restoredBalls > 0) restoredBalls else playerUpgrades.startingBalls()
        timeAttackRemaining = gameConfig.timeLimitSeconds.toFloat()
        sessionStartTime = System.currentTimeMillis()

        bricks.clear()
        balls.clear()
        collectables.clear()
        floatingTexts.clear()
        activePowerUps.clear()
        particles.clear()
        nextBallId = 0
        nextBrickId = 1L
        damageMultiplier = playerUpgrades.ballDamage()
        coinsThisSession = 0
        physics.resetAccumulator()
        physics.timeScale = 1f
        doubleDamageActive = false

        if (gameConfig.mode == GameMode.CHALLENGE) {
            val rows = (2 + gameConfig.challengeLevel / 12).coerceIn(2, 12)
            repeat(rows) {
                moveBricksDown()
                spawnTopRow()
            }
        } else if (restoredRound <= 1) {
            spawnInitialBricks()
        } else {
            repeat(minOf(restoredRound, 5)) {
                moveBricksDown()
                spawnTopRow()
            }
        }

        resetBallsForAim()
        phase = GamePhase.AIMING
    }

    private fun spawnInitialBricks() {
        // Gentle start: two sparse rows. Difficulty ramps as rows accumulate
        // and brick HP scales with the round number.
        repeat(2) {
            moveBricksDown()
            spawnTopRow()
        }
    }

    /**
     * Brick HP scales with the round for a smooth difficulty curve.
     * Early rounds are easy (round 1 -> 1..3 HP) and grow steadily.
     */
    private fun brickHpForRound(): Int {
        if (config.mode == GameMode.CHALLENGE) {
            val level = config.challengeLevel
            return (level / 2 + Random.nextInt(1, 4)).coerceAtMost(80)
        }
        return round + Random.nextInt(0, 3)
    }

    /** Fraction of cells left empty so rows are not a solid wall. */
    private fun gapChance(): Float {
        if (config.mode == GameMode.CHALLENGE) {
            return when {
                config.challengeLevel <= 10 -> 0.30f
                config.challengeLevel <= 40 -> 0.22f
                config.challengeLevel <= 80 -> 0.15f
                else -> 0.10f
            }
        }
        return if (round <= 2) 0.35f else 0.2f
    }

    fun spawnTopRow() {
        val y = bounds.brickAreaTop
        val cy = y + brickHeight / 2f

        // Policy: guarantee one "+1 ball" pickup per row so the ball count grows
        // steadily as the player keeps hitting it. Optionally one bonus pickup.
        val ballCol = Random.nextInt(0, cols)
        val bonusCol = if (Random.nextFloat() < 0.4f) Random.nextInt(0, cols) else -1

        for (col in 0 until cols) {
            val x = bounds.left + col * (brickWidth + 6f)
            val cx = x + brickWidth / 2f

            if (col == ballCol) {
                collectables.add(Collectable.ExtraBall(nextCollectableId++, cx, cy))
                continue
            }
            if (col == bonusCol) {
                spawnBonusCollectable(cx, cy)
                continue
            }
            if (Random.nextFloat() < gapChance()) continue

            val hp = brickHpForRound()
            bricks.add(
                Brick(
                    id = nextBrickId++,
                    x = x,
                    y = y,
                    width = brickWidth,
                    height = brickHeight,
                    hp = hp,
                    maxHp = hp
                )
            )
        }
    }

    /** Coin or power-up drop (extra balls are handled by the per-row guarantee). */
    private fun spawnBonusCollectable(x: Float, y: Float) {
        collectables.add(
            if (Random.nextFloat() < 0.7f) {
                Collectable.Coin(nextCollectableId++, x, y, amount = 1 + round / 5)
            } else {
                Collectable.PowerUpCollectable(
                    nextCollectableId++, x, y,
                    powerUpType = PowerUpType.entries.random()
                )
            }
        )
    }

    private fun spawnRandomCollectable(x: Float, y: Float) {
        val roll = Random.nextFloat()
        collectables.add(
            when {
                roll < 0.4f -> Collectable.ExtraBall(nextCollectableId++, x, y)
                roll < 0.7f -> Collectable.Coin(nextCollectableId++, x, y, amount = 1 + round / 5)
                else -> Collectable.PowerUpCollectable(
                    nextCollectableId++, x, y,
                    powerUpType = PowerUpType.entries.random()
                )
            }
        )
    }

    fun startAim(x: Float, y: Float) {
        if (phase != GamePhase.AIMING) return
        if (launcherAnimating) return // wait until the launcher finishes sliding
        isAiming = true
        aimStartX = launcherX
        aimStartY = launcherY
        aimEndX = x
        aimEndY = y
    }

    fun updateAim(x: Float, y: Float) {
        if (!isAiming) return
        aimEndX = x
        aimEndY = y
    }

    fun releaseAim() {
        if (!isAiming || phase != GamePhase.AIMING) return
        isAiming = false

        // Ignore taps with no real aim movement; require a small drag distance.
        val dx = aimEndX - aimStartX
        val dy = aimEndY - aimStartY
        if (dx * dx + dy * dy < 100f) return

        // The angle is always clamped upward by computeLaunchAngle, so any
        // genuine drag results in a launch toward the bricks.
        phase = GamePhase.LAUNCHING
        launchQueueIndex = 0
        launchTimer = 0f
        physics.resetAccumulator()
        resetBallsForLaunch()
        onShoot?.invoke()
    }

    private fun resetBallsForAim() {
        balls.clear()
        nextBallId = 0
        for (i in 0 until totalBalls) {
            balls.add(Ball(nextBallId++, launcherX, launcherY, 0f, 0f, 13f))
        }
    }

    private fun resetBallsForLaunch() {
        for (ball in balls) {
            ball.x = launcherX
            ball.y = launcherY
            ball.vx = 0f
            ball.vy = 0f
            ball.active = false
            ball.launched = false
            ball.lastHitBrickId = -1L
        }
    }

    fun update(deltaTime: Float): Boolean {
        updatePowerUps(deltaTime)
        particles.update(deltaTime)
        updateFloatingTexts(deltaTime)
        updateBrickAnimations(deltaTime)
        updateLauncherAnimation(deltaTime)

        if (config.mode == GameMode.TIME_ATTACK && phase != GamePhase.GAME_OVER) {
            timeAttackRemaining -= deltaTime
            if (timeAttackRemaining <= 0) {
                phase = GamePhase.VICTORY
                return false
            }
        }

        when (phase) {
            GamePhase.AIMING -> return true
            GamePhase.LAUNCHING -> {
                launchTimer += deltaTime
                if (launchTimer >= launchDelay) {
                    launchTimer = 0f
                    if (launchQueueIndex < balls.size) {
                        val angle = physics.computeLaunchAngle(aimStartX, aimStartY, aimEndX, aimEndY)
                        physics.launchBall(balls[launchQueueIndex], angle)
                        ballsLaunchedTotal++
                        launchQueueIndex++
                    } else {
                        phase = GamePhase.SIMULATING
                    }
                }
                // Simulate already-launched balls while the rest are still firing.
                // Without this every ball spawns on the same point and overlaps.
                physics.update(
                    balls, bricks, collectables, bounds,
                    onBrickHit = { brick, ball -> handleBrickHit(brick, ball) },
                    onBrickDestroyed = { brick -> handleBrickDestroyed(brick) },
                    onBallBounce = { onBallBounce?.invoke() },
                    onCollectableHit = { c, _ -> handleCollectable(c) },
                    deltaTime = deltaTime
                )
                return true
            }
            GamePhase.SIMULATING -> {
                val anyActive = physics.update(
                    balls, bricks, collectables, bounds,
                    onBrickHit = { brick, ball -> handleBrickHit(brick, ball) },
                    onBrickDestroyed = { brick -> handleBrickDestroyed(brick) },
                    onBallBounce = { onBallBounce?.invoke() },
                    onCollectableHit = { c, _ -> handleCollectable(c) },
                    deltaTime = deltaTime
                )
                // The first ball to come back defines next round's launch spot.
                if (!hasNextLauncher) {
                    for (b in balls) {
                        if (b.launched && !b.active) {
                            nextLauncherX = b.x.coerceIn(bounds.left + 14f, bounds.right - 14f)
                            hasNextLauncher = true
                            break
                        }
                    }
                }
                if (!anyActive) {
                    endRound()
                }
                return true
            }
            else -> return false
        }
    }

    private fun handleBrickHit(brick: Brick, ball: Ball) {
        var damage = damageMultiplier
        if (doubleDamageActive) damage *= 2
        if (Random.nextFloat() < upgrades.criticalHitChance()) {
            damage *= 2
            addFloatingText(brick.x + brick.width / 2, brick.y, "CRIT!")
        }
        brick.hp -= damage
        score += damage
        if (brick.hp <= 0 && !brick.isDestroying) {
            brick.isDestroying = true
            brick.destroyAnimProgress = 0f
        }
    }

    private fun handleBrickDestroyed(brick: Brick, playEffects: Boolean = true) {
        bricksDestroyedThisRound++
        bricksDestroyedTotal++
        val color = brickColorForHp(brick.maxHp)

        val coinAmount = ((1 + round / 10f) * coinMultiplier).roundToInt().coerceAtLeast(1)
        coinsThisSession += coinAmount
        score += coinAmount * 10

        if (playEffects) {
            if (particleEffectsEnabled) {
                particles.emitExplosion(brick.x + brick.width / 2, brick.y + brick.height / 2, color)
            }
            addFloatingText(brick.x + brick.width / 2, brick.y, "+${brick.maxHp}")
            if (Random.nextFloat() < 0.08f) {
                spawnRandomCollectable(brick.x + brick.width / 2, brick.y + brick.height / 2)
            }
            onBrickDestroyed?.invoke(brick)
        }
    }

    private fun handleCollectable(c: Collectable) {
        when (c) {
            is Collectable.ExtraBall -> {
                totalBalls++
                if (particleEffectsEnabled) {
                    particles.emitGlowBurst(c.x, c.y, androidx.compose.ui.graphics.Color(0xFF00E5FF))
                }
            }
            is Collectable.Coin -> {
                coinsThisSession += c.amount
                score += c.amount * 5
                if (particleEffectsEnabled) {
                    particles.emitGlowBurst(c.x, c.y, androidx.compose.ui.graphics.Color(0xFFFFD600))
                }
            }
            is Collectable.PowerUpCollectable -> activatePowerUp(c.powerUpType, c.x, c.y)
        }
        onCollect?.invoke(c)
    }

    private fun activatePowerUp(type: PowerUpType, x: Float, y: Float) {
        if (particleEffectsEnabled) {
            particles.emitGlowBurst(x, y, androidx.compose.ui.graphics.Color(0xFFE040FB))
        }
        onPowerUpActivated?.invoke(type)
        when (type) {
            PowerUpType.MULTI_BALL -> totalBalls += 5
            PowerUpType.BOMB -> applyBomb(x, y)
            PowerUpType.LASER -> applyLaser(x)
            PowerUpType.SLOW_MOTION -> {
                // Was slowing the whole game (timeScale 0.5) which felt like stuck lag.
                // Repurposed: instant +3 balls, no simulation slowdown.
                totalBalls += 3
                addFloatingText(x, y - 20f, "+3 Balls!")
            }
            PowerUpType.DOUBLE_DAMAGE -> {
                doubleDamageActive = true
                activePowerUps.add(ActivePowerUp(type, 8f))
            }
        }
    }

    private fun applyBomb(x: Float, y: Float) {
        val radius = brickWidth * 2.5f
        val radiusSq = radius * radius
        var destroyed = 0
        var sample: Brick? = null
        for (brick in bricks) {
            if (brick.hp <= 0) continue
            val cx = brick.x + brick.width / 2
            val cy = brick.y + brick.height / 2
            val dx = cx - x
            val dy = cy - y
            if (dx * dx + dy * dy <= radiusSq) {
                brick.hp = 0
                brick.isDestroying = true
                if (sample == null) sample = brick
                handleBrickDestroyed(brick, playEffects = false)
                destroyed++
            }
        }
        if (destroyed > 0 && particleEffectsEnabled) {
            particles.emitExplosion(x, y, androidx.compose.ui.graphics.Color(0xFFFF5722), 10)
            sample?.let { onBrickDestroyed?.invoke(it) }
        }
    }

    private fun applyLaser(columnX: Float) {
        val col = ((columnX - bounds.left) / (brickWidth + 6f)).toInt().coerceIn(0, cols - 1)
        val targetX = bounds.left + col * (brickWidth + 6f)
        var destroyed = 0
        var sample: Brick? = null
        var effectY = bounds.brickAreaTop
        for (brick in bricks) {
            if (brick.hp <= 0) continue
            if (brick.x >= targetX - 1 && brick.x <= targetX + brick.width + 1) {
                brick.hp = 0
                brick.isDestroying = true
                if (sample == null) sample = brick
                effectY = brick.y + brick.height / 2f
                handleBrickDestroyed(brick, playEffects = false)
                destroyed++
            }
        }
        if (destroyed > 0 && particleEffectsEnabled) {
            particles.emitExplosion(
                targetX + brickWidth / 2f,
                effectY,
                androidx.compose.ui.graphics.Color(0xFFE040FB),
                10
            )
            sample?.let { onBrickDestroyed?.invoke(it) }
        }
    }

    private fun endRound() {
        // Check game over - any brick reached bottom
        for (brick in bricks) {
            if (brick.hp > 0 && brick.y + brick.height >= bounds.bottom - 20f) {
                if (config.mode == GameMode.HARDCORE) {
                    phase = GamePhase.GAME_OVER
                    onGameOver?.invoke()
                    return
                }
                phase = GamePhase.GAME_OVER
                onGameOver?.invoke()
                return
            }
        }

        // Challenge mode victory check
        if (config.mode == GameMode.CHALLENGE && bricks.none { it.hp > 0 }) {
            phase = GamePhase.VICTORY
            return
        }

        phase = GamePhase.ROUND_END
        moveBricksDown()
        round++
        spawnTopRow()
        coinsThisSession += (5 + round / 2)
        bricksDestroyedThisRound = 0

        // Slide the launcher to where the first ball returned this round.
        val target = if (hasNextLauncher) nextLauncherX else launcherX
        launcherAnimFrom = launcherX
        launcherAnimTo = target
        launcherAnimT = 0f
        launcherAnimating = launcherAnimFrom != launcherAnimTo
        hasNextLauncher = false

        physics.timeScale = 1f
        doubleDamageActive = false
        activePowerUps.clear()

        resetBallsForAim()
        phase = GamePhase.AIMING
        onRoundComplete?.invoke(round)
    }

    private fun updateLauncherAnimation(deltaTime: Float) {
        if (!launcherAnimating) return
        launcherAnimT += deltaTime / LAUNCHER_ANIM_DURATION
        if (launcherAnimT >= 1f) {
            launcherAnimT = 1f
            launcherAnimating = false
        }
        // Ease-out so the slide feels snappy then settles.
        val t = launcherAnimT
        val eased = 1f - (1f - t) * (1f - t)
        launcherX = launcherAnimFrom + (launcherAnimTo - launcherAnimFrom) * eased
        // Keep the waiting balls sitting on the moving launcher.
        for (b in balls) {
            if (!b.launched) {
                b.x = launcherX
                b.y = launcherY
            }
        }
    }

    private fun moveBricksDown() {
        val step = brickHeight + 6f
        for (brick in bricks) {
            brick.y += step
        }
        for (c in collectables) {
            if (!c.collected) {
                c.y += step
            }
        }
        bricks.removeAll { it.hp <= 0 }
        collectables.removeAll { it.collected }
    }

    private fun updatePowerUps(dt: Float) {
        val iter = activePowerUps.iterator()
        while (iter.hasNext()) {
            val pu = iter.next()
            pu.remainingSeconds -= dt
            if (pu.remainingSeconds <= 0) {
                when (pu.type) {
                    PowerUpType.DOUBLE_DAMAGE -> doubleDamageActive = false
                    else -> {}
                }
                iter.remove()
            }
        }
        // Never leave simulation slowed — power-ups must not affect global speed.
        physics.timeScale = 1f
    }

    private fun updateFloatingTexts(dt: Float) {
        val iter = floatingTexts.iterator()
        while (iter.hasNext()) {
            val ft = iter.next()
            ft.offsetY -= 60f * dt
            ft.alpha -= dt * 0.8f
            if (ft.alpha <= 0) iter.remove()
        }
    }

    private fun updateBrickAnimations(dt: Float) {
        for (brick in bricks) {
            if (brick.isDestroying) {
                brick.destroyAnimProgress += dt * 3f
            }
        }
        bricks.removeAll { it.isDestroying && it.destroyAnimProgress >= 1f }
    }

    private fun addFloatingText(x: Float, y: Float, text: String) {
        floatingTexts.add(FloatingText(nextFloatingTextId++, x, y, text))
    }

    fun getTrajectoryPoints(): FloatArray {
        if (!isAiming) return EMPTY_TRAJECTORY
        val angle = physics.computeLaunchAngle(aimStartX, aimStartY, aimEndX, aimEndY)
        return physics.predictTrajectory(launcherX, launcherY, angle, bounds, bricks)
    }

    fun pause() {
        if (phase != GamePhase.GAME_OVER && phase != GamePhase.VICTORY) {
            phase = GamePhase.PAUSED
            physics.timeScale = 1f
        }
    }

    fun resume() {
        if (phase == GamePhase.PAUSED) {
            phase = GamePhase.AIMING
            physics.timeScale = 1f
        }
    }

    fun getPlayTimeMs(): Long = System.currentTimeMillis() - sessionStartTime

    companion object {
        private const val LAUNCHER_ANIM_DURATION = 0.35f
        private val EMPTY_TRAJECTORY = FloatArray(0)
        private val BRICK_COLORS: Array<androidx.compose.ui.graphics.Color> = Array(51) { hp ->
            val ratio = (hp.coerceAtMost(50) / 50f)
            val r = (255 * ratio).toInt().coerceIn(50, 255)
            val g = (200 * (1 - ratio)).toInt().coerceIn(50, 200)
            val b = (100 + 155 * (1 - ratio)).toInt().coerceIn(50, 255)
            androidx.compose.ui.graphics.Color(r, g, b)
        }

        fun brickColorForHp(hp: Int): androidx.compose.ui.graphics.Color =
            BRICK_COLORS[hp.coerceIn(1, 50)]
    }
}
