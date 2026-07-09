package com.mostafa.brickblast.domain.model

import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor

object ColorPackIds {
    const val CLASSIC = "classic"
    const val NEON_NIGHT = "neon_night"
    const val SUNSET_BLAZE = "sunset_blaze"
    const val OCEAN_DEPTH = "ocean_depth"
    const val ROYAL_GOLD = "royal_gold"
    const val GALAXY = "galaxy"
    const val BLOCK_WORLD = "block_world"
}

enum class BoardFeature {
    GLOW_BRICKS,
    STARFIELD_BG,
    GRID_BG,
    SHIMMER_HIGH_HP,
    ANIMATED_BG,
    BALL_GLOW,
    LAUNCHER_AURA,
    EMBER_DRIFT,
    WAVE_OVERLAY,
    BLOCKY_BRICKS,
    VOXEL_WORLD_BG
}

data class ColorPackDefinition(
    val id: String,
    val price: Int,
    val bgDark: String,
    val bgLight: String,
    val ballColor: String,
    val launcherColor: String,
    val trajectoryColor: String,
    val brickLowHp: Triple<Int, Int, Int>,
    val brickHighHp: Triple<Int, Int, Int>,
    val features: Set<BoardFeature> = emptySet()
)

object ColorPackDefinitions {
    val CLASSIC = ColorPackDefinition(
        id = ColorPackIds.CLASSIC,
        price = 0,
        bgDark = "#0D1B2A",
        bgLight = "#F2F5F9",
        ballColor = "#64B5F6",
        launcherColor = "#448AFF",
        trajectoryColor = "#80D8FF",
        brickLowHp = Triple(100, 100, 155),
        brickHighHp = Triple(255, 50, 50)
    )

    val NEON_NIGHT = ColorPackDefinition(
        id = ColorPackIds.NEON_NIGHT,
        price = 3_000,
        bgDark = "#0A0014",
        bgLight = "#1A0A2E",
        ballColor = "#00E5FF",
        launcherColor = "#E040FB",
        trajectoryColor = "#FF00FF",
        brickLowHp = Triple(0, 229, 255),
        brickHighHp = Triple(255, 0, 200),
        features = setOf(
            BoardFeature.GLOW_BRICKS,
            BoardFeature.ANIMATED_BG,
            BoardFeature.BALL_GLOW,
            BoardFeature.LAUNCHER_AURA
        )
    )

    val SUNSET_BLAZE = ColorPackDefinition(
        id = ColorPackIds.SUNSET_BLAZE,
        price = 8_000,
        bgDark = "#1A0A00",
        bgLight = "#FFF3E0",
        ballColor = "#FF6D00",
        launcherColor = "#FF3D00",
        trajectoryColor = "#FFAB40",
        brickLowHp = Triple(255, 213, 79),
        brickHighHp = Triple(211, 47, 47),
        features = setOf(
            BoardFeature.SHIMMER_HIGH_HP,
            BoardFeature.ANIMATED_BG,
            BoardFeature.EMBER_DRIFT,
            BoardFeature.BALL_GLOW
        )
    )

    val OCEAN_DEPTH = ColorPackDefinition(
        id = ColorPackIds.OCEAN_DEPTH,
        price = 18_000,
        bgDark = "#001A33",
        bgLight = "#E0F7FA",
        ballColor = "#00BCD4",
        launcherColor = "#0288D1",
        trajectoryColor = "#4DD0E1",
        brickLowHp = Triple(128, 222, 234),
        brickHighHp = Triple(1, 87, 155),
        features = setOf(
            BoardFeature.GRID_BG,
            BoardFeature.WAVE_OVERLAY,
            BoardFeature.ANIMATED_BG
        )
    )

    val ROYAL_GOLD = ColorPackDefinition(
        id = ColorPackIds.ROYAL_GOLD,
        price = 35_000,
        bgDark = "#1A0A2E",
        bgLight = "#FFF8E1",
        ballColor = "#FFD700",
        launcherColor = "#9C27B0",
        trajectoryColor = "#E1BEE7",
        brickLowHp = Triple(255, 215, 0),
        brickHighHp = Triple(106, 27, 154),
        features = setOf(
            BoardFeature.GLOW_BRICKS,
            BoardFeature.SHIMMER_HIGH_HP,
            BoardFeature.ANIMATED_BG,
            BoardFeature.BALL_GLOW,
            BoardFeature.LAUNCHER_AURA
        )
    )

