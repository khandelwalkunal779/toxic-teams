package com.toxicteams.app.haptics

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
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
 * Android implementation optimized for modern Google Pixel and all Android devices.
 *
 * Notes on Pixel haptics:
 * 1. Using USAGE_ALARM in VibrationAttributes/AudioAttributes ensures vibration signals
 *    are NOT suppressed if the user has disabled "Touch feedback" in Pixel system settings.
 * 2. Predefined effects (like EFFECT_TICK) are often inaudible/unfelt on flat surfaces and
 *    strictly categorized as touch feedback. Direct waveform/oneshot pulses with explicit
 *    amplitudes deliver the physical mechanical impulse needed to perturb optical mouse sensors.
 */
class AndroidHapticFeedbackManager(context: Context) : HapticController {

    private val appContext = context.applicationContext

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator ?: (appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
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
            if (!currentVibrator.hasVibrator()) {
                Log.d("HapticFeedback", "Device reports no vibrator hardware")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = createPhysicalEffect(currentVibrator, intensity) ?: return

                // 1. Modern API 33+ (Tiramisu, UpsideDownCake, VanillaIceCream, Pixel 7/8/9/10)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    try {
                        val vibrationAttributes = VibrationAttributes.Builder()
                            .setUsage(VibrationAttributes.USAGE_ALARM)
                            .build()
                        currentVibrator.vibrate(effect, vibrationAttributes)
                        return
                    } catch (e: Exception) {
                        Log.w("HapticFeedback", "VibrationAttributes execution failed, attempting AudioAttributes", e)
                    }
                }

                // 2. Android 8.0 to Android 12 fallback using AudioAttributes USAGE_ALARM
                try {
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    currentVibrator.vibrate(effect, audioAttributes)
                    return
                } catch (e: Exception) {
                    Log.w("HapticFeedback", "AudioAttributes execution failed, attempting simple vibrate", e)
                }

                // 3. Fallback to direct vibrate without attributes
                currentVibrator.vibrate(effect)
            } else {
                // Legacy Android fallback
                @Suppress("DEPRECATION")
                val durationMs = when (intensity) {
                    VibrationIntensity.OFF -> 0L
                    VibrationIntensity.GENTLE -> 45L
                    VibrationIntensity.STANDARD -> 80L
                }
                if (durationMs > 0) {
                    @Suppress("DEPRECATION")
                    currentVibrator.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            Log.e("HapticFeedback", "Failed to trigger vibration pulse", e)
        }
    }

    private fun createPhysicalEffect(vibrator: Vibrator, intensity: VibrationIntensity): VibrationEffect? {
        if (intensity == VibrationIntensity.OFF) return null

        val hasAmplitude = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.hasAmplitudeControl()
        } else false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (intensity) {
                VibrationIntensity.OFF -> null
                VibrationIntensity.GENTLE -> {
                    // Crisp 45ms physical pulse
                    if (hasAmplitude) {
                        VibrationEffect.createOneShot(45L, 180)
                    } else {
                        VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }
                VibrationIntensity.STANDARD -> {
                    // Energetic double-tap pulse (60ms on, 35ms off, 60ms on) to jolt optical mouse tracking
                    if (hasAmplitude) {
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 60, 35, 60),
                            intArrayOf(0, 255, 0, 255),
                            -1
                        )
                    } else {
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 60, 35, 60),
                            -1
                        )
                    }
                }
            }
        } else {
            null
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
 * Testable mock controller for unit tests and Compose previews.
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
