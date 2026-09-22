package com.toxicteams.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.toxicteams.app.data.JigglerPhase
import com.toxicteams.app.data.JigglerUiState
import com.toxicteams.app.data.MotionPattern
import com.toxicteams.app.data.VibrationIntensity
import com.toxicteams.app.haptics.HapticController
import com.toxicteams.app.haptics.NoOpHapticController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ViewModel governing the jiggler engine state machine, timing loops, and haptic triggers.
 */
class JigglerViewModel(
    private val hapticController: HapticController = NoOpHapticController()
) : ViewModel() {

    private val _uiState = MutableStateFlow(JigglerUiState())
    val uiState: StateFlow<JigglerUiState> = _uiState.asStateFlow()

    private var engineJob: Job? = null

    /**
     * Start the jiggler engine cycle loop.
     */
    fun startJiggler() {
        if (_uiState.value.isRunning) return

        _uiState.update {
            it.copy(
                isRunning = true,
                currentCycleCount = 0,
                totalActiveSeconds = 0L
            )
        }

        engineJob = viewModelScope.launch {
            while (isActive) {
                // ==========================================
                // 1. ACTIVE PHASE
                // ==========================================
                val activeDuration = _uiState.value.activeDurationSeconds
                _uiState.update {
                    it.copy(
                        phase = JigglerPhase.ACTIVE,
                        secondsRemainingInPhase = activeDuration
                    )
                }

                for (remaining in activeDuration downTo 1) {
                    _uiState.update {
                        it.copy(secondsRemainingInPhase = remaining)
                    }

                    // Trigger micro-vibrations periodically during active phase (every 2 seconds or on start)
                    val elapsed = activeDuration - remaining
                    if (elapsed % 2 == 0) {
                        hapticController.triggerPulse(_uiState.value.vibrationIntensity)
                    }

                    delay(1000L)

                    _uiState.update {
                        it.copy(totalActiveSeconds = it.totalActiveSeconds + 1)
                    }
                }

                // Stop haptics at the end of active phase
                hapticController.stop()

                // Increment cycle counter
                _uiState.update {
                    it.copy(
                        currentCycleCount = it.currentCycleCount + 1,
                        secondsRemainingInPhase = 0
                    )
                }

                // ==========================================
                // 2. SLEEP / BATTERY SAVER PHASE
                // ==========================================
                val idleDuration = _uiState.value.idleDurationSeconds
                _uiState.update {
                    it.copy(
                        phase = JigglerPhase.SLEEP,
                        secondsRemainingInPhase = idleDuration
                    )
                }

                for (remaining in idleDuration downTo 1) {
                    _uiState.update {
                        it.copy(secondsRemainingInPhase = remaining)
                    }
                    delay(1000L)
                }

                _uiState.update {
                    it.copy(secondsRemainingInPhase = 0)
                }
            }
        }
    }

    /**
     * Stop the jiggler engine and restore idle state.
     */
    fun stopJiggler() {
        engineJob?.cancel()
        engineJob = null
        hapticController.stop()

        _uiState.update {
            it.copy(
                isRunning = false,
                phase = JigglerPhase.IDLE,
                secondsRemainingInPhase = 0
            )
        }
    }

    fun setActiveDuration(seconds: Int) {
        val clamped = seconds.coerceIn(5, 20)
        _uiState.update { it.copy(activeDurationSeconds = clamped) }
    }

    fun setIdleDuration(seconds: Int) {
        val clamped = seconds.coerceIn(20, 120)
        _uiState.update { it.copy(idleDurationSeconds = clamped) }
    }

    fun setVibrationIntensity(intensity: VibrationIntensity) {
        _uiState.update { it.copy(vibrationIntensity = intensity) }
    }

    fun testVibration(intensity: VibrationIntensity) {
        hapticController.triggerPulse(intensity)
    }

    fun setMotionPattern(pattern: MotionPattern) {
        _uiState.update { it.copy(motionPattern = pattern) }
    }

    override fun onCleared() {
        super.onCleared()
        engineJob?.cancel()
        hapticController.stop()
    }

    companion object {
        fun provideFactory(hapticController: HapticController): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return JigglerViewModel(hapticController) as T
                }
            }
    }
}

