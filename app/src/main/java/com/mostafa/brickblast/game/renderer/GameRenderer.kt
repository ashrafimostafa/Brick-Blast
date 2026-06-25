package com.mostafa.brickblast.game.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mostafa.brickblast.domain.model.Collectable
import com.mostafa.brickblast.domain.model.GamePhase
import com.mostafa.brickblast.domain.model.PowerUpType
import com.mostafa.brickblast.game.engine.GameEngine
import com.mostafa.brickblast.game.particle.Particle
import com.mostafa.brickblast.game.particle.ParticleType

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

    fun DrawScope.renderGame(
        engine: GameEngine,
        trajectoryPoints: FloatArray,
        showTrajectory: Boolean,
        isDark: Boolean,
        textMeasurer: androidx.compose.ui.text.TextMeasurer
    ) {
        // Theme-aware foreground tint: light lines on dark bg, dark lines on light bg.
        val ink = if (isDark) Color.White else Color(0xFF101418)

        // Background
        drawRect(brush = if (isDark) darkBackgroundBrush else lightBackgroundBrush)

        // Compact play-field border (smaller area = faster simulation & rendering)
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

        // Grid lines (subtle, only inside play field)
        val gridColor = ink.copy(alpha = 0.04f)
        val step = (b.right - b.left) / 6f
        var x = b.left
        while (x <= b.right) {
            drawLine(gridColor, Offset(x, b.top), Offset(x, b.bottom), 1f)
            x += step
        }

        // Bricks (skip only fully-dead bricks; destroying ones still animate)
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
            val hpText = brick.hp.toString()
            val textLayout = textMeasurer.measure(hpText, TextStyle(fontSize = 14.sp, color = Color.White))
            drawText(
                textLayout,
                topLeft = Offset(cx - textLayout.size.width / 2, cy - textLayout.size.height / 2)
            )
        }

        // Collectables
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

        // Balls — solid fill (no per-ball gradient) for smoother rendering
        for (ball in engine.balls) {
            if (!ball.launched && !ball.active) continue
            if (ball.active) {
                drawCircle(Color(0xFF64B5F6), ball.radius, Offset(ball.x, ball.y))
                drawCircle(Color.White.copy(0.35f), ball.radius, Offset(ball.x, ball.y), style = Stroke(1.5f))
            }
        }

        // Marker showing where the launcher will move to next round (the spot
        // where the first ball returned). Shown while the balls are in flight.
        if (engine.hasNextLauncher && engine.phase == GamePhase.SIMULATING) {
            val mx = engine.nextLauncherX
            val my = engine.launcherY
            drawCircle(Color(0xFF80D8FF).copy(0.5f), 12f, Offset(mx, my), style = Stroke(2f))
            // Small downward chevron above the marker.
            val chevron = Path().apply {
                moveTo(mx - 7f, my - 22f)
                lineTo(mx, my - 14f)
                lineTo(mx + 7f, my - 22f)
            }
            drawPath(chevron, Color(0xFF80D8FF).copy(0.7f), style = Stroke(2f))
        }

        // Launcher indicator
        drawCircle(Color(0xFF448AFF), 14f, Offset(engine.launcherX, engine.launcherY))
        drawCircle(ink.copy(0.4f), 14f, Offset(engine.launcherX, engine.launcherY), style = Stroke(2f))

        // Ball-count badge at the launcher. Idle balls stack on the same point,
        // so this tells the player how many balls they actually have / are waiting.
        var idleBalls = 0
        for (b in engine.balls) if (!b.launched) idleBalls++
        if (idleBalls > 0) {
            val badge = "×$idleBalls"
            val badgeLayout = textMeasurer.measure(
                badge,
                TextStyle(fontSize = 20.sp, color = ink, fontWeight = FontWeight.Bold)
            )
            drawText(
                badgeLayout,
                topLeft = Offset(
                    engine.launcherX - badgeLayout.size.width / 2f,
                    engine.launcherY - 44f
                )
            )
        }

        // Trajectory preview — spaced dots so the player sees individual balls
        if (showTrajectory && trajectoryPoints.size >= 4) {
            val dotSpacing = 28f
            var distSinceLastDot = dotSpacing
            var prevX = trajectoryPoints[0]
            var prevY = trajectoryPoints[1]
            drawCircle(Color(0xFF80D8FF).copy(0.85f), 6f, Offset(prevX, prevY))
            var i = 2
            while (i < trajectoryPoints.size) {
                val x = trajectoryPoints[i]
                val y = trajectoryPoints[i + 1]
                if (y > b.bottom) break
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

        // Aim line while dragging
        if (engine.isAiming) {
            drawLine(
                Color.White.copy(0.6f),
                Offset(engine.aimStartX, engine.aimStartY),
                Offset(engine.aimEndX, engine.aimEndY),
                strokeWidth = 3f
            )
        }

        // Particles
        for (p in engine.particles.particles) {
            if (!p.active) continue
            renderParticle(p)
        }

        // Floating score texts
        for (ft in engine.floatingTexts) {
            val layout = textMeasurer.measure(ft.text, TextStyle(fontSize = 16.sp, color = Color(0xFFFFD600).copy(alpha = ft.alpha)))
            drawText(layout, topLeft = Offset(ft.x - layout.size.width / 2, ft.y + ft.offsetY))
        }
    }

    /**
     * Destruction animation: the brick flashes white, splits into four corner
     * shards that fly outward while spinning, and a shock ring expands. Driven by
     * brick.destroyAnimProgress (0..1), advanced by the engine each frame.
     */
    private fun DrawScope.renderDestroyingBrick(
        brick: com.mostafa.brickblast.domain.model.Brick,
        cx: Float,
        cy: Float
    ) {
        val p = brick.destroyAnimProgress.coerceIn(0f, 1f)
        val alpha = (1f - p)
        val baseColor = GameEngine.brickColorForHp(brick.maxHp)

        // Expanding shock ring.
        val ringRadius = (brick.width * 0.5f) + p * brick.width * 1.2f
        drawCircle(
            color = Color.White.copy(alpha = 0.5f * (1f - p)),
            radius = ringRadius,
            center = Offset(cx, cy),
            style = Stroke(2f + 3f * (1f - p))
        )

        // Four shards from each quadrant, flying apart with a slight spin.
        val shardW = brick.width * 0.42f
        val shardH = brick.height * 0.42f
        val travel = p * brick.width * 0.9f
        val shardAlpha = alpha
        val dirs = arrayOf(
            (-1f) to (-1f), (1f) to (-1f), (-1f) to (1f), (1f) to (1f)
        )
        for ((dx, dy) in dirs) {
            val sx = cx + dx * (brick.width * 0.22f + travel)
            val sy = cy + dy * (brick.height * 0.22f + travel) + p * p * 40f // slight gravity
            rotate(degrees = p * 60f * dx, pivot = Offset(sx, sy)) {
                drawRoundRect(
                    color = baseColor.copy(alpha = shardAlpha),
                    topLeft = Offset(sx - shardW / 2f, sy - shardH / 2f),
                    size = Size(shardW, shardH),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
        }

        // Initial white flash over the whole brick footprint.
        if (p < 0.35f) {
            val flash = (1f - p / 0.35f)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.8f * flash),
                topLeft = Offset(brick.x, brick.y),
                size = Size(brick.width, brick.height),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
    }

    private fun DrawScope.renderParticle(p: Particle) {
        val alpha = 1f - (p.life / p.maxLife)
        val color = p.color.copy(alpha = alpha)
        when (p.type) {
            ParticleType.EXPLOSION -> drawCircle(color, p.size * (1f + p.life), Offset(p.x, p.y))
            ParticleType.SPARK -> drawCircle(color, p.size, Offset(p.x, p.y))
            ParticleType.GLOW -> drawCircle(color.copy(alpha = alpha * 0.6f), p.size * 2, Offset(p.x, p.y))
        }
    }

    fun DrawScope.renderHud(
        score: Int,
        bestScore: Int,
        round: Int,
        totalBalls: Int,
        coins: Int,
        timeRemaining: Float?,
        isDark: Boolean,
        textMeasurer: androidx.compose.ui.text.TextMeasurer
    ) {
        val ink = if (isDark) Color.White else Color(0xFF101418)
        val isNewBest = score > bestScore
        // Effective best shown to the player tracks the live score once beaten.
        val shownBest = maxOf(score, bestScore)

        // --- Top-center score block ---
        val topY = 54f
        // BEST label line.
        val bestLabel = "BEST  $shownBest"
        val bestStyle = TextStyle(
            fontSize = 14.sp,
            color = if (isNewBest) Color(0xFFFFB300) else ink.copy(0.6f),
            fontWeight = if (isNewBest) FontWeight.Bold else FontWeight.Normal
        )
        val bestLayout = textMeasurer.measure(bestLabel, bestStyle)
        drawText(bestLayout, topLeft = Offset(size.width / 2f - bestLayout.size.width / 2f, topY))

        // Current score, large. Turns gold when it is a new record.
        val scoreColor = if (isNewBest) Color(0xFFFFB300) else ink
        val scoreLayout = textMeasurer.measure(
            score.toString(),
            TextStyle(fontSize = 34.sp, color = scoreColor, fontWeight = FontWeight.Bold)
        )
        val scoreY = topY + bestLayout.size.height + 4f
        drawText(scoreLayout, topLeft = Offset(size.width / 2f - scoreLayout.size.width / 2f, scoreY))

        // "NEW BEST!" badge under the score when the record is being beaten.
        if (isNewBest) {
            val badge = textMeasurer.measure(
                "NEW BEST!",
                TextStyle(fontSize = 13.sp, color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
            )
            drawText(
                badge,
                topLeft = Offset(
                    size.width / 2f - badge.size.width / 2f,
                    scoreY + scoreLayout.size.height + 2f
                )
            )
        }

        // --- Left stats column ---
        val style = TextStyle(fontSize = 15.sp, color = ink.copy(0.85f))
        val left = 24f
        var y = topY
        val lines = buildList {
            add("Round  $round")
            add("Balls  $totalBalls")
            add("Coins  $coins")
            timeRemaining?.let { add("Time  ${it.toInt()}s") }
        }
        for (line in lines) {
            val layout = textMeasurer.measure(line, style)
            drawText(layout, topLeft = Offset(left, y))
            y += layout.size.height + 6f
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
