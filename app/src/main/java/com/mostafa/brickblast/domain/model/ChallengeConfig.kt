package com.mostafa.brickblast.domain.model

/** Challenge mode level layout and pagination. */
object ChallengeConfig {
    const val TOTAL_LEVELS = 125
    const val LEVELS_PER_PAGE = 25

    val totalPages: Int get() = TOTAL_LEVELS / LEVELS_PER_PAGE

    fun pageForLevel(level: Int): Int =
        ((level - 1) / LEVELS_PER_PAGE).coerceIn(0, totalPages - 1)

    fun levelsOnPage(pageIndex: Int): IntRange {
        val start = pageIndex * LEVELS_PER_PAGE + 1
        val end = minOf(start + LEVELS_PER_PAGE - 1, TOTAL_LEVELS)
        return start..end
    }
}

data class ChallengeProgress(
    val highestUnlocked: Int = 1,
    val completedLevels: Set<Int> = emptySet()
) {
    fun isUnlocked(level: Int): Boolean = level in 1..highestUnlocked
    fun isCompleted(level: Int): Boolean = level in completedLevels
}
