package com.toxicteams.app.viewmodel

import com.toxicteams.app.data.JigglerPhase
import com.toxicteams.app.data.MotionPattern
import com.toxicteams.app.data.VibrationIntensity
import com.toxicteams.app.haptics.NoOpHapticController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JigglerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var hapticController: NoOpHapticController
    private lateinit var viewModel: JigglerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        hapticController = NoOpHapticController()
        viewModel = JigglerViewModel(hapticController)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        val state = viewModel.uiState.value
        assertFalse(state.isRunning)
        assertEquals(JigglerPhase.IDLE, state.phase)
        assertEquals(0, state.secondsRemainingInPhase)
        assertEquals(8, state.activeDurationSeconds)
        assertEquals(45, state.idleDurationSeconds)
        assertEquals(VibrationIntensity.STANDARD, state.vibrationIntensity)
        assertEquals(MotionPattern.MOIRE_RINGS, state.motionPattern)
        assertEquals(0, state.currentCycleCount)
        assertEquals(0L, state.totalActiveSeconds)
    }

    @Test
    fun testStartJigglerTransitionsToActivePhase() = runTest(testDispatcher) {
        viewModel.startJiggler()
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isRunning)
        assertEquals(JigglerPhase.ACTIVE, state.phase)
        assertEquals(8, state.secondsRemainingInPhase)
        assertEquals(0, state.currentCycleCount)
    }

    @Test
    fun testActiveCountdownAndHapticPulses() = runTest(testDispatcher) {
        viewModel.setActiveDuration(6)
        viewModel.startJiggler()
        testDispatcher.scheduler.runCurrent()

        assertEquals(6, viewModel.uiState.value.secondsRemainingInPhase)
        assertEquals(VibrationIntensity.STANDARD, hapticController.lastTriggeredIntensity)
        assertTrue(hapticController.pulseCount >= 1)

        // Advance 1 second
        advanceTimeBy(1000L)
        testDispatcher.scheduler.runCurrent()
        assertEquals(5, viewModel.uiState.value.secondsRemainingInPhase)
        assertEquals(1L, viewModel.uiState.value.totalActiveSeconds)

        // Advance 1 more second (elapsed = 2s, should trigger another pulse)
        val countBefore = hapticController.pulseCount
        advanceTimeBy(1000L)
        testDispatcher.scheduler.runCurrent()
        assertEquals(4, viewModel.uiState.value.secondsRemainingInPhase)
        assertEquals(2L, viewModel.uiState.value.totalActiveSeconds)
        assertTrue(hapticController.pulseCount > countBefore)
    }

    @Test
    fun testTransitionFromActiveToSleepPhase() = runTest(testDispatcher) {
        viewModel.setActiveDuration(5)
        viewModel.setIdleDuration(30)
        viewModel.startJiggler()
        testDispatcher.scheduler.runCurrent()

        assertEquals(JigglerPhase.ACTIVE, viewModel.uiState.value.phase)

        // Advance past the 5s active duration
        advanceTimeBy(5000L)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals(JigglerPhase.SLEEP, state.phase)
        assertEquals(30, state.secondsRemainingInPhase)
        assertEquals(1, state.currentCycleCount)
        assertEquals(5L, state.totalActiveSeconds)
    }

    @Test
    fun testTransitionFromSleepBackToActivePhase() = runTest(testDispatcher) {
        viewModel.setActiveDuration(5)
        viewModel.setIdleDuration(20)
        viewModel.startJiggler()
        testDispatcher.scheduler.runCurrent()

        // 5s active + 20s sleep = 25s
        advanceTimeBy(25000L)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals(JigglerPhase.ACTIVE, state.phase)
        assertEquals(5, state.secondsRemainingInPhase)
        assertEquals(1, state.currentCycleCount)
        assertEquals(5L, state.totalActiveSeconds)
    }

    @Test
    fun testStopJigglerRestoresIdleState() = runTest(testDispatcher) {
        viewModel.startJiggler()
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.uiState.value.isRunning)

        advanceTimeBy(2000L)
        testDispatcher.scheduler.runCurrent()

        viewModel.stopJiggler()
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isRunning)
        assertEquals(JigglerPhase.IDLE, state.phase)
        assertEquals(0, state.secondsRemainingInPhase)
    }

    @Test
    fun testConfigurationClamping() {
        // Active duration clamped between 5 and 20
        viewModel.setActiveDuration(3)
        assertEquals(5, viewModel.uiState.value.activeDurationSeconds)

        viewModel.setActiveDuration(25)
        assertEquals(20, viewModel.uiState.value.activeDurationSeconds)

        // Idle duration clamped between 20 and 120
        viewModel.setIdleDuration(10)
        assertEquals(20, viewModel.uiState.value.idleDurationSeconds)

        viewModel.setIdleDuration(150)
        assertEquals(120, viewModel.uiState.value.idleDurationSeconds)

        // Settings updates
        viewModel.setVibrationIntensity(VibrationIntensity.GENTLE)
        assertEquals(VibrationIntensity.GENTLE, viewModel.uiState.value.vibrationIntensity)

        viewModel.setMotionPattern(MotionPattern.RADAR_PULSE)
        assertEquals(MotionPattern.RADAR_PULSE, viewModel.uiState.value.motionPattern)
    }

    @Test
    fun testDirectVibrationTesting() {
        assertEquals(0, hapticController.pulseCount)
        viewModel.testVibration(VibrationIntensity.GENTLE)
        assertEquals(1, hapticController.pulseCount)
        assertEquals(VibrationIntensity.GENTLE, hapticController.lastTriggeredIntensity)

        viewModel.testVibration(VibrationIntensity.STANDARD)
        assertEquals(2, hapticController.pulseCount)
        assertEquals(VibrationIntensity.STANDARD, hapticController.lastTriggeredIntensity)

        viewModel.testVibration(VibrationIntensity.OFF)
        assertEquals(2, hapticController.pulseCount)
    }
}

