package com.mostafa.brickblast.game.engine

import android.content.Context
import com.mostafa.brickblast.R
import com.mostafa.brickblast.domain.model.ActivePowerUp
import com.mostafa.brickblast.domain.model.Ball
import com.mostafa.brickblast.domain.model.Brick
import com.mostafa.brickblast.domain.model.Collectable
import com.mostafa.brickblast.domain.model.FloatingText
import com.mostafa.brickblast.domain.model.GameConfig
import com.mostafa.brickblast.domain.model.GameMode
import com.mostafa.brickblast.domain.model.GamePhase
import com.mostafa.brickblast.data.local.GameStateSerializer
import com.mostafa.brickblast.domain.model.GameSaveState
import com.mostafa.brickblast.domain.model.PlayerUpgrades
import com.mostafa.brickblast.domain.model.PowerUpType
import com.mostafa.brickblast.game.particle.ParticleSystem
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Central game engine orchestrating turn-based gameplay, physics, powerups, and progression.
 * Designed for 300+ balls and 500+ bricks at 60 FPS with minimal allocations.
 */
class GameEngine(private val context: Context) {

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
    var cols = 5
    var rowsVisible = 10
    var brickLayoutGeneration = 0
        private set
    var unlaunchedBallCount = 0
        private set
    var activeBallCount = 0
        private set

    private var cachedLaunchAngle = Float.NaN

    private val critText = context.getString(R.string.floating_crit)
    private val plusBallsText = context.getString(R.string.floating_plus_balls)

    // Tight spacing between launches; while-loop catches up after frame drops.
    private fun launchInterval(): Float {
        val spacing = BALL_DIAMETER * 0.4f
        val base = spacing / PhysicsEngine.BALL_SPEED
        val count = balls.size
        return when {
            count > 100 -> (0.9f / count).coerceAtLeast(0.004f)
            count > 40 -> base.coerceAtMost(0.01f)
            else -> base.coerceIn(0.006f, 0.014f)
        }
    }

    private var cachedTrajAimEndX = Float.NaN
    private var cachedTrajAimEndY = Float.NaN
    private var cachedTrajLauncherX = Float.NaN
    private var cachedTrajectory: FloatArray = EMPTY_TRAJECTORY

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
        save: GameSaveState? = null
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
        brickWidth = (bounds.right - bounds.left - (cols - 1) * BRICK_GAP) / cols
        brickHeight = brickWidth * 0.72f

        bricks.clear()
        balls.clear()
        collectables.clear()
        floatingTexts.clear()
        activePowerUps.clear()
        particles.clear()
        nextBallId = 0
        nextBrickId = 1L
        nextCollectableId = 1L
        damageMultiplier = playerUpgrades.ballDamage()
        physics.resetAccumulator()
        physics.timeScale = 1f
        doubleDamageActive = false

        val restored = save != null && save.mode == gameConfig.mode
        if (restored && save.bricksJson.isNotBlank()) {
            round = save.round
            score = save.score
            totalBalls = save.totalBalls
            coinsThisSession = save.coinsThisSession
            launcherX = if (save.launcherX > 0f) save.launcherX else screenWidth / 2f
            launcherY = bounds.launcherY
            nextLauncherX = save.nextLauncherX
            hasNextLauncher = save.hasNextLauncher
            timeAttackRemaining = if (gameConfig.mode == GameMode.TIME_ATTACK && save.timeAttackRemaining > 0f) {
                save.timeAttackRemaining
            } else {
                gameConfig.timeLimitSeconds.toFloat()
            }
            GameStateSerializer.restoreInto(
                this,
                save.bricksJson,
                save.collectablesJson
            )
        } else {
            round = if (gameConfig.mode == GameMode.CHALLENGE) {
                gameConfig.challengeLevel
            } else {
                save?.round ?: 1
            }
            score = save?.score ?: 0
            totalBalls = save?.totalBalls?.takeIf { it > 0 } ?: playerUpgrades.startingBalls()
            coinsThisSession = save?.coinsThisSession ?: 0
            launcherX = screenWidth / 2f
            launcherY = bounds.launcherY
            nextLauncherX = launcherX
            hasNextLauncher = false
            timeAttackRemaining = gameConfig.timeLimitSeconds.toFloat()

            if (gameConfig.mode == GameMode.CHALLENGE) {
                val rows = (2 + gameConfig.challengeLevel / 12).coerceIn(2, 12)
                repeat(rows) {
                    moveBricksDown()
                    spawnTopRow()
                }
            } else if ((save?.round ?: 1) <= 1) {
                spawnInitialBricks()
            } else {
                repeat(minOf(save?.round ?: 1, 5)) {
                    moveBricksDown()
                    spawnTopRow()
                }
            }
        }

