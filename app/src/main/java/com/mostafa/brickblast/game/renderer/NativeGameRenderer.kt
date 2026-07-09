package com.mostafa.brickblast.game.renderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.mostafa.brickblast.R
import com.mostafa.brickblast.domain.model.BoardFeature
import com.mostafa.brickblast.domain.model.BoardVisualTheme
import com.mostafa.brickblast.domain.model.Collectable
import com.mostafa.brickblast.domain.model.GamePhase
import com.mostafa.brickblast.domain.model.PowerUpType
import com.mostafa.brickblast.game.particle.ParticleType
import kotlin.math.cos
import kotlin.math.sin

/**
 * Hardware-accelerated Android Canvas renderer for the game loop.
 * Avoids Compose draw scope / TextMeasurer overhead on every frame.
 */
class NativeGameRenderer(context: Context, density: Float) {

    private val densityScale = density
    private val cornerRadius = 6f * densityScale
    private val boardRect = RectF()
    private val brickRect = RectF()
    private val chevronPath = Path()
    private val wavePath = Path()
    private val clipRect = RectF()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ambientPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val emberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * densityScale
    }
    private val boardGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * densityScale
    }
    private val floorLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * densityScale
    }
    private val brickFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val brickStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * densityScale
        color = Color.argb(38, 255, 255, 255)
    }
    private val brickGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val brickShimmerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * densityScale
    }
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * densityScale
    }
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val nebulaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ballGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ballStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * densityScale
        color = Color.argb(89, 255, 255, 255)
    }
    private val launcherPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val launcherAuraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val launcherStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * densityScale
    }
    private val aimLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(153, 255, 255, 255)
        strokeWidth = 3f * densityScale
        style = Paint.Style.STROKE
    }
    private val trajPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hpTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 14f * densityScale
        color = Color.WHITE
    }
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 20f * densityScale
        isFakeBoldText = true
    }
    private val floatTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 16f * densityScale
        color = Color.parseColor("#FFD600")
    }
    private val chevronPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * densityScale
        color = Color.argb(179, 128, 216, 255)
    }
    private val markerStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * densityScale
        color = Color.argb(128, 128, 216, 255)
    }
    private val voxelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isAntiAlias = false }
    private val blockLightEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = false
        style = Paint.Style.STROKE
        strokeWidth = 2f * densityScale
    }
    private val blockDarkEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = false
        style = Paint.Style.STROKE
        strokeWidth = 2f * densityScale
    }
    private val blockOrePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isAntiAlias = false }
    private val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isAntiAlias = false }

    private val persianTypeface: Typeface? = ResourcesCompat.getFont(context, R.font.vazir_regular)
    private val persianBoldTypeface: Typeface? = ResourcesCompat.getFont(context, R.font.vazir_bold)

    private var cachedIdleBalls = -1
    private var cachedIdleLabel = ""

    fun render(
        canvas: Canvas,
        engine: com.mostafa.brickblast.game.engine.GameEngine,
        trajectoryPoints: FloatArray,
        showTrajectory: Boolean,
        theme: BoardVisualTheme,
        isDark: Boolean,
        persianUi: Boolean
    ) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val timeSec = System.currentTimeMillis() * 0.001f
        val voxelWorld = BoardFeature.VOXEL_WORLD_BG in theme.features

        val activeBalls = engine.activeBallCount
        val heavyLoad = activeBalls > 30
        val skipPremiumFx = heavyLoad || activeBalls > 20

        if (voxelWorld) {
            drawVoxelWorldBackground(canvas, w, h, timeSec, skipPremiumFx)
        } else {
            bgPaint.color = if (isDark) theme.bgDark else theme.bgLight
            canvas.drawRect(0f, 0f, w, h, bgPaint)
        }

        if (!voxelWorld && !skipPremiumFx && BoardFeature.ANIMATED_BG in theme.features) {
            drawAnimatedAmbient(canvas, w, h, theme, timeSec, isDark)
        }
        if (!skipPremiumFx && BoardFeature.EMBER_DRIFT in theme.features) {
            drawEmberDrift(canvas, w, h, theme, timeSec)
        }
        if (BoardFeature.STARFIELD_BG in theme.features && isDark) {
            drawNebulaBlobs(canvas, w, h, theme, timeSec, skipPremiumFx)
            drawStarfield(canvas, w, h, timeSec, skipPremiumFx)
        }

        val ink = if (voxelWorld) Color.parseColor("#3E2723") else if (isDark) Color.WHITE else Color.parseColor("#101418")
        borderPaint.color = if (voxelWorld) {
            Color.parseColor("#5D4037")
        } else {
            Color.argb(26, Color.red(ink), Color.green(ink), Color.blue(ink))
        }
        floorLinePaint.color = if (voxelWorld) {
            Color.parseColor("#6D4C41")
        } else {
            Color.argb(41, Color.red(ink), Color.green(ink), Color.blue(ink))
        }
        launcherStrokePaint.color = if (voxelWorld) {
            Color.parseColor("#4E342E")
        } else {
            Color.argb(102, Color.red(ink), Color.green(ink), Color.blue(ink))
        }
        badgePaint.color = if (voxelWorld) Color.parseColor("#3E2723") else ink
        badgePaint.typeface = if (persianUi) persianBoldTypeface else Typeface.DEFAULT_BOLD
        hpTextPaint.typeface = if (persianUi) persianTypeface else Typeface.DEFAULT
        floatTextPaint.typeface = if (persianUi) persianTypeface else Typeface.DEFAULT

        ballPaint.color = theme.ballColor
        launcherPaint.color = theme.launcherColor
        trajPaint.color = Color.argb(
            217,
            Color.red(theme.trajectoryColor),
            Color.green(theme.trajectoryColor),
            Color.blue(theme.trajectoryColor)
        )

        val b = engine.bounds
        boardRect.set(b.left - 4f, b.top - 4f, b.right + 4f, b.bottom + 4f)

        if (BoardFeature.GRID_BG in theme.features) {
            drawBoardGrid(canvas, b, ink, timeSec, skipPremiumFx)
        }
        if (!skipPremiumFx && BoardFeature.WAVE_OVERLAY in theme.features) {
            drawOceanWaves(canvas, b, theme, timeSec)
        }

        if (!skipPremiumFx && theme.isPremium && !voxelWorld) {
            val boardPulse = sin(timeSec * 1.4f) * 0.5f + 0.5f
            boardGlowPaint.color = Color.argb(
                (18 + boardPulse * 28).toInt(),
                Color.red(theme.launcherColor),
                Color.green(theme.launcherColor),
                Color.blue(theme.launcherColor)
            )
            boardGlowPaint.strokeWidth = (2f + boardPulse * 2f) * densityScale
            canvas.drawRoundRect(boardRect, 8f * densityScale, 8f * densityScale, boardGlowPaint)
        }

        if (voxelWorld) {
            canvas.drawRect(boardRect, borderPaint)
        } else {
            canvas.drawRoundRect(boardRect, 8f * densityScale, 8f * densityScale, borderPaint)
        }
        canvas.drawLine(b.left, b.bottom, b.right, b.bottom, floorLinePaint)

        val skipBallStroke = activeBalls > 20
        val skipBrickStroke = activeBalls > 15
        val skipBrickHp = activeBalls > 25
        val skipParticles = activeBalls > 40 ||
            engine.phase == GamePhase.LAUNCHING ||
            engine.phase == GamePhase.RECALLING
        val skipFloatText = heavyLoad
        val blockyBricks = BoardFeature.BLOCKY_BRICKS in theme.features
        val useRoundBricks = !heavyLoad && !blockyBricks
        val glowBricks = BoardFeature.GLOW_BRICKS in theme.features && !skipBrickStroke
        val shimmerBricks = BoardFeature.SHIMMER_HIGH_HP in theme.features && !skipBrickStroke
        val ballGlow = BoardFeature.BALL_GLOW in theme.features && !skipPremiumFx
        val launcherAura = BoardFeature.LAUNCHER_AURA in theme.features && !skipPremiumFx

        for (brick in engine.bricks) {
            if (brick.hp <= 0 && !brick.isDestroying) continue
            val cx = brick.x + brick.width * 0.5f
            val cy = brick.y + brick.height * 0.5f

            if (brick.isDestroying) {
                drawDestroyingBrick(canvas, brick, cx, cy, heavyLoad, theme, timeSec, glowBricks, blockyBricks)
                continue
            }

            brickFillPaint.color = theme.brickColorIntForHp(brick.hp)
            brickRect.set(brick.x, brick.y, brick.x + brick.width, brick.y + brick.height)
            if (blockyBricks) {
                drawBlockyBrick(canvas, brickRect, brickFillPaint.color, brick.hp)
            } else if (useRoundBricks) {
                canvas.drawRoundRect(brickRect, cornerRadius, cornerRadius, brickFillPaint)
                if (!skipBrickStroke) {
                    canvas.drawRoundRect(brickRect, cornerRadius, cornerRadius, brickStrokePaint)
                }
                if (glowBricks) {
                    drawBrickGlow(canvas, brickRect, brickFillPaint.color, cx, timeSec)
                }
                if (shimmerBricks && brick.hp >= 28) {
                    drawBrickShimmer(canvas, brickRect, cornerRadius, timeSec, brick.hp)
                }
            } else {
                canvas.drawRect(brickRect, brickFillPaint)
            }

            if (!skipBrickHp) {
                val textY = cy - (hpTextPaint.descent() + hpTextPaint.ascent()) * 0.5f
                if (blockyBricks) {
                    hpTextPaint.color = if (brick.hp >= 22) Color.WHITE else Color.parseColor("#3E2723")
                }
                canvas.drawText(brick.hp.toString(), cx, textY, hpTextPaint)
                if (blockyBricks) hpTextPaint.color = Color.WHITE
            }
        }

        for (c in engine.collectables) {
            if (c.collected) continue
            when (c) {
                is Collectable.ExtraBall -> {
                    canvas.drawCircle(c.x, c.y, c.radius, COLLECT_BALL_PAINT)
                }
                is Collectable.Coin -> {
                    canvas.drawCircle(c.x, c.y, c.radius, COIN_PAINT)
                }
                is Collectable.PowerUpCollectable -> {
                    canvas.drawCircle(c.x, c.y, c.radius, powerUpPaint(c.powerUpType))
                }
            }
        }

        for (ball in engine.balls) {
            if (!ball.active) continue
            if (ballGlow) {
                val pulse = sin(timeSec * 4f + ball.x * 0.02f) * 0.5f + 0.5f
                ballGlowPaint.color = Color.argb(
                    (35 + pulse * 55).toInt(),
                    Color.red(theme.ballColor),
                    Color.green(theme.ballColor),
                    Color.blue(theme.ballColor)
                )
                canvas.drawCircle(ball.x, ball.y, ball.radius * (1.6f + pulse * 0.5f), ballGlowPaint)
            }
            canvas.drawCircle(ball.x, ball.y, ball.radius, ballPaint)
            if (!skipBallStroke) {
                canvas.drawCircle(ball.x, ball.y, ball.radius, ballStrokePaint)
            }
        }

        if (engine.hasNextLauncher && engine.phase == GamePhase.SIMULATING) {
            val mx = engine.nextLauncherX
            val my = engine.launcherY
            canvas.drawCircle(mx, my, 12f * densityScale, markerStrokePaint)
            chevronPath.reset()
            chevronPath.moveTo(mx - 7f * densityScale, my - 22f * densityScale)
            chevronPath.lineTo(mx, my - 14f * densityScale)
            chevronPath.lineTo(mx + 7f * densityScale, my - 22f * densityScale)
            canvas.drawPath(chevronPath, chevronPaint)
        }

        if (launcherAura) {
            val auraPulse = sin(timeSec * 2.5f) * 0.5f + 0.5f
            launcherAuraPaint.color = Color.argb(
                (50 + auraPulse * 90).toInt(),
                Color.red(theme.launcherColor),
                Color.green(theme.launcherColor),
                Color.blue(theme.launcherColor)
            )
            launcherAuraPaint.strokeWidth = (2f + auraPulse * 3f) * densityScale
            canvas.drawCircle(
                engine.launcherX,
                engine.launcherY,
                (18f + auraPulse * 8f) * densityScale,
                launcherAuraPaint
            )
        }

        if (blockyBricks) {
            val launcherSize = 14f * densityScale
            brickRect.set(
                engine.launcherX - launcherSize,
                engine.launcherY - launcherSize,
                engine.launcherX + launcherSize,
                engine.launcherY + launcherSize
            )
            drawBlockyBrick(canvas, brickRect, theme.launcherColor, hp = 1)
        } else {
            canvas.drawCircle(engine.launcherX, engine.launcherY, 14f * densityScale, launcherPaint)
            canvas.drawCircle(engine.launcherX, engine.launcherY, 14f * densityScale, launcherStrokePaint)
        }

        val idleBalls = engine.unlaunchedBallCount
        if (idleBalls > 0) {
            if (idleBalls != cachedIdleBalls) {
                cachedIdleBalls = idleBalls
                cachedIdleLabel = "×$idleBalls"
            }
            canvas.drawText(
                cachedIdleLabel,
                engine.launcherX,
                engine.launcherY - 44f * densityScale,
                badgePaint
            )
        } else {
            cachedIdleBalls = -1
        }

        if (showTrajectory && trajectoryPoints.size >= 4) {
            var i = 0
            while (i < trajectoryPoints.size) {
                val x = trajectoryPoints[i]
                val y = trajectoryPoints[i + 1]
                if (y <= 0f || y > b.bottom) break
                if ((i / 2) % 2 == 0) {
                    canvas.drawCircle(x, y, 5f * densityScale, trajPaint)
                }
                i += 2
            }
        }

        if (engine.isAiming) {
            canvas.drawLine(engine.aimStartX, engine.aimStartY, engine.aimEndX, engine.aimEndY, aimLinePaint)
        }

        if (!skipParticles) {
            engine.particles.forEachActive { p ->
                val alpha = ((1f - p.life / p.maxLife) * 255f).toInt().coerceIn(0, 255)
                particlePaint.color = composeColorToArgb(p.color, alpha)
                when (p.type) {
                    ParticleType.EXPLOSION ->
                        canvas.drawCircle(p.x, p.y, p.size * (1f + p.life), particlePaint)
                    ParticleType.SPARK ->
                        canvas.drawCircle(p.x, p.y, p.size, particlePaint)
                    ParticleType.GLOW ->
                        canvas.drawCircle(p.x, p.y, p.size * 2f, particlePaint)
                }
            }
        }

        if (!skipFloatText) {
            for (ft in engine.floatingTexts) {
                floatTextPaint.alpha = (ft.alpha * 255f).toInt().coerceIn(0, 255)
                canvas.drawText(
                    ft.text,
                    ft.x,
                    ft.y + ft.offsetY,
                    floatTextPaint
                )
            }
            floatTextPaint.alpha = 255
        }
    }

    private fun drawVoxelWorldBackground(
        canvas: Canvas,
        w: Float,
        h: Float,
        timeSec: Float,
        reduced: Boolean
    ) {
        voxelPaint.shader = LinearGradient(
            0f, 0f, 0f, h,
            intArrayOf(
                Color.parseColor("#4FA7FF"),
                Color.parseColor("#87CEEB"),
                Color.parseColor("#C8E6FF")
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, voxelPaint)
        voxelPaint.shader = null

        if (!reduced) {
            cloudPaint.color = Color.argb(230, 255, 255, 255)
            val block = 9f * densityScale
            for (i in 0 until 5) {
                val drift = timeSec * 10f + i * 140f
                val cx = (drift % (w + 220f)) - 110f
                val cy = h * (0.06f + i * 0.055f)
                canvas.drawRect(cx, cy, cx + block * 3.2f, cy + block, cloudPaint)
                canvas.drawRect(cx + block, cy - block * 0.45f, cx + block * 4.2f, cy + block * 0.55f, cloudPaint)
            }
        }

        val grassTop = h * 0.7f
        voxelPaint.color = Color.parseColor("#77AB2F")
        canvas.drawRect(0f, grassTop, w, h, voxelPaint)

        voxelPaint.color = Color.parseColor("#8B6914")
        val dirtBand = 14f * densityScale
        var dy = grassTop + dirtBand
        while (dy < h) {
            canvas.drawRect(0f, dy, w, dy + dirtBand * 0.35f, voxelPaint)
            dy += dirtBand
        }

        val grassTile = 14f * densityScale
        var gx = 0f
        while (gx < w) {
            voxelPaint.color = Color.parseColor("#6F9B32")
            canvas.drawRect(gx, grassTop, gx + grassTile * 0.55f, grassTop + grassTile * 0.22f, voxelPaint)
            gx += grassTile
        }
    }

    private fun drawBlockyBrick(canvas: Canvas, rect: RectF, color: Int, hp: Int) {
        val inset = 1f * densityScale
        val inner = RectF(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset)
        if (inner.width() <= 0f || inner.height() <= 0f) return

        voxelPaint.color = color
        canvas.drawRect(inner, voxelPaint)

        if (hp <= 12) {
            val grassH = inner.height() * 0.32f
            voxelPaint.color = color
            canvas.drawRect(inner.left, inner.top, inner.right, inner.top + grassH, voxelPaint)
            blockOrePaint.color = Color.parseColor("#6D4C41")
            canvas.drawRect(inner.left, inner.top + grassH, inner.right, inner.bottom, blockOrePaint)
            blockOrePaint.color = Color.parseColor("#5D4037")
            canvas.drawRect(inner.left, inner.bottom - 2f * densityScale, inner.right, inner.bottom, blockOrePaint)
        }

        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        blockLightEdgePaint.color = Color.rgb(
            (r + (255 - r) * 0.38f).toInt().coerceIn(0, 255),
            (g + (255 - g) * 0.38f).toInt().coerceIn(0, 255),
            (b + (255 - b) * 0.38f).toInt().coerceIn(0, 255)
        )
        blockDarkEdgePaint.color = Color.rgb(
            (r * 0.52f).toInt().coerceIn(0, 255),
            (g * 0.52f).toInt().coerceIn(0, 255),
            (b * 0.52f).toInt().coerceIn(0, 255)
        )
        canvas.drawLine(inner.left, inner.top, inner.right, inner.top, blockLightEdgePaint)
        canvas.drawLine(inner.left, inner.top, inner.left, inner.bottom, blockLightEdgePaint)
        canvas.drawLine(inner.left, inner.bottom, inner.right, inner.bottom, blockDarkEdgePaint)
        canvas.drawLine(inner.right, inner.top, inner.right, inner.bottom, blockDarkEdgePaint)

        if (hp >= 32) {
            blockOrePaint.color = Color.argb(210, 230, 230, 230)
            val speck = 3f * densityScale
            canvas.drawRect(inner.left + speck, inner.top + speck, inner.left + speck * 2.2f, inner.top + speck * 2.2f, blockOrePaint)
            canvas.drawRect(
                inner.right - speck * 3.5f,
                inner.bottom - speck * 3.5f,
                inner.right - speck,
                inner.bottom - speck,
                blockOrePaint
            )
        } else if (hp in 20..31) {
            blockOrePaint.color = Color.argb(160, 180, 140, 100)
            val speck = 2.5f * densityScale
            canvas.drawRect(
                inner.centerX() - speck,
                inner.centerY() - speck,
                inner.centerX() + speck,
                inner.centerY() + speck,
                blockOrePaint
            )
        }
    }

    private fun drawAnimatedAmbient(
        canvas: Canvas,
        w: Float,
        h: Float,
        theme: BoardVisualTheme,
        timeSec: Float,
        isDark: Boolean
    ) {
        val pulse = sin(timeSec * 0.7f) * 0.5f + 0.5f
        val cx = w * (0.35f + sin(timeSec * 0.25f) * 0.2f)
        val cy = h * (0.2f + cos(timeSec * 0.18f) * 0.08f)
        val radius = w * (0.45f + pulse * 0.2f)
        val accent = if (isDark) theme.launcherColor else theme.ballColor
        ambientPaint.shader = RadialGradient(
            cx, cy, radius,
            intArrayOf(
                Color.argb((22 + pulse * 30).toInt(), Color.red(accent), Color.green(accent), Color.blue(accent)),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, ambientPaint)
        ambientPaint.shader = null

        val cx2 = w * (0.7f + sin(timeSec * 0.2f + 2f) * 0.15f)
        val cy2 = h * 0.55f
        ambientPaint.shader = RadialGradient(
            cx2, cy2, radius * 0.7f,
            intArrayOf(
                Color.argb((12 + pulse * 18).toInt(), Color.red(theme.ballColor), Color.green(theme.ballColor), Color.blue(theme.ballColor)),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, ambientPaint)
        ambientPaint.shader = null
    }

    private fun drawEmberDrift(canvas: Canvas, w: Float, h: Float, theme: BoardVisualTheme, timeSec: Float) {
        for (i in 0 until 14) {
            val phase = timeSec * 0.35f + i * 0.6f
            val x = (w * ((i * 73 + 17) % 100) / 100f) + sin(phase) * 12f
            val y = h - ((phase * 55f + i * 40f) % (h + 60f))
            val alpha = (sin(phase * 2f) * 0.5f + 0.5f) * 120f + 40f
            emberPaint.color = Color.argb(
                alpha.toInt().coerceIn(0, 180),
                Color.red(theme.ballColor),
                Color.green(theme.ballColor),
                Color.blue(theme.ballColor)
            )
            canvas.drawCircle(x, y, (1.5f + (i % 3)) * densityScale, emberPaint)
        }
    }

    private fun drawNebulaBlobs(
        canvas: Canvas,
        w: Float,
        h: Float,
        theme: BoardVisualTheme,
        timeSec: Float,
        reduced: Boolean
    ) {
        val blobCount = if (reduced) 2 else 4
        for (i in 0 until blobCount) {
            val drift = sin(timeSec * 0.15f + i * 1.7f)
            val cx = w * (0.2f + i * 0.22f) + drift * 30f
            val cy = h * (0.15f + (i % 3) * 0.25f) + sin(timeSec * 0.12f + i) * 20f
            val radius = w * (0.18f + sin(timeSec * 0.3f + i) * 0.04f)
            val color = if (i % 2 == 0) theme.launcherColor else theme.ballColor
            nebulaPaint.shader = RadialGradient(
                cx, cy, radius,
                intArrayOf(
                    Color.argb(38, Color.red(color), Color.green(color), Color.blue(color)),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, radius, nebulaPaint)
            nebulaPaint.shader = null
        }
    }

    private fun drawStarfield(canvas: Canvas, w: Float, h: Float, timeSec: Float, reduced: Boolean) {
        val count = if (reduced) 28 else 56
        for (i in 0 until count) {
            val x = ((i * 137 + 53) % 1000) / 1000f * w
            val y = ((i * 251 + 97) % 1000) / 1000f * h
            val twinkle = sin(timeSec * 2.2f + i * 0.85f) * 0.5f + 0.5f
            val alpha = (35 + twinkle * 110).toInt()
            starPaint.color = Color.argb(alpha, 255, 255, 255)
            val radius = (0.8f + twinkle * 1.4f + (i % 2)) * densityScale
            canvas.drawCircle(x, y, radius, starPaint)
        }
    }

    private fun drawBoardGrid(
        canvas: Canvas,
        bounds: com.mostafa.brickblast.game.engine.GameBounds,
        ink: Int,
        timeSec: Float,
        reduced: Boolean
    ) {
        val pulse = sin(timeSec * 1.1f) * 0.5f + 0.5f
        gridPaint.color = Color.argb((14 + pulse * 16).toInt(), Color.red(ink), Color.green(ink), Color.blue(ink))
        val step = 48f * densityScale
        val drift = if (reduced) 0f else sin(timeSec * 0.5f) * 4f
        var x = bounds.left + drift
        while (x <= bounds.right) {
            canvas.drawLine(x, bounds.top, x, bounds.bottom, gridPaint)
            x += step
        }
        var y = bounds.top
        while (y <= bounds.bottom) {
            canvas.drawLine(bounds.left, y, bounds.right, y, gridPaint)
            y += step
        }
    }

    private fun drawOceanWaves(
        canvas: Canvas,
        bounds: com.mostafa.brickblast.game.engine.GameBounds,
        theme: BoardVisualTheme,
        timeSec: Float
    ) {
        wavePaint.color = Color.argb(35, Color.red(theme.ballColor), Color.green(theme.ballColor), Color.blue(theme.ballColor))
        for (i in 0 until 3) {
            val baseY = bounds.top + ((timeSec * 28f + i * 70f) % (bounds.bottom - bounds.top + 80f))
            wavePath.reset()
            var x = bounds.left
            wavePath.moveTo(x, baseY)
            while (x <= bounds.right) {
                val y = baseY + sin((x * 0.03f) + timeSec * 2f + i) * 6f * densityScale
                wavePath.lineTo(x, y)
                x += 12f * densityScale
            }
            canvas.drawPath(wavePath, wavePaint)
        }
    }

    private fun drawBrickGlow(canvas: Canvas, rect: RectF, brickColor: Int, cx: Float, timeSec: Float) {
        val pulse = sin(timeSec * 3.2f + cx * 0.008f) * 0.5f + 0.5f
        brickGlowPaint.strokeWidth = (2.5f + pulse * 4f) * densityScale
        brickGlowPaint.color = Color.argb(
            (55 + pulse * 100).toInt(),
            Color.red(brickColor),
            Color.green(brickColor),
            Color.blue(brickColor)
        )
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, brickGlowPaint)
        brickGlowPaint.color = Color.argb(
            (20 + pulse * 35).toInt(),
            Color.red(brickColor),
            Color.green(brickColor),
            Color.blue(brickColor)
        )
        brickGlowPaint.strokeWidth = (5f + pulse * 5f) * densityScale
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, brickGlowPaint)
    }

    private fun drawBrickShimmer(
        canvas: Canvas,
        rect: RectF,
        cornerRadius: Float,
        timeSec: Float,
        hp: Int
    ) {
        val phase = (timeSec * 1.4f + rect.centerX() * 0.004f + hp * 0.02f) % 1f
        val shineWidth = rect.width() * 0.22f
        val shineX = rect.left + rect.width() * phase
        clipRect.set(
            shineX - shineWidth * 0.5f,
            rect.top,
            shineX + shineWidth * 0.5f,
            rect.bottom
        )
        brickShimmerPaint.shader = LinearGradient(
            clipRect.left, rect.top, clipRect.right, rect.bottom,
            intArrayOf(Color.TRANSPARENT, Color.argb(140, 255, 255, 255), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.save()
        canvas.clipRect(rect)
        canvas.drawRoundRect(clipRect, cornerRadius, cornerRadius, brickShimmerPaint)
        canvas.restore()
        brickShimmerPaint.shader = null
    }

    private fun drawDestroyingBrick(
        canvas: Canvas,
        brick: com.mostafa.brickblast.domain.model.Brick,
        cx: Float,
        cy: Float,
        fast: Boolean,
        theme: BoardVisualTheme,
        timeSec: Float,
        glowBricks: Boolean,
        blockyBricks: Boolean
    ) {
        val p = brick.destroyAnimProgress.coerceIn(0f, 1f)
        val alpha = ((1f - p) * 255f).toInt().coerceIn(0, 255)
        val brickColor = theme.brickColorIntForHp(brick.maxHp)
        brickFillPaint.color = brickColor
        brickFillPaint.alpha = alpha
        if (blockyBricks && !fast) {
            val scale = 1f - p * 0.35f
            val bw = brick.width * scale
            val bh = brick.height * scale
            brickRect.set(cx - bw * 0.5f, cy - bh * 0.5f, cx + bw * 0.5f, cy + bh * 0.5f)
            drawBlockyBrick(canvas, brickRect, brickColor, brick.maxHp)
        } else if (fast) {
            brickRect.set(brick.x, brick.y, brick.x + brick.width, brick.y + brick.height)
            canvas.drawRect(brickRect, brickFillPaint)
        } else {
            val scale = 1f - p * 0.35f
            val bw = brick.width * scale
            val bh = brick.height * scale
            brickRect.set(cx - bw * 0.5f, cy - bh * 0.5f, cx + bw * 0.5f, cy + bh * 0.5f)
            canvas.drawRoundRect(brickRect, cornerRadius, cornerRadius, brickFillPaint)
            if (glowBricks && p < 0.6f) {
                drawBrickGlow(canvas, brickRect, brickFillPaint.color, cx, timeSec)
            }
        }
        brickFillPaint.alpha = 255
    }

    private fun powerUpPaint(type: PowerUpType): Paint = when (type) {
        PowerUpType.MULTI_BALL -> POWER_MULTI_PAINT
        PowerUpType.BOMB -> POWER_BOMB_PAINT
        PowerUpType.LASER -> POWER_LASER_PAINT
        PowerUpType.SLOW_MOTION -> POWER_LASER_PAINT
        PowerUpType.DOUBLE_DAMAGE -> POWER_DAMAGE_PAINT
    }

    private fun composeColorToArgb(color: androidx.compose.ui.graphics.Color, alpha: Int): Int {
        return Color.argb(
            alpha,
            (color.red * 255f).toInt().coerceIn(0, 255),
            (color.green * 255f).toInt().coerceIn(0, 255),
            (color.blue * 255f).toInt().coerceIn(0, 255)
        )
    }

    companion object {
        private val COLLECT_BALL_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#00E5FF") }
        private val COIN_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFD600") }
        private val POWER_MULTI_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#00E676") }
        private val POWER_BOMB_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF5722") }
        private val POWER_LASER_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E040FB") }
        private val POWER_DAMAGE_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF1744") }
    }
}
