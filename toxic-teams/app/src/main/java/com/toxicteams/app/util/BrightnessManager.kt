package com.toxicteams.app.util

import android.view.Window
import android.view.WindowManager
import com.toxicteams.app.data.JigglerPhase

object BrightnessManager {

    const val BRIGHTNESS_ACTIVE = 1.0f
    const val BRIGHTNESS_SLEEP = 0.01f
    const val BRIGHTNESS_DEFAULT = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE

    /**
     * Applies window brightness and screen-on flags based on current session state.
     */
    fun applyState(window: Window, isRunning: Boolean, phase: JigglerPhase) {
        val layoutParams = window.attributes
        if (isRunning) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            layoutParams.screenBrightness = when (phase) {
                JigglerPhase.ACTIVE -> BRIGHTNESS_ACTIVE
                JigglerPhase.SLEEP -> BRIGHTNESS_SLEEP
                JigglerPhase.IDLE -> BRIGHTNESS_DEFAULT
            }
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            layoutParams.screenBrightness = BRIGHTNESS_DEFAULT
        }
        window.attributes = layoutParams
    }

    /**
     * Resets brightness to system default and clears keep-screen-on flag.
     */
    fun reset(window: Window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val layoutParams = window.attributes
        layoutParams.screenBrightness = BRIGHTNESS_DEFAULT
        window.attributes = layoutParams
    }
}

