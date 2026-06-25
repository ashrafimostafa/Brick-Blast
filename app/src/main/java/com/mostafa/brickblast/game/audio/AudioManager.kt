package com.mostafa.brickblast.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class SoundEffect {
    SHOOT,
    BOUNCE,
    DESTROY,
    COLLECT,
    GAME_OVER,
    POWER_UP
}

/**
 * Audio manager abstraction using SoundPool for SFX and ToneGenerator for procedural sounds.
 * Replace tone mappings with raw resource IDs when audio assets are added.
 */
@Singleton
class AudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var soundEnabled = true
    private var musicEnabled = true

    private val soundPool: SoundPool
    private val toneGenerator: ToneGenerator
    private val handler = Handler(Looper.getMainLooper())
    private var musicRunnable: Runnable? = null

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(attrs)
            .build()
        toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 60)
    }

    fun setSoundEnabled(enabled: Boolean) { soundEnabled = enabled }
    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (!enabled) stopMusic()
        else startMusic()
    }

    fun play(effect: SoundEffect) {
        if (!soundEnabled) return
        val tone = when (effect) {
            SoundEffect.SHOOT -> ToneGenerator.TONE_PROP_BEEP
            SoundEffect.BOUNCE -> ToneGenerator.TONE_PROP_BEEP2
            SoundEffect.DESTROY -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
            SoundEffect.COLLECT -> ToneGenerator.TONE_CDMA_CONFIRM
            SoundEffect.GAME_OVER -> ToneGenerator.TONE_CDMA_ABBR_ALERT
            SoundEffect.POWER_UP -> ToneGenerator.TONE_CDMA_ONE_MIN_BEEP
        }
        val duration = when (effect) {
            SoundEffect.DESTROY, SoundEffect.GAME_OVER -> 200
            else -> 80
        }
        toneGenerator.startTone(tone, duration)
    }

    fun startMusic() {
        // No background-music asset is bundled yet. The previous placeholder
        // looped an annoying ringtone tone every 3s even while idle, so it is
        // disabled. Hook a real MediaPlayer/looping track here when available.
    }

    fun stopMusic() {
        musicRunnable?.let { handler.removeCallbacks(it) }
        musicRunnable = null
    }

    fun release() {
        stopMusic()
        soundPool.release()
        toneGenerator.release()
    }
}
