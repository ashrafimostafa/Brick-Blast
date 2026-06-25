package com.example.brickblast.game.particle

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class ParticleType {
    EXPLOSION,
    SPARK,
    GLOW
}

/**
 * Single particle instance - mutable for zero-allocation updates.
 */
class Particle {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var life: Float = 0f
    var maxLife: Float = 1f
    var size: Float = 4f
    var color: Color = Color.White
    var type: ParticleType = ParticleType.SPARK

    fun reset() {
        active = false
        x = 0f; y = 0f; vx = 0f; vy = 0f
        life = 0f; maxLife = 1f; size = 4f
    }
}

/**
 * Object-pooled particle system for brick destruction, bounces, and collectables.
 */
class ParticleSystem(poolSize: Int = 512) {
    private val pool = Array(poolSize) { Particle() }
    private var activeCount = 0

    val particles: Array<Particle> get() = pool

    fun emitExplosion(x: Float, y: Float, color: Color, count: Int = 16) {
        repeat(count) {
            val p = acquire() ?: return@repeat
            val angle = Random.nextFloat() * 6.28f
            val speed = Random.nextFloat() * 300f + 100f
            p.x = x; p.y = y
            p.vx = kotlin.math.cos(angle) * speed
            p.vy = kotlin.math.sin(angle) * speed
            p.life = 0f
            p.maxLife = Random.nextFloat() * 0.4f + 0.3f
            p.size = Random.nextFloat() * 6f + 3f
            p.color = color
            p.type = ParticleType.EXPLOSION
            p.active = true
        }
    }

    fun emitSpark(x: Float, y: Float, count: Int = 4) {
        repeat(count) {
            val p = acquire() ?: return@repeat
            val angle = Random.nextFloat() * 6.28f
            val speed = Random.nextFloat() * 200f + 50f
            p.x = x; p.y = y
            p.vx = kotlin.math.cos(angle) * speed
            p.vy = kotlin.math.sin(angle) * speed
            p.life = 0f
            p.maxLife = Random.nextFloat() * 0.2f + 0.1f
            p.size = Random.nextFloat() * 3f + 2f
            p.color = Color(0xFFFFD54F)
            p.type = ParticleType.SPARK
            p.active = true
        }
    }

    fun emitGlowBurst(x: Float, y: Float, color: Color, count: Int = 12) {
        repeat(count) {
            val p = acquire() ?: return@repeat
            val angle = Random.nextFloat() * 6.28f
            val speed = Random.nextFloat() * 150f + 30f
            p.x = x; p.y = y
            p.vx = kotlin.math.cos(angle) * speed
            p.vy = kotlin.math.sin(angle) * speed
            p.life = 0f
            p.maxLife = Random.nextFloat() * 0.5f + 0.3f
            p.size = Random.nextFloat() * 8f + 4f
            p.color = color
            p.type = ParticleType.GLOW
            p.active = true
        }
    }

    fun update(deltaTime: Float) {
        activeCount = 0
        for (p in pool) {
            if (!p.active) continue
            p.life += deltaTime
            if (p.life >= p.maxLife) {
                p.active = false
                continue
            }
            p.x += p.vx * deltaTime
            p.y += p.vy * deltaTime
            p.vy += 400f * deltaTime // gravity for explosion particles
            activeCount++
        }
    }

    private fun acquire(): Particle? {
        for (p in pool) {
            if (!p.active) return p
        }
        return null // pool exhausted
    }

    fun clear() {
        pool.forEach { it.active = false }
        activeCount = 0
    }
}
