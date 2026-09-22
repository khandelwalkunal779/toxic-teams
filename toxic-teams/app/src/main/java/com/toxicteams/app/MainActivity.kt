package com.toxicteams.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toxicteams.app.haptics.AndroidHapticFeedbackManager
import com.toxicteams.app.ui.JigglerScreen
import com.toxicteams.app.ui.theme.CharcoalBackground
import com.toxicteams.app.ui.theme.ToxicTeamsTheme
import com.toxicteams.app.util.BrightnessManager
import com.toxicteams.app.viewmodel.JigglerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: JigglerViewModel by viewModels {
        JigglerViewModel.provideFactory(AndroidHapticFeedbackManager(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ToxicTeamsTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val lifecycleOwner = LocalLifecycleOwner.current

                // 1. Manage dynamic screen brightness and keep-awake flag based on engine phase
                LaunchedEffect(uiState.isRunning, uiState.phase) {
                    BrightnessManager.applyState(window, uiState.isRunning, uiState.phase)
                }

                // 2. Lifecycle observer: restore system brightness immediately when backgrounded
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_PAUSE,
                            Lifecycle.Event.ON_STOP -> {
                                BrightnessManager.reset(window)
                            }
                            Lifecycle.Event.ON_RESUME -> {
                                BrightnessManager.applyState(window, uiState.isRunning, uiState.phase)
                            }
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        BrightnessManager.reset(window)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CharcoalBackground
                ) {
                    JigglerScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Ensure default brightness is restored if OS kills or stops window
        BrightnessManager.reset(window)
    }

    override fun onDestroy() {
        super.onDestroy()
        BrightnessManager.reset(window)
    }
}

