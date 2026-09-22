package com.toxicteams.app.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.toxicteams.app.data.VibrationIntensity

/**
 * Controller interface for triggering haptic feedback.
 */
interface HapticController {
    fun triggerPulse(intensity: VibrationIntensity)
    fun stop()
}

/**
 * Android implementation supporting VibratorManager (API 31+) and legacy Vibrator.
 */
class AndroidHapticFeedbackManager(context: Context) : HapticController {

    private val appContext = context.applicationContext

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w("HapticFeedback", "Unable to acquire Vibrator service", e)
            null
        }
    }

    override fun triggerPulse(intensity: VibrationIntensity) {
        if (intensity == VibrationIntensity.OFF) return
        val currentVibrator = vibrator ?: return

        try {
            if (!currentVibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = when (intensity) {
                    VibrationIntensity.OFF -> null
                    VibrationIntensity.GENTLE -> {
                        // Subtle tick effect
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    }
                    VibrationIntensity.STANDARD -> {
                        // Sharp click effect to dislodge optical sensor
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    }
                }
                if (effect != null) {
                    currentVibrator.vibrate(effect)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val (durationMs, amplitude) = when (intensity) {
                    VibrationIntensity.OFF -> Pair(0L, 0)
                    VibrationIntensity.GENTLE -> Pair(15L, 60)
                    VibrationIntensity.STANDARD -> Pair(40L, 200)
                }
                if (durationMs > 0) {
                    val effect = VibrationEffect.createOneShot(durationMs, amplitude)
                    currentVibrator.vibrate(effect)
                }
            } else {
                @Suppress("DEPRECATION")
                val durationMs = when (intensity) {
                    VibrationIntensity.OFF -> 0L
                    VibrationIntensity.GENTLE -> 15L
                    VibrationIntensity.STANDARD -> 40L
                }
                if (durationMs > 0) {
                    @Suppress("DEPRECATION")
                    currentVibrator.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            Log.w("HapticFeedback", "Failed to trigger vibration pulse", e)
        }
    }

    override fun stop() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.w("HapticFeedback", "Failed to cancel vibration", e)
        }
    }
}

/**
 * No-op implementation for preview and unit tests.
 */
class NoOpHapticController : HapticController {
    var lastTriggeredIntensity: VibrationIntensity? = null
    var pulseCount = 0

    override fun triggerPulse(intensity: VibrationIntensity) {
        lastTriggeredIntensity = intensity
        if (intensity != VibrationIntensity.OFF) {
            pulseCount++
        }
    }

    override fun stop() {
        // No-op
    }
}

