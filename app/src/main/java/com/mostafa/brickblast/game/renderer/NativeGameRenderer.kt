package com.mostafa.brickblast.game.renderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.mostafa.brickblast.R
import com.mostafa.brickblast.domain.model.Collectable
import com.mostafa.brickblast.domain.model.GamePhase
import com.mostafa.brickblast.domain.model.PowerUpType
import com.mostafa.brickblast.game.engine.GameEngine
import com.mostafa.brickblast.game.particle.ParticleType

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

    private val bgPaintDark = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0D1B2A") }
    private val bgPaintLight = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F2F5F9") }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * densityScale
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
    private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#64B5F6") }
    private val ballStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * densityScale
        color = Color.argb(89, 255, 255, 255)
    }
    private val launcherPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#448AFF") }
    private val launcherStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * densityScale
    }
    private val aimLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(153, 255, 255, 255)
        strokeWidth = 3f * densityScale
        style = Paint.Style.STROKE
    }
    private val trajPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(217, 128, 216, 255) }
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

    private val persianTypeface: Typeface? = ResourcesCompat.getFont(context, R.font.vazir_regular)
    private val persianBoldTypeface: Typeface? = ResourcesCompat.getFont(context, R.font.vazir_bold)

    private var cachedIdleBalls = -1
    private var cachedIdleLabel = ""

    fun render(
        canvas: Canvas,
        engine: GameEngine,
        trajectoryPoints: FloatArray,
        showTrajectory: Boolean,
        isDark: Boolean,
        persianUi: Boolean
    ) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        canvas.drawRect(0f, 0f, w, h, if (isDark) bgPaintDark else bgPaintLight)

        val ink = if (isDark) Color.WHITE else Color.parseColor("#101418")
        borderPaint.color = Color.argb(26, Color.red(ink), Color.green(ink), Color.blue(ink))
        floorLinePaint.color = Color.argb(41, Color.red(ink), Color.green(ink), Color.blue(ink))
        launcherStrokePaint.color = Color.argb(102, Color.red(ink), Color.green(ink), Color.blue(ink))
        badgePaint.color = ink
        badgePaint.typeface = if (persianUi) persianBoldTypeface else Typeface.DEFAULT_BOLD
        hpTextPaint.typeface = if (persianUi) persianTypeface else Typeface.DEFAULT
        floatTextPaint.typeface = if (persianUi) persianTypeface else Typeface.DEFAULT

        val b = engine.bounds
        boardRect.set(b.left - 4f, b.top - 4f, b.right + 4f, b.bottom + 4f)
        canvas.drawRoundRect(boardRect, 8f * densityScale, 8f * densityScale, borderPaint)
        canvas.drawLine(b.left, b.bottom, b.right, b.bottom, floorLinePaint)

        val activeBalls = engine.activeBallCount
        val heavyLoad = activeBalls > 30
        val skipBallStroke = activeBalls > 20
        val skipBrickStroke = activeBalls > 15
        val skipBrickHp = activeBalls > 25
        val skipParticles = activeBalls > 40 ||
            engine.phase == GamePhase.LAUNCHING ||
            engine.phase == GamePhase.RECALLING
        val skipFloatText = heavyLoad
        val useRoundBricks = !heavyLoad

        for (brick in engine.bricks) {
            if (brick.hp <= 0 && !brick.isDestroying) continue
            val cx = brick.x + brick.width * 0.5f
            val cy = brick.y + brick.height * 0.5f

            if (brick.isDestroying) {
                drawDestroyingBrick(canvas, brick, cx, cy, heavyLoad)
                continue
            }

            brickFillPaint.color = GameEngine.brickColorIntForHp(brick.hp)
            brickRect.set(brick.x, brick.y, brick.x + brick.width, brick.y + brick.height)
            if (useRoundBricks) {
                canvas.drawRoundRect(brickRect, cornerRadius, cornerRadius, brickFillPaint)
                if (!skipBrickStroke) {
                    canvas.drawRoundRect(brickRect, cornerRadius, cornerRadius, brickStrokePaint)
                }
            } else {
                canvas.drawRect(brickRect, brickFillPaint)
            }

            if (!skipBrickHp) {
                val textY = cy - (hpTextPaint.descent() + hpTextPaint.ascent()) * 0.5f
                canvas.drawText(brick.hp.toString(), cx, textY, hpTextPaint)
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

        canvas.drawCircle(engine.launcherX, engine.launcherY, 14f * densityScale, launcherPaint)
        canvas.drawCircle(engine.launcherX, engine.launcherY, 14f * densityScale, launcherStrokePaint)

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

    private fun drawDestroyingBrick(
        canvas: Canvas,
        brick: com.mostafa.brickblast.domain.model.Brick,
        cx: Float,
        cy: Float,
        fast: Boolean
    ) {
        val p = brick.destroyAnimProgress.coerceIn(0f, 1f)
        val alpha = ((1f - p) * 255f).toInt().coerceIn(0, 255)
        brickFillPaint.color = GameEngine.brickColorIntForHp(brick.maxHp)
        brickFillPaint.alpha = alpha
        if (fast) {
            brickRect.set(brick.x, brick.y, brick.x + brick.width, brick.y + brick.height)
            canvas.drawRect(brickRect, brickFillPaint)
        } else {
            val scale = 1f - p * 0.35f
            val w = brick.width * scale
            val h = brick.height * scale
            brickRect.set(cx - w * 0.5f, cy - h * 0.5f, cx + w * 0.5f, cy + h * 0.5f)
            canvas.drawRoundRect(brickRect, cornerRadius, cornerRadius, brickFillPaint)
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
