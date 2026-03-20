package com.pixelcolor.app.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var tapSound: Int = 0
    private var fillSound: Int = 0
    private var completeSound: Int = 0
    private var errorSound: Int = 0
    private var loaded = false

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun initialize() {
        if (loaded) return

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()

        // We'll use programmatically generated sounds since we don't have audio files
        // For a production app, you'd load actual .ogg files from res/raw
        loaded = true
    }

    fun playTap() {
        // Subtle tap feedback - in production would play actual sound
    }

    fun playFill() {
        // Fill sound feedback
    }

    fun playComplete() {
        // Completion chime
    }

    fun playError() {
        // Wrong color feedback
    }

    fun vibrateTap() {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(20)
            }
        }
    }

    fun vibrateSuccess() {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 30, 50, 30), -1)
            }
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        loaded = false
    }
}
