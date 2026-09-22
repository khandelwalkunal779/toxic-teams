package com.toxicteams.app.data

/**
 * Represents the current operational phase of the jiggler engine.
 */
enum class JigglerPhase {
    IDLE,
    ACTIVE,
    SLEEP
}

/**
 * Vibration intensity levels for micro-vibrations during active jiggle cycles.
 */
enum class VibrationIntensity {
    OFF,
    GENTLE,
    STANDARD
}

/**
 * Visual motion patterns engineered to trigger optical/laser mouse sensors.
 */
enum class MotionPattern {
    MOIRE_RINGS,
    NOISE_GRID,
    RADAR_PULSE
}

/**
 * UI State for the Toxic Teams jiggler dashboard.
 */
data class JigglerUiState(
    val isRunning: Boolean = false,
    val phase: JigglerPhase = JigglerPhase.IDLE,
    val secondsRemainingInPhase: Int = 0,
    val activeDurationSeconds: Int = 8,
    val idleDurationSeconds: Int = 45,
    val vibrationIntensity: VibrationIntensity = VibrationIntensity.STANDARD,
    val motionPattern: MotionPattern = MotionPattern.MOIRE_RINGS,
    val currentCycleCount: Int = 0,
    val totalActiveSeconds: Long = 0L
)

