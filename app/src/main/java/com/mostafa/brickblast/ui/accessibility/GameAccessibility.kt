package com.mostafa.brickblast.ui.accessibility

import com.mostafa.brickblast.domain.model.GamePhase

object GameAccessibility {

    fun statusDescription(
        score: Int,
        bestScore: Int,
        round: Int,
        totalBalls: Int,
        coins: Int,
        phase: GamePhase,
        isAiming: Boolean,
        timeRemaining: Float?
    ): String = buildString {
        append("Score $score.")
        if (score > bestScore) append(" New best score.")
        append(" Best score ${maxOf(score, bestScore)}.")
        append(" Round $round.")
        append(" Balls $totalBalls.")
        append(" Coins $coins.")
        timeRemaining?.let { append(" Time remaining ${it.toInt()} seconds.") }
        append(' ')
        append(phaseDescription(phase, isAiming))
    }

    fun phaseDescription(phase: GamePhase, isAiming: Boolean): String = when (phase) {
        GamePhase.AIMING ->
            if (isAiming) "Aiming. Drag upward to adjust shot direction, then release to fire."
            else "Ready to aim. Drag upward on the game board to shoot."
        GamePhase.LAUNCHING -> "Launching balls."
        GamePhase.SIMULATING -> "Balls in play."
        GamePhase.ROUND_END -> "Round ending."
        GamePhase.PAUSED -> "Game paused."
        GamePhase.GAME_OVER -> "Game over."
        GamePhase.VICTORY -> "Level complete. Victory."
    }

    const val GAME_BOARD_DESCRIPTION =
        "Game board. Drag upward from the launcher to aim, then release to shoot balls at the bricks."
}
