package com.mostafa.brickblast.game.particle

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
class ParticleSystem(poolSize: Int = 256) {
    private val pool = Array(poolSize) { Particle() }
    private val activeIndices = IntArray(poolSize)
    private val freeIndices = IntArray(poolSize)
    private var activeListSize = 0
    private var freeListSize = poolSize

    init {
        for (i in pool.indices) {
            freeIndices[i] = i
        }
    }

    fun forEachActive(block: (Particle) -> Unit) {
        for (i in 0 until activeListSize) {
            block(pool[activeIndices[i]])
        }
    }

    fun emitExplosion(x: Float, y: Float, color: Color, count: Int = 6) {
        repeat(count) {
            val index = acquireIndex() ?: return@repeat
            val p = pool[index]
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

    fun emitSpark(x: Float, y: Float, count: Int = 2) {
        repeat(count) {
            val index = acquireIndex() ?: return@repeat
            val p = pool[index]
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

    fun emitGlowBurst(x: Float, y: Float, color: Color, count: Int = 6) {
        repeat(count) {
            val index = acquireIndex() ?: return@repeat
            val p = pool[index]
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
        var write = 0
        for (i in 0 until activeListSize) {
            val index = activeIndices[i]
            val p = pool[index]
            p.life += deltaTime
            if (p.life >= p.maxLife) {
                p.active = false
                releaseIndex(index)
                continue
            }
            p.x += p.vx * deltaTime
            p.y += p.vy * deltaTime
            p.vy += 400f * deltaTime
            activeIndices[write++] = index
        }
        activeListSize = write
    }

    private fun acquireIndex(): Int? {
        if (freeListSize == 0) return null
        val index = freeIndices[--freeListSize]
        activeIndices[activeListSize++] = index
        return index
    }

    private fun releaseIndex(index: Int) {
        freeIndices[freeListSize++] = index
    }

    fun clear() {
        for (i in 0 until activeListSize) {
            val index = activeIndices[i]
            pool[index].active = false
            freeIndices[freeListSize++] = index
        }
        activeListSize = 0
    }
}
