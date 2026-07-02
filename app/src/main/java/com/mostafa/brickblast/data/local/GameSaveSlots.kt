package com.mostafa.brickblast.data.local

import com.mostafa.brickblast.domain.model.GameMode

/** One active save slot per game mode (classic, hardcore, challenge, time attack). */
object GameSaveSlots {
    fun key(mode: GameMode): String = mode.name
}
