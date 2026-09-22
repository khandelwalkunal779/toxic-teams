package com.toxicteams.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toxicteams.app.R
import com.toxicteams.app.data.JigglerPhase
import com.toxicteams.app.data.JigglerUiState
import com.toxicteams.app.ui.components.JigglerControls
import com.toxicteams.app.ui.components.OpticalMotionCanvas
import com.toxicteams.app.ui.components.PlacementGuideCard
import com.toxicteams.app.ui.components.StatusBadge
import com.toxicteams.app.ui.theme.CharcoalBackground
import com.toxicteams.app.ui.theme.CharcoalSurface
import com.toxicteams.app.ui.theme.CorporateGreen
import com.toxicteams.app.ui.theme.CorporateGreenGlow
import com.toxicteams.app.ui.theme.ElectricLavender
import com.toxicteams.app.ui.theme.ErrorRed
import com.toxicteams.app.ui.theme.SlateBorder
import com.toxicteams.app.ui.theme.SlateSurfaceVariant
import com.toxicteams.app.ui.theme.TextHighEmphasis
import com.toxicteams.app.ui.theme.TextLowEmphasis
import com.toxicteams.app.ui.theme.TextMediumEmphasis
import com.toxicteams.app.viewmodel.JigglerViewModel

@Composable
fun JigglerScreen(
    viewModel: JigglerViewModel,
    uiState: JigglerUiState
) {
    AnimatedContent(
        targetState = uiState.isRunning,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "JigglerScreenTransition"
    ) { isRunning ->
        if (isRunning) {
            // ========================================================
            // FULL-SCREEN RUNNING MODE:
            // High-contrast patterns cover the entire display!
            // Mouse can be rested anywhere on the glass surface.
            // ========================================================
            FullScreenJigglerMode(
                viewModel = viewModel,
                uiState = uiState
            )
        } else {
            // ========================================================
            // DASHBOARD CONFIGURATION MODE:
            // Preview target, settings, guide, and Start Hero button.
            // ========================================================
            DashboardMode(
                viewModel = viewModel,
                uiState = uiState
            )
        }
    }
}

/**
 * Fullscreen execution mode: The chosen optical motion pattern fills the entire screen.
 */
@Composable
private fun FullScreenJigglerMode(
    viewModel: JigglerViewModel,
    uiState: JigglerUiState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Full screen optical canvas
        OpticalMotionCanvas(
            modifier = Modifier.fillMaxSize(),
            phase = uiState.phase,
            pattern = uiState.motionPattern,
            secondsRemaining = uiState.secondsRemainingInPhase,
            totalIdleDuration = uiState.idleDurationSeconds,
            isFullScreen = true
        )

        // Top Status HUD Overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .border(
                    width = 1.5.dp,
                    color = if (uiState.phase == JigglerPhase.ACTIVE) CorporateGreen else ElectricLavender,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_toxic_teams_logo),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = if (uiState.phase == JigglerPhase.ACTIVE) {
                        "ACTIVE • REST MOUSE ANYWHERE"
                    } else {
                        "STEALTH BATTERY SAVER"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                    color = if (uiState.phase == JigglerPhase.ACTIVE) CorporateGreenGlow else ElectricLavender
                )
                Text(
                    text = if (uiState.phase == JigglerPhase.ACTIVE) {
                        "Cycle #${uiState.currentCycleCount + 1} • ${uiState.secondsRemainingInPhase}s remaining (100% Brightness)"
                    } else {
                        "Next active cycle in ${uiState.secondsRemainingInPhase}s (1% Brightness)"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextHighEmphasis
                )
            }
        }

        // Bottom Prominent Stop Button
        Button(
            onClick = { viewModel.stopJiggler() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = ErrorRed
                ),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ErrorRed,
                contentColor = Color.White
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.action_stop).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.2.sp
                    )
                )
            }
        }
    }
}

/**
 * Standard Dashboard Configuration Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardMode(
    viewModel: JigglerViewModel,
    uiState: JigglerUiState
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = CharcoalBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_toxic_teams_logo),
                            contentDescription = "Toxic Teams Logo",
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = TextHighEmphasis
                        )
                    }
                },
                actions = {
                    StatusBadge(
                        phase = uiState.phase,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CharcoalSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Optical Motion Target Preview Canvas
            OpticalMotionCanvas(
                phase = uiState.phase,
                pattern = uiState.motionPattern,
                secondsRemaining = uiState.secondsRemainingInPhase,
                totalIdleDuration = uiState.idleDurationSeconds,
                isFullScreen = false
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle prompt
            Text(
                text = stringResource(R.string.target_zone_idle_sub),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMediumEmphasis,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Hero Start Action Button
            HeroStartButton(
                onStart = { viewModel.startJiggler() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Session Stats Strip
            if (uiState.currentCycleCount > 0 || uiState.totalActiveSeconds > 0) {
                SessionStatsStrip(
                    cycles = uiState.currentCycleCount,
                    totalActiveSeconds = uiState.totalActiveSeconds
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Customization Controls
            JigglerControls(
                activeDurationSeconds = uiState.activeDurationSeconds,
                idleDurationSeconds = uiState.idleDurationSeconds,
                vibrationIntensity = uiState.vibrationIntensity,
                motionPattern = uiState.motionPattern,
                onActiveDurationChange = viewModel::setActiveDuration,
                onIdleDurationChange = viewModel::setIdleDuration,
                onVibrationIntensityChange = viewModel::setVibrationIntensity,
                onTestVibration = { viewModel.testVibration(it) },
                onMotionPatternChange = viewModel::setMotionPattern
            )

            Spacer(modifier = Modifier.height(16.dp))

            // How To Place Visual Guide Card
            PlacementGuideCard()

            Spacer(modifier = Modifier.height(28.dp))

            // Author Credits
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.credits_designed_by),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    ),
                    color = TextLowEmphasis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.credits_author),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = CorporateGreen
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Large expressive Start Hero Button.
 */
@Composable
private fun HeroStartButton(
    onStart: () -> Unit
) {
    Button(
        onClick = onStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = CorporateGreenGlow
            ),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CorporateGreen,
            contentColor = Color.Black
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.action_start).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

/**
 * Quick stats strip showing cycles and active seconds.
 */
@Composable
private fun SessionStatsStrip(
    cycles: Int,
    totalActiveSeconds: Long
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SlateSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cycles Stat
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = CorporateGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "$cycles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis
                    )
                    Text(
                        text = stringResource(R.string.stat_cycles),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextLowEmphasis
                    )
                }
            }

            // Divider dot
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(SlateBorder)
            )

            // Active Time Stat
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = ElectricLavender,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    val minutes = totalActiveSeconds / 60
                    val seconds = totalActiveSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis
                    )
                    Text(
                        text = stringResource(R.string.stat_active_time),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextLowEmphasis
                    )
                }
            }
        }
    }
}
