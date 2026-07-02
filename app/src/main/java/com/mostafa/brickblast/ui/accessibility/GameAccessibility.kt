package com.mostafa.brickblast.ui.accessibility

import android.content.Context
import com.mostafa.brickblast.R
import com.mostafa.brickblast.domain.model.GamePhase

object GameAccessibility {

    fun statusDescription(
        context: Context,
        score: Int,
        bestScore: Int,
        round: Int,
        totalBalls: Int,
        coins: Int,
        phase: GamePhase,
        isAiming: Boolean,
        timeRemaining: Float?
    ): String = buildString {
        append(context.getString(R.string.a11y_status_score, score))
        if (score > bestScore) append(context.getString(R.string.a11y_status_new_best))
        append(context.getString(R.string.a11y_status_best, maxOf(score, bestScore)))
        append(context.getString(R.string.a11y_status_round, round))
        append(context.getString(R.string.a11y_status_balls, totalBalls))
        append(context.getString(R.string.a11y_status_coins, coins))
        timeRemaining?.let { append(context.getString(R.string.a11y_status_time, it.toInt())) }
        append(' ')
        append(phaseDescription(context, phase, isAiming))
    }

    fun phaseDescription(context: Context, phase: GamePhase, isAiming: Boolean): String = when (phase) {
        GamePhase.AIMING ->
            if (isAiming) context.getString(R.string.a11y_phase_aiming_drag)
            else context.getString(R.string.a11y_phase_aiming_ready)
        GamePhase.LAUNCHING -> context.getString(R.string.a11y_phase_launching)
        GamePhase.SIMULATING -> context.getString(R.string.a11y_phase_simulating)
        GamePhase.ROUND_END -> context.getString(R.string.a11y_phase_round_end)
        GamePhase.PAUSED -> context.getString(R.string.a11y_phase_paused)
        GamePhase.GAME_OVER -> context.getString(R.string.a11y_phase_game_over)
        GamePhase.VICTORY -> context.getString(R.string.a11y_phase_victory)
    }

    fun gameBoardDescription(context: Context): String =
        context.getString(R.string.a11y_game_board)
}