    val GALAXY = ColorPackDefinition(
        id = ColorPackIds.GALAXY,
        price = 75_000,
        bgDark = "#0D0221",
        bgLight = "#1A0533",
        ballColor = "#CE93D8",
        launcherColor = "#7C4DFF",
        trajectoryColor = "#B388FF",
        brickLowHp = Triple(179, 157, 219),
        brickHighHp = Triple(233, 30, 99),
        features = setOf(
            BoardFeature.GLOW_BRICKS,
            BoardFeature.STARFIELD_BG,
            BoardFeature.ANIMATED_BG,
            BoardFeature.BALL_GLOW,
            BoardFeature.LAUNCHER_AURA
        )
    )

    val BLOCK_WORLD = ColorPackDefinition(
        id = ColorPackIds.BLOCK_WORLD,
        price = 25_000,
        bgDark = "#78A7FF",
        bgLight = "#78A7FF",
        ballColor = "#55FF55",
        launcherColor = "#8B5A2B",
        trajectoryColor = "#FFFFFF",
        brickLowHp = Triple(106, 170, 48),
        brickHighHp = Triple(60, 60, 60),
        features = setOf(
            BoardFeature.BLOCKY_BRICKS,
            BoardFeature.VOXEL_WORLD_BG
        )
    )

    val ALL = listOf(CLASSIC, NEON_NIGHT, SUNSET_BLAZE, OCEAN_DEPTH, ROYAL_GOLD, GALAXY, BLOCK_WORLD)

    fun byId(id: String): ColorPackDefinition? = ALL.find { it.id == id }
}

class BoardVisualTheme private constructor(
    val id: String,
    val bgDark: Int,
    val bgLight: Int,
    val ballColor: Int,
    val launcherColor: Int,
    val trajectoryColor: Int,
    private val brickColorInts: IntArray,
    private val brickColors: Array<Color>,
    val features: Set<BoardFeature>
) {
    val isPremium: Boolean get() = id != ColorPackIds.CLASSIC

    fun brickColorIntForHp(hp: Int): Int = brickColorInts[hp.coerceIn(1, 50)]
    fun brickColorForHp(hp: Int): Color = brickColors[hp.coerceIn(1, 50)]

    companion object {
        val Classic: BoardVisualTheme = fromDefinition(ColorPackDefinitions.CLASSIC)

        fun fromPackId(id: String): BoardVisualTheme {
            val def = ColorPackDefinitions.byId(id) ?: ColorPackDefinitions.CLASSIC
            return fromDefinition(def)
        }

        fun fromDefinition(def: ColorPackDefinition): BoardVisualTheme {
            val ints = IntArray(51)
            val colors = Array(51) { Color.Black }
            for (hp in 1..50) {
                val ratio = hp / 50f
                val (r1, g1, b1) = def.brickLowHp
                val (r2, g2, b2) = def.brickHighHp
                val r = (r1 + (r2 - r1) * ratio).toInt().coerceIn(0, 255)
                val g = (g1 + (g2 - g1) * ratio).toInt().coerceIn(0, 255)
                val b = (b1 + (b2 - b1) * ratio).toInt().coerceIn(0, 255)
                ints[hp] = AndroidColor.rgb(r, g, b)
                colors[hp] = Color(r, g, b)
            }
            return BoardVisualTheme(
                id = def.id,
                bgDark = AndroidColor.parseColor(def.bgDark),
                bgLight = AndroidColor.parseColor(def.bgLight),
                ballColor = AndroidColor.parseColor(def.ballColor),
                launcherColor = AndroidColor.parseColor(def.launcherColor),
                trajectoryColor = AndroidColor.parseColor(def.trajectoryColor),
                brickColorInts = ints,
                brickColors = colors,
                features = def.features
            )
        }
    }
}
