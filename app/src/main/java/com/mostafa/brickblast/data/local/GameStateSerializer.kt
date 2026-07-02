package com.mostafa.brickblast.data.local

import com.mostafa.brickblast.domain.model.Brick
import com.mostafa.brickblast.domain.model.Collectable
import com.mostafa.brickblast.domain.model.PowerUpType
import com.mostafa.brickblast.game.engine.GameEngine
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class BrickSave(
    val id: Long,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val hp: Int,
    val maxHp: Int,
    val isDestroying: Boolean = false,
    val destroyAnimProgress: Float = 0f
)

@Serializable
private data class CollectableSave(
    val type: String,
    val id: Long,
    val x: Float,
    val y: Float,
    val radius: Float,
    val collected: Boolean,
    val amount: Int? = null,
    val powerUpType: String? = null
)

object GameStateSerializer {
    private val json = Json { ignoreUnknownKeys = true }

    fun serializeBricks(bricks: List<Brick>): String {
        val payload = bricks
            .filter { it.hp > 0 || it.isDestroying }
            .map {
                BrickSave(
                    id = it.id,
                    x = it.x,
                    y = it.y,
                    width = it.width,
                    height = it.height,
                    hp = it.hp,
                    maxHp = it.maxHp,
                    isDestroying = it.isDestroying,
                    destroyAnimProgress = it.destroyAnimProgress
                )
            }
        return json.encodeToString(payload)
    }

    fun serializeCollectables(collectables: List<Collectable>): String {
        val payload = collectables
            .filter { !it.collected }
            .map { c ->
                when (c) {
                    is Collectable.ExtraBall -> CollectableSave(
                        type = "EXTRA_BALL",
                        id = c.id,
                        x = c.x,
                        y = c.y,
                        radius = c.radius,
                        collected = c.collected
                    )
                    is Collectable.Coin -> CollectableSave(
                        type = "COIN",
                        id = c.id,
                        x = c.x,
                        y = c.y,
                        radius = c.radius,
                        collected = c.collected,
                        amount = c.amount
                    )
                    is Collectable.PowerUpCollectable -> CollectableSave(
                        type = "POWER_UP",
                        id = c.id,
                        x = c.x,
                        y = c.y,
                        radius = c.radius,
                        collected = c.collected,
                        powerUpType = c.powerUpType.name
                    )
                }
            }
        return json.encodeToString(payload)
    }

    fun restoreInto(engine: GameEngine, bricksJson: String, collectablesJson: String) {
        if (bricksJson.isNotBlank()) {
            val saved = json.decodeFromString<List<BrickSave>>(bricksJson)
            engine.bricks.clear()
            var maxId = 0L
            for (b in saved) {
                engine.bricks.add(
                    Brick(
                        id = b.id,
                        x = b.x,
                        y = b.y,
                        width = b.width,
                        height = b.height,
                        hp = b.hp,
                        maxHp = b.maxHp,
                        destroyAnimProgress = b.destroyAnimProgress,
                        isDestroying = b.isDestroying
                    )
                )
                maxId = maxOf(maxId, b.id)
            }
            engine.setNextBrickId(maxId + 1)
        }

        if (collectablesJson.isNotBlank()) {
            val saved = json.decodeFromString<List<CollectableSave>>(collectablesJson)
            engine.collectables.clear()
            var maxId = 0L
            for (c in saved) {
                val collectable: Collectable = when (c.type) {
                    "COIN" -> Collectable.Coin(
                        id = c.id,
                        x = c.x,
                        y = c.y,
                        radius = c.radius,
                        collected = c.collected,
                        amount = c.amount ?: 1
                    )
                    "POWER_UP" -> Collectable.PowerUpCollectable(
                        id = c.id,
                        x = c.x,
                        y = c.y,
                        radius = c.radius,
                        collected = c.collected,
                        powerUpType = PowerUpType.valueOf(c.powerUpType ?: PowerUpType.MULTI_BALL.name)
                    )
                    else -> Collectable.ExtraBall(
                        id = c.id,
                        x = c.x,
                        y = c.y,
                        radius = c.radius,
                        collected = c.collected
                    )
                }
                engine.collectables.add(collectable)
                maxId = maxOf(maxId, c.id)
            }
            engine.setNextCollectableId(maxId + 1)
        }
    }
}
