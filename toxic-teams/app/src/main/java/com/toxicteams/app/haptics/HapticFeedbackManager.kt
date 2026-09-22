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
 * Controller interface for managing haptic vibrations.
 */
interface HapticController {
    /**
     * Start continuous vibration pattern for the active jiggle phase.
     * Vibrates continuously until [stop] is invoked.
     */
    fun startContinuous(intensity: VibrationIntensity)

    /**
     * Stop all vibrations immediately (for sleep phase or when jiggler stops).
     */
    fun stop()

    /**
     * Trigger a single test pulse for previewing intensity in settings.
     */
    fun triggerTestPulse(intensity: VibrationIntensity)
}

/**
 * Android implementation supporting continuous vibrations on Google Pixel and other Android devices.
 *
 * Key design considerations for Google Pixel:
 * 1. Uses USAGE_ALARM so that vibrations are not filtered out by Pixel's "Touch feedback" disable setting.
 * 2. Uses repeating waveform effects (repeatIndex = 0) so the vibration is continuous throughout
 *    the active phase, and instantly stops via cancel() when transitioning to sleep or stopping.
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

    override fun startContinuous(intensity: VibrationIntensity) {
        if (intensity == VibrationIntensity.OFF) {
            stop()
            return
        }
        val currentVibrator = vibrator ?: return

        try {
            if (!currentVibrator.hasVibrator()) {
                Log.d("HapticFeedback", "No vibrator hardware present")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val hasAmplitude = currentVibrator.hasAmplitudeControl()
                val effect = createContinuousEffect(intensity, hasAmplitude) ?: return

                // 1. API 33+ (Android 13, 14, 15, 16, Google Pixel)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    try {
                        val vibrationAttributes = VibrationAttributes.Builder()
                            .setUsage(VibrationAttributes.USAGE_ALARM)
                            .build()
                        currentVibrator.vibrate(effect, vibrationAttributes)
                        return
                    } catch (e: Exception) {
                        Log.w("HapticFeedback", "Vibrate with VibrationAttributes failed, trying AudioAttributes", e)
                    }
                }

                // 2. Android 8.0 - 12 fallback with AudioAttributes USAGE_ALARM
                try {
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    currentVibrator.vibrate(effect, audioAttributes)
                    return
                } catch (e: Exception) {
                    Log.w("HapticFeedback", "Vibrate with AudioAttributes failed, falling back", e)
                }

                // 3. Fallback without attributes
                currentVibrator.vibrate(effect)
            } else {
                // Legacy Android (< API 26) continuous repeating pattern
                @Suppress("DEPRECATION")
                val timings = when (intensity) {
                    VibrationIntensity.OFF -> longArrayOf(0)
                    VibrationIntensity.GENTLE -> longArrayOf(0, 150, 40)
                    VibrationIntensity.STANDARD -> longArrayOf(0, 250, 40)
                }
                if (intensity != VibrationIntensity.OFF) {
                    @Suppress("DEPRECATION")
                    currentVibrator.vibrate(timings, 0)
                }
            }
        } catch (e: Exception) {
            Log.e("HapticFeedback", "Failed to start continuous vibration", e)
        }
    }

    override fun stop() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.w("HapticFeedback", "Failed to cancel vibration", e)
        }
    }

    override fun triggerTestPulse(intensity: VibrationIntensity) {
        if (intensity == VibrationIntensity.OFF) return
        val currentVibrator = vibrator ?: return

        try {
            if (!currentVibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val hasAmplitude = currentVibrator.hasAmplitudeControl()
                val (durationMs, amplitude) = when (intensity) {
                    VibrationIntensity.OFF -> Pair(0L, 0)
                    VibrationIntensity.GENTLE -> Pair(250L, if (hasAmplitude) 150 else VibrationEffect.DEFAULT_AMPLITUDE)
                    VibrationIntensity.STANDARD -> Pair(350L, if (hasAmplitude) 255 else VibrationEffect.DEFAULT_AMPLITUDE)
                }
                if (durationMs > 0) {
                    val effect = VibrationEffect.createOneShot(durationMs, amplitude)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val attrs = VibrationAttributes.Builder()
                            .setUsage(VibrationAttributes.USAGE_ALARM)
                            .build()
                        currentVibrator.vibrate(effect, attrs)
                    } else {
                        val audioAttrs = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                        currentVibrator.vibrate(effect, audioAttrs)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                currentVibrator.vibrate(300L)
            }
        } catch (e: Exception) {
            Log.e("HapticFeedback", "Failed to trigger test pulse", e)
        }
    }

    private fun createContinuousEffect(
        intensity: VibrationIntensity,
        hasAmplitude: Boolean
    ): VibrationEffect? {
        if (intensity == VibrationIntensity.OFF) return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (intensity) {
                VibrationIntensity.OFF -> null
                VibrationIntensity.GENTLE -> {
                    // Steady gentle repeating vibration (160ms on, 40ms off, repeating at index 0)
                    if (hasAmplitude) {
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 160, 40),
                            intArrayOf(0, 150, 0),
                            0 // Repeat indefinitely
                        )
                    } else {
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 160, 40),
                            0 // Repeat indefinitely
                        )
                    }
                }
                VibrationIntensity.STANDARD -> {
                    // Robust continuous vibration (260ms on, 40ms off, repeating at index 0)
                    if (hasAmplitude) {
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 260, 40),
                            intArrayOf(0, 255, 0),
                            0 // Repeat indefinitely
                        )
                    } else {
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 260, 40),
                            0 // Repeat indefinitely
                        )
                    }
                }
            }
        } else {
            null
        }
    }
}

/**
 * Mock controller for unit tests and Compose previews.
 */
class NoOpHapticController : HapticController {
    var isContinuousRunning = false
    var lastActiveIntensity: VibrationIntensity? = null
    var testPulseCount = 0

    override fun startContinuous(intensity: VibrationIntensity) {
        if (intensity == VibrationIntensity.OFF) {
            isContinuousRunning = false
            lastActiveIntensity = null
        } else {
            isContinuousRunning = true
            lastActiveIntensity = intensity
        }
    }

    override fun stop() {
        isContinuousRunning = false
    }

    override fun triggerTestPulse(intensity: VibrationIntensity) {
        if (intensity != VibrationIntensity.OFF) {
            testPulseCount++
        }
    }
}
