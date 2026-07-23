package com.mostafa.brickblast.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

enum class SoundEffect {
    SHOOT,
    BALL_LAUNCH,
    BOUNCE,
    DESTROY,
    COLLECT,
    GAME_OVER,
    POWER_UP,
    ACHIEVEMENT
}

/**
 * Game audio: synthesized one-shots via SoundPool (overlap-friendly) with ToneGenerator fallback.
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

    private var shootId = 0
    private var launchId = 0
    private var bounceId = 0
    private var destroyCrackId = 0
    private var destroyBlamId = 0
    private var destroyPopId = 0
    private var collectId = 0
    private var gameOverId = 0
    private var powerUpId = 0
    private var poolReady = false

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(16)
            .setAudioAttributes(attrs)
            .build()
        toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 70)
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) poolReady = true
        }
        loadSynthesizedSounds()
    }

    private fun loadSynthesizedSounds() {
        val dir = File(context.cacheDir, "sfx").also { it.mkdirs() }
        shootId = soundPool.load(writeWav(dir, "shoot.wav", 55, 520f, noise = 0.15f, volume = 0.85f), 1)
        launchId = soundPool.load(writeWav(dir, "launch.wav", 18, 1400f, noise = 0.35f, volume = 0.55f), 1)
        bounceId = soundPool.load(writeWav(dir, "bounce.wav", 22, 900f, noise = 0.1f, volume = 0.4f), 1)
        destroyCrackId = soundPool.load(writeWav(dir, "crack.wav", 48, 380f, noise = 0.7f, volume = 0.9f), 1)
        destroyBlamId = soundPool.load(writeWav(dir, "blam.wav", 70, 160f, noise = 0.45f, volume = 0.95f), 1)
        destroyPopId = soundPool.load(writeWav(dir, "pop.wav", 36, 720f, noise = 0.4f, volume = 0.8f), 1)
        collectId = soundPool.load(writeWav(dir, "collect.wav", 40, 1100f, noise = 0.05f, volume = 0.65f), 1)
        gameOverId = soundPool.load(writeWav(dir, "gameover.wav", 220, 220f, noise = 0.2f, volume = 0.85f), 1)
        powerUpId = soundPool.load(writeWav(dir, "powerup.wav", 90, 660f, noise = 0.1f, volume = 0.75f), 1)
    }

    fun setSoundEnabled(enabled: Boolean) { soundEnabled = enabled }
    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (!enabled) stopMusic()
        else startMusic()
    }

    fun play(effect: SoundEffect) {
        if (!soundEnabled) return
        if (effect == SoundEffect.ACHIEVEMENT) {
            playPool(collectId, rate = 1.15f)
            handler.postDelayed({
                if (soundEnabled) playPool(powerUpId, rate = 1.25f)
            }, 110L)
            return
        }
        if (poolReady) {
            when (effect) {
                SoundEffect.SHOOT -> playPool(shootId, rate = 0.92f + Random.nextFloat() * 0.1f)
                SoundEffect.BALL_LAUNCH -> playPool(launchId, rate = 0.85f + Random.nextFloat() * 0.35f)
                SoundEffect.BOUNCE -> playPool(bounceId, rate = 0.9f + Random.nextFloat() * 0.2f)
                SoundEffect.DESTROY -> playDestroyVariant()
                SoundEffect.COLLECT -> playPool(collectId, rate = 0.95f + Random.nextFloat() * 0.15f)
                SoundEffect.GAME_OVER -> playPool(gameOverId)
                SoundEffect.POWER_UP -> playPool(powerUpId, rate = 1f + Random.nextFloat() * 0.1f)
                SoundEffect.ACHIEVEMENT -> Unit
            }
            return
        }
        playToneFallback(effect)
    }

    private fun playDestroyVariant() {
        when (Random.nextInt(3)) {
            0 -> playPool(destroyCrackId, rate = 0.9f + Random.nextFloat() * 0.25f)
            1 -> playPool(destroyBlamId, rate = 0.85f + Random.nextFloat() * 0.2f)
            else -> playPool(destroyPopId, rate = 0.95f + Random.nextFloat() * 0.2f)
        }
    }

    private fun playPool(soundId: Int, rate: Float = 1f) {
        if (soundId == 0) return
        soundPool.play(soundId, 1f, 1f, 1, 0, rate.coerceIn(0.5f, 2f))
    }

    private fun playToneFallback(effect: SoundEffect) {
        val tone = when (effect) {
            SoundEffect.SHOOT -> ToneGenerator.TONE_PROP_BEEP
            SoundEffect.BALL_LAUNCH -> ToneGenerator.TONE_PROP_BEEP2
            SoundEffect.BOUNCE -> ToneGenerator.TONE_PROP_BEEP2
            SoundEffect.DESTROY -> when (Random.nextInt(3)) {
                0 -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
                1 -> ToneGenerator.TONE_CDMA_ABBR_ALERT
                else -> ToneGenerator.TONE_CDMA_CONFIRM
            }
            SoundEffect.COLLECT -> ToneGenerator.TONE_CDMA_CONFIRM
            SoundEffect.GAME_OVER -> ToneGenerator.TONE_CDMA_ABBR_ALERT
            SoundEffect.POWER_UP -> ToneGenerator.TONE_CDMA_ONE_MIN_BEEP
            SoundEffect.ACHIEVEMENT -> ToneGenerator.TONE_CDMA_ANSWER
        }
        val duration = when (effect) {
            SoundEffect.BALL_LAUNCH -> 25
            SoundEffect.DESTROY -> 90
            SoundEffect.GAME_OVER -> 200
            SoundEffect.SHOOT -> 60
            else -> 80
        }
        toneGenerator.startTone(tone, duration)
    }

    fun startMusic() {
        // No background-music asset is bundled yet.
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

    companion object {
        private fun writeWav(
            dir: File,
            name: String,
            durationMs: Int,
            frequencyHz: Float,
            noise: Float,
            volume: Float
        ): String {
            val file = File(dir, name)
            val sampleRate = 22050
            val numSamples = (sampleRate * durationMs / 1000).coerceAtLeast(1)
            val dataSize = numSamples * 2
            RandomAccessFile(file, "rw").use { raf ->
                raf.setLength(0)
                val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
                header.put("RIFF".toByteArray())
                header.putInt(36 + dataSize)
                header.put("WAVE".toByteArray())
                header.put("fmt ".toByteArray())
                header.putInt(16)
                header.putShort(1) // PCM
                header.putShort(1) // mono
                header.putInt(sampleRate)
                header.putInt(sampleRate * 2)
                header.putShort(2)
                header.putShort(16)
                header.put("data".toByteArray())
                header.putInt(dataSize)
                raf.write(header.array())

                val pcm = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = (1.0 - i.toDouble() / numSamples).coerceIn(0.0, 1.0)
                    // Fast attack then decay feels more "hit"-like.
                    val attack = (i / (sampleRate * 0.004)).coerceIn(0.0, 1.0)
                    val sine = sin(2.0 * PI * frequencyHz * t)
                    val n = (Random.nextDouble() * 2.0 - 1.0) * noise
                    val sample = ((sine * (1.0 - noise) + n) * attack * env * volume * 32767.0)
                        .toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    pcm.putShort(sample.toShort())
                }
                raf.write(pcm.array())
            }
            return file.absolutePath
        }
    }
}
