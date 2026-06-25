package com.mostafa.brickblast.game.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mostafa.brickblast.domain.model.Collectable
import com.mostafa.brickblast.domain.model.GamePhase
import com.mostafa.brickblast.domain.model.PowerUpType
import com.mostafa.brickblast.game.engine.GameEngine

/**
 * Canvas renderer for the game world. Called from GameCanvas composable
 * to avoid unnecessary recompositions - only draws, no state changes.
 */
object GameRenderer {

    private val darkBackgroundBrush = Brush.verticalGradient(
        listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF0D1B2A))
    )
    private val lightBackgroundBrush = Brush.verticalGradient(
        listOf(Color(0xFFFFFFFF), Color(0xFFEEF3F8), Color(0xFFFFFFFF))
    )
    private val brickHpStyle = TextStyle(fontSize = 14.sp, color = Color.White)
    private val floatingTextStyle = TextStyle(fontSize = 16.sp)
    private val badgeStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
    private val launcherChevron = Path()
    private val hpTextCache = HashMap<Int, TextLayoutResult>(64)
    private val textCache = HashMap<String, TextLayoutResult>(32)
    private var cachedIdleBalls = -1
    private var cachedIdleBadge: TextLayoutResult? = null

    fun DrawScope.renderGame(
        engine: GameEngine,
        trajectoryPoints: FloatArray,
        showTrajectory: Boolean,
        isDark: Boolean,
        textMeasurer: androidx.compose.ui.text.TextMeasurer
    ) {
        val ink = if (isDark) Color.White else Color(0xFF101418)

        drawRect(brush = if (isDark) darkBackgroundBrush else lightBackgroundBrush)

        val b = engine.bounds
        drawRoundRect(
            color = ink.copy(alpha = 0.10f),
            topLeft = Offset(b.left - 4f, b.top - 4f),
            size = Size(b.right - b.left + 8f, b.bottom - b.top + 8f),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(2f)
        )
        drawLine(
            ink.copy(alpha = 0.16f),
            Offset(b.left, b.bottom),
            Offset(b.right, b.bottom),
            strokeWidth = 2f
        )

        for (brick in engine.bricks) {
            if (brick.hp <= 0 && !brick.isDestroying) continue
            val cx = brick.x + brick.width / 2
            val cy = brick.y + brick.height / 2

            if (brick.isDestroying) {
                renderDestroyingBrick(brick, cx, cy)
                continue
            }

            val color = GameEngine.brickColorForHp(brick.hp)
            drawRoundRect(
                color = color,
                topLeft = Offset(brick.x, brick.y),
                size = Size(brick.width, brick.height),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.15f),
                topLeft = Offset(brick.x, brick.y),
                size = Size(brick.width, brick.height),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(1.5f)
            )
            val textLayout = hpTextCache.getOrPut(brick.hp) {
                textMeasurer.measure(brick.hp.toString(), brickHpStyle)
            }
            drawText(
                textLayout,
                topLeft = Offset(cx - textLayout.size.width / 2, cy - textLayout.size.height / 2)
            )
        }

        for (c in engine.collectables) {
            if (c.collected) continue
            when (c) {
                is Collectable.ExtraBall -> {
                    drawCircle(Color(0xFF00E5FF), c.radius, Offset(c.x, c.y))
                    drawCircle(Color.White.copy(0.5f), c.radius, Offset(c.x, c.y), style = Stroke(2f))
                }
                is Collectable.Coin -> {
                    drawCircle(Color(0xFFFFD600), c.radius, Offset(c.x, c.y))
                }
                is Collectable.PowerUpCollectable -> {
                    val color = powerUpColor(c.powerUpType)
                    drawCircle(color, c.radius, Offset(c.x, c.y))
                    drawCircle(Color.White.copy(0.6f), c.radius * 0.5f, Offset(c.x, c.y))
                }
            }
        }

        for (ball in engine.balls) {
            if (!ball.launched && !ball.active) continue
            if (ball.active) {
                drawCircle(Color(0xFF64B5F6), ball.radius, Offset(ball.x, ball.y))
                drawCircle(Color.White.copy(0.35f), ball.radius, Offset(ball.x, ball.y), style = Stroke(1.5f))
            }
        }

        if (engine.hasNextLauncher && engine.phase == GamePhase.SIMULATING) {
            val mx = engine.nextLauncherX
            val my = engine.launcherY
            drawCircle(Color(0xFF80D8FF).copy(0.5f), 12f, Offset(mx, my), style = Stroke(2f))
            launcherChevron.reset()
            launcherChevron.moveTo(mx - 7f, my - 22f)
            launcherChevron.lineTo(mx, my - 14f)
            launcherChevron.lineTo(mx + 7f, my - 22f)
            drawPath(launcherChevron, Color(0xFF80D8FF).copy(0.7f), style = Stroke(2f))
        }

        drawCircle(Color(0xFF448AFF), 14f, Offset(engine.launcherX, engine.launcherY))
        drawCircle(ink.copy(0.4f), 14f, Offset(engine.launcherX, engine.launcherY), style = Stroke(2f))

        var idleBalls = 0
        for (ball in engine.balls) if (!ball.launched) idleBalls++
        if (idleBalls > 0) {
            if (idleBalls != cachedIdleBalls) {
                cachedIdleBalls = idleBalls
                cachedIdleBadge = textMeasurer.measure("×$idleBalls", badgeStyle.copy(color = ink))
            }
            cachedIdleBadge?.let { badgeLayout ->
                drawText(
                    badgeLayout,
                    topLeft = Offset(
                        engine.launcherX - badgeLayout.size.width / 2f,
                        engine.launcherY - 44f
                    )
                )
            }
        } else {
            cachedIdleBalls = -1
        }

        if (showTrajectory && trajectoryPoints.size >= 4) {
            val dotSpacing = 32f
            var distSinceLastDot = dotSpacing
            var prevX = trajectoryPoints[0]
            var prevY = trajectoryPoints[1]
            if (prevY <= b.bottom) {
                drawCircle(Color(0xFF80D8FF).copy(0.85f), 6f, Offset(prevX, prevY))
            }
            var i = 2
            while (i < trajectoryPoints.size) {
                val x = trajectoryPoints[i]
                val y = trajectoryPoints[i + 1]
                if (y <= 0f || y > b.bottom) break
                val segDx = x - prevX
                val segDy = y - prevY
                val segLen = kotlin.math.sqrt(segDx * segDx + segDy * segDy)
                if (segLen > 0.01f) {
                    var traveled = 0f
                    while (traveled + distSinceLastDot <= segLen) {
                        traveled += distSinceLastDot
                        val t = traveled / segLen
                        drawCircle(
                            Color(0xFF80D8FF).copy(0.85f),
                            6f,
                            Offset(prevX + segDx * t, prevY + segDy * t)
                        )
                        distSinceLastDot = dotSpacing
                    }
                    distSinceLastDot += segLen - traveled
                }
                prevX = x
                prevY = y
                i += 2
            }
        }

        if (engine.isAiming) {
            drawLine(
                Color.White.copy(0.6f),
                Offset(engine.aimStartX, engine.aimStartY),
                Offset(engine.aimEndX, engine.aimEndY),
                strokeWidth = 3f
            )
        }

        engine.particles.forEachActive { p ->
            renderParticle(p)
        }

        for (ft in engine.floatingTexts) {
            val layout = textCache.getOrPut(ft.text) {
                textMeasurer.measure(ft.text, floatingTextStyle)
            }
            drawText(
                layout,
                topLeft = Offset(ft.x - layout.size.width / 2, ft.y + ft.offsetY),
                color = Color(0xFFFFD600).copy(alpha = ft.alpha)
            )
        }
    }

    private fun DrawScope.renderDestroyingBrick(
        brick: com.mostafa.brickblast.domain.model.Brick,
        cx: Float,
        cy: Float
    ) {
        val p = brick.destroyAnimProgress.coerceIn(0f, 1f)
        val alpha = 1f - p
        val scale = 1f - p * 0.35f
        val w = brick.width * scale
        val h = brick.height * scale
        val baseColor = GameEngine.brickColorForHp(brick.maxHp)
        drawRoundRect(
            color = baseColor.copy(alpha = alpha),
            topLeft = Offset(cx - w / 2f, cy - h / 2f),
            size = Size(w, h),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )
        if (p < 0.25f) {
            val flash = 1f - p / 0.25f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.7f * flash),
                topLeft = Offset(brick.x, brick.y),
                size = Size(brick.width, brick.height),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
    }

    private fun DrawScope.renderParticle(p: com.mostafa.brickblast.game.particle.Particle) {
        val alpha = 1f - (p.life / p.maxLife)
        val base = p.color
        val r = (base.red * 255f).toInt().coerceIn(0, 255)
        val g = (base.green * 255f).toInt().coerceIn(0, 255)
        val b = (base.blue * 255f).toInt().coerceIn(0, 255)
        val color = Color(r, g, b, (alpha * 255f).toInt().coerceIn(0, 255))
        when (p.type) {
            com.mostafa.brickblast.game.particle.ParticleType.EXPLOSION ->
                drawCircle(color, p.size * (1f + p.life), Offset(p.x, p.y))
            com.mostafa.brickblast.game.particle.ParticleType.SPARK ->
                drawCircle(color, p.size, Offset(p.x, p.y))
            com.mostafa.brickblast.game.particle.ParticleType.GLOW ->
                drawCircle(
                    Color(r, g, b, (alpha * 0.6f * 255f).toInt().coerceIn(0, 255)),
                    p.size * 2,
                    Offset(p.x, p.y)
                )
        }
    }

    private fun powerUpColor(type: PowerUpType): Color = when (type) {
        PowerUpType.MULTI_BALL -> Color(0xFF00E676)
        PowerUpType.BOMB -> Color(0xFFFF5722)
        PowerUpType.LASER -> Color(0xFFE040FB)
        PowerUpType.SLOW_MOTION -> Color(0xFFE040FB)
        PowerUpType.DOUBLE_DAMAGE -> Color(0xFFFF1744)
    }
}