        launcherAnimating = false
        launcherAnimT = 1f
        sessionStartTime = System.currentTimeMillis()
        bricksDestroyedTotal = 0L
        ballsLaunchedTotal = 0L
        resetBallsForAim()
        markBricksChanged()
        phase = GamePhase.AIMING
    }

    fun setNextBrickId(id: Long) {
        nextBrickId = id
    }

    fun setNextCollectableId(id: Long) {
        nextCollectableId = id
    }

    private fun markBricksChanged() {
        brickLayoutGeneration++
    }

    private fun spawnInitialBricks() {
        // Start with a few rows; HP and density ramp with round number.
        val rows = if (config.mode == GameMode.CHALLENGE) 2 else 2
        repeat(rows) {
            moveBricksDown()
            spawnTopRow()
        }
    }

    /**
     * Brick HP scales faster than round so growing ball count does not trivialize mid-game.
     */
    private fun brickHpForRound(): Int {
        if (config.mode == GameMode.CHALLENGE) {
            val level = config.challengeLevel
            return (level * 0.5f + Random.nextInt(1, 4)).toInt().coerceIn(1, 120)
        }
        return when (config.mode) {
            GameMode.CLASSIC -> (round + Random.nextInt(0, 3)).coerceAtLeast(1)
            GameMode.TIME_ATTACK -> (round + round / 4 + Random.nextInt(0, 3)).coerceAtLeast(1)
            GameMode.HARDCORE -> (round + round / 2 + 2 + Random.nextInt(1, 4)).coerceAtLeast(1)
            else -> (round + Random.nextInt(0, 3)).coerceAtLeast(1)
        }
    }

    /** Fraction of cells left empty so rows are not a solid wall. */
    private fun gapChance(): Float {
        if (config.mode == GameMode.CHALLENGE) {
            return when {
                config.challengeLevel <= 10 -> 0.24f
                config.challengeLevel <= 40 -> 0.18f
                config.challengeLevel <= 80 -> 0.12f
                else -> 0.08f
            }
        }
        return when (config.mode) {
            GameMode.CLASSIC -> when {
                round <= 2 -> 0.32f
                round <= 12 -> 0.22f
                else -> 0.15f
            }
            else -> when {
                round <= 2 -> 0.28f
                round <= 10 -> 0.18f
                else -> 0.12f
            }
        }
    }

    /** Classic: every new row includes a +1 ball pickup. Other modes use chance. */
    private fun shouldSpawnBallPickupThisRow(): Boolean {
        if (config.mode == GameMode.CLASSIC) return true
        val chance = when (config.mode) {
            GameMode.CHALLENGE -> 0.45f + config.challengeLevel.coerceAtMost(80) / 400f
            GameMode.HARDCORE -> 0.22f
            GameMode.TIME_ATTACK -> 0.48f
            else -> when {
                round <= 6 -> 0.65f
                round <= 15 -> 0.42f
                else -> 0.30f
            }
        }
        return Random.nextFloat() < chance
    }

    fun spawnTopRow() {
        val y = bounds.brickAreaTop
        val cy = y + brickHeight / 2f

        // One optional "+1 ball" per row (chance drops in later rounds).
        val ballCol = if (shouldSpawnBallPickupThisRow()) Random.nextInt(0, cols) else -1
        var bonusCol = if (Random.nextFloat() < 0.22f) Random.nextInt(0, cols) else -1
        if (bonusCol == ballCol) bonusCol = -1

        for (col in 0 until cols) {
            val x = bounds.left + col * (brickWidth + BRICK_GAP)
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
                roll < 0.22f -> Collectable.ExtraBall(nextCollectableId++, x, y)
                roll < 0.62f -> Collectable.Coin(nextCollectableId++, x, y, amount = 1 + round / 5)
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
        cachedLaunchAngle = physics.computeLaunchAngle(aimStartX, aimStartY, aimEndX, aimEndY)
        physics.resetAccumulator()
        resetBallsForLaunch()
        onShoot?.invoke()
    }

    /** Begins recalling all balls to the launcher; round advances once they arrive. */
    fun cancelShot(): Boolean {
        if (phase != GamePhase.LAUNCHING && phase != GamePhase.SIMULATING) return false

        if (!hasNextLauncher) {
            for (b in balls) {
                if (b.launched && !b.active) {
                    nextLauncherX = b.x.coerceIn(bounds.left + 14f, bounds.right - 14f)
                    hasNextLauncher = true
                    break
                }
            }
        }

        launchQueueIndex = balls.size
        unlaunchedBallCount = 0
        isAiming = false
        physics.resetAccumulator()
        particles.clear()
        floatingTexts.clear()

        for (ball in balls) {
            ball.vx = 0f
            ball.vy = 0f
            when {
                !ball.launched -> {
                    ball.x = launcherX
                    ball.y = launcherY
                    ball.active = false
                }
                !ball.active -> ball.active = true
            }
        }

        phase = GamePhase.RECALLING
        updateActiveBallCount()
        return true
    }

    private fun updateBallRecall(dt: Float) {
        val targetX = launcherX
        val targetY = launcherY
        var anyMoving = false

        for (ball in balls) {
            if (!ball.launched || !ball.active) continue

            val dx = targetX - ball.x
            val dy = targetY - ball.y
            val distSq = dx * dx + dy * dy
            if (distSq < RECALL_ARRIVE_RADIUS * RECALL_ARRIVE_RADIUS) {
                ball.x = targetX
                ball.y = targetY
                ball.active = false
                ball.vx = 0f
                ball.vy = 0f
                continue
            }

            anyMoving = true
            val dist = kotlin.math.sqrt(distSq)
            val move = (RECALL_SPEED * dt).coerceAtMost(dist)
            ball.x += dx / dist * move
            ball.y += dy / dist * move
        }

        updateActiveBallCount()
        if (!anyMoving) {
            finishCanceledRound()
        }
    }

    private fun finishCanceledRound() {
        for (ball in balls) {
            ball.active = false
            ball.vx = 0f
            ball.vy = 0f
        }
        activeBallCount = 0
        endRound()
    }

    private fun resetBallsForAim() {
        balls.clear()
        nextBallId = 0
        for (i in 0 until totalBalls) {
            balls.add(Ball(nextBallId++, launcherX, launcherY, 0f, 0f, BALL_RADIUS))
        }
        unlaunchedBallCount = totalBalls
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
        val dt = (deltaTime * GAME_SPEED).coerceAtMost(0.055f)
        updateActiveBallCount()
        val heavyLoad = activeBallCount > 40

        updatePowerUps(dt)
        if (!heavyLoad) {
            particles.update(dt)
            updateFloatingTexts(dt)
            updateBrickAnimations(dt)
        } else {
            particles.clear()
            floatingTexts.clear()
            updateBrickAnimationsFast()
        }
        updateLauncherAnimation(dt)

        if (config.mode == GameMode.TIME_ATTACK && phase != GamePhase.GAME_OVER) {
            timeAttackRemaining -= dt
            if (timeAttackRemaining <= 0) {
                phase = GamePhase.VICTORY
                return false
            }
        }

        when (phase) {
            GamePhase.AIMING -> return true
            GamePhase.LAUNCHING -> {
                launchTimer += dt
                val interval = launchInterval()
                while (launchTimer >= interval && launchQueueIndex < balls.size) {
                    launchTimer -= interval
                    physics.launchBall(balls[launchQueueIndex], cachedLaunchAngle)
                    ballsLaunchedTotal++
                    launchQueueIndex++
                    unlaunchedBallCount = balls.size - launchQueueIndex
                }
                if (launchQueueIndex >= balls.size) {
                    phase = GamePhase.SIMULATING
                }
                physics.update(
                    balls, bricks, collectables, bounds, brickLayoutGeneration, activeBallCount,
                    onBrickHit = { brick, ball -> handleBrickHit(brick, ball) },
                    onBrickDestroyed = { brick -> handleBrickDestroyed(brick) },
                    onBallBounce = { if (activeBallCount < 45) onBallBounce?.invoke() },
                    onCollectableHit = { c, _ -> handleCollectable(c) },
                    deltaTime = dt
                )
                updateActiveBallCount()
                return true
            }
            GamePhase.SIMULATING -> {
                val anyActive = physics.update(
                    balls, bricks, collectables, bounds, brickLayoutGeneration, activeBallCount,
                    onBrickHit = { brick, ball -> handleBrickHit(brick, ball) },
                    onBrickDestroyed = { brick -> handleBrickDestroyed(brick) },
                    onBallBounce = { if (activeBallCount < 45) onBallBounce?.invoke() },
                    onCollectableHit = { c, _ -> handleCollectable(c) },
                    deltaTime = dt
                )
                updateActiveBallCount()
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
            GamePhase.RECALLING -> {
                updateBallRecall(dt)
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
            if (activeBallCount < 45) {
                addFloatingText(brick.x + brick.width / 2, brick.y, critText)
            }
        }
        brick.hp -= damage
        score += damage
        if (brick.hp <= 0 && !brick.isDestroying) {
            brick.isDestroying = true
            brick.destroyAnimProgress = 0f
            markBricksChanged()
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
            if (particleEffectsEnabled && activeBallCount < 50) {
                particles.emitExplosion(brick.x + brick.width / 2, brick.y + brick.height / 2, color)
            }
            if (activeBallCount < 35) {
                addFloatingText(brick.x + brick.width / 2, brick.y, "+${brick.maxHp}")
            }
            if (Random.nextFloat() < 0.04f) {
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
            PowerUpType.MULTI_BALL -> totalBalls += 2
            PowerUpType.BOMB -> applyBomb(x, y)
            PowerUpType.LASER -> applyLaser(x)
            PowerUpType.SLOW_MOTION -> {
                totalBalls += 2
                addFloatingText(x, y - 20f, plusBallsText)
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
        if (destroyed > 0) {
            markBricksChanged()
            if (particleEffectsEnabled) {
                particles.emitExplosion(x, y, androidx.compose.ui.graphics.Color(0xFFFF5722), 10)
            }
            sample?.let { onBrickDestroyed?.invoke(it) }
        }
    }

    private fun applyLaser(columnX: Float) {
        val col = ((columnX - bounds.left) / (brickWidth + BRICK_GAP)).toInt().coerceIn(0, cols - 1)
        val targetX = bounds.left + col * (brickWidth + BRICK_GAP)
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
        if (destroyed > 0) {
            markBricksChanged()
            if (particleEffectsEnabled) {
                particles.emitExplosion(
                    targetX + brickWidth / 2f,
                    effectY,
                    androidx.compose.ui.graphics.Color(0xFFE040FB),
                    10
                )
            }
            sample?.let { onBrickDestroyed?.invoke(it) }
        }
    }

    private fun endRound() {
        val loseMargin = if (config.mode == GameMode.CLASSIC) {
            brickHeight.coerceAtLeast(32f) * 0.35f
        } else {
            brickHeight.coerceAtLeast(32f)
        }
        val loseLine = bounds.bottom - loseMargin
        // Check game over - bricks encroaching on the launcher zone
        for (brick in bricks) {
            if (brick.hp > 0 && brick.y + brick.height >= loseLine) {
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
        // Classic: occasional extra row pressure (less frequent than before).
        if (config.mode == GameMode.CLASSIC && round % 8 == 0) {
            moveBricksDown()
            spawnTopRow()
        }
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

    /**
     * Rewarded-ad continue: clear the bottom [rowCount] brick rows and resume play.
     * Called when the player watches an ad after losing (store flavor).
     */
    fun continueAfterRewardedAd(rowCount: Int = 3): Boolean {
        if (phase != GamePhase.GAME_OVER) return false

        removeBottomBrickRows(rowCount)
        isAiming = false
        physics.resetAccumulator()
        resetBallsForAim()
        phase = GamePhase.AIMING
        return true
    }

    private fun removeBottomBrickRows(rowCount: Int) {
        if (rowCount <= 0) return
        val rowStep = brickHeight + BRICK_GAP
        if (rowStep <= 0f) return

        val rowKeys = bricks
            .asSequence()
            .filter { it.hp > 0 && !it.isDestroying }
            .map { kotlin.math.round(it.y / rowStep).toLong() }
            .distinct()
            .sortedDescending()
            .take(rowCount)
            .toSet()

        if (rowKeys.isEmpty()) return
        bricks.removeAll { brick ->
            brick.hp > 0 && kotlin.math.round(brick.y / rowStep).toLong() in rowKeys
        }
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
        val step = brickHeight + BRICK_GAP
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
        markBricksChanged()
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
        var removed = false
        for (brick in bricks) {
            if (brick.isDestroying) {
                brick.destroyAnimProgress += dt * 3f
            }
        }
        if (bricks.removeAll { it.isDestroying && it.destroyAnimProgress >= 1f }) {
            removed = true
        }
        if (removed) markBricksChanged()
    }

    private fun updateBrickAnimationsFast() {
        var removed = false
        for (brick in bricks) {
            if (brick.isDestroying) {
                brick.destroyAnimProgress = 1f
            }
        }
        if (bricks.removeAll { it.isDestroying }) {
            removed = true
        }
        if (removed) markBricksChanged()
    }

    private fun updateActiveBallCount() {
        var count = 0
        for (i in balls.indices) {
            val ball = balls[i]
            if (ball.active && ball.launched) count++
        }
        activeBallCount = count
    }

    private fun addFloatingText(x: Float, y: Float, text: String) {
        if (floatingTexts.size >= 10) {
            floatingTexts.removeAt(0)
        }
        floatingTexts.add(FloatingText(nextFloatingTextId++, x, y, text))
    }

    fun getTrajectoryPoints(): FloatArray {
        if (!isAiming) return EMPTY_TRAJECTORY
        if (aimEndX == cachedTrajAimEndX && aimEndY == cachedTrajAimEndY &&
            launcherX == cachedTrajLauncherX
        ) {
            return cachedTrajectory
        }
        val angle = physics.computeLaunchAngle(aimStartX, aimStartY, aimEndX, aimEndY)
        cachedTrajectory = physics.predictTrajectory(launcherX, launcherY, angle, bounds)
        cachedTrajAimEndX = aimEndX
        cachedTrajAimEndY = aimEndY
        cachedTrajLauncherX = launcherX
        return cachedTrajectory
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
        const val BALL_RADIUS = 12f
        private const val BALL_DIAMETER = BALL_RADIUS * 2f
        private const val BRICK_GAP = 8f
        private const val LAUNCHER_ANIM_DURATION = 0.30f
        private const val RECALL_SPEED = 1600f
        private const val RECALL_ARRIVE_RADIUS = 6f
        /** Slightly faster pacing for balls, launches, animations, and timers. */
        private const val GAME_SPEED = 1.1f
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

        fun brickColorIntForHp(hp: Int): Int = BRICK_COLOR_INTS[hp.coerceIn(1, 50)]

        private val BRICK_COLOR_INTS: IntArray = IntArray(51) { hp ->
            val ratio = (hp.coerceAtMost(50) / 50f)
            val r = (255 * ratio).toInt().coerceIn(50, 255)
            val g = (200 * (1 - ratio)).toInt().coerceIn(50, 200)
            val b = (100 + 155 * (1 - ratio)).toInt().coerceIn(50, 255)
            android.graphics.Color.rgb(r, g, b)
        }
    }
}
