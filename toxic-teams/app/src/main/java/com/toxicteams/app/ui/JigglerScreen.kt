package com.toxicteams.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
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
import com.toxicteams.app.ui.theme.CorporateGreenDark
import com.toxicteams.app.ui.theme.CorporateGreenGlow
import com.toxicteams.app.ui.theme.ElectricLavender
import com.toxicteams.app.ui.theme.ErrorRed
import com.toxicteams.app.ui.theme.SlateBorder
import com.toxicteams.app.ui.theme.SlateSurfaceVariant
import com.toxicteams.app.ui.theme.TextHighEmphasis
import com.toxicteams.app.ui.theme.TextLowEmphasis
import com.toxicteams.app.ui.theme.TextMediumEmphasis
import com.toxicteams.app.viewmodel.JigglerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JigglerScreen(
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
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CorporateGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = CorporateGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
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
            // Optical Motion Active Target Canvas
            OpticalMotionCanvas(
                phase = uiState.phase,
                pattern = uiState.motionPattern,
                secondsRemaining = uiState.secondsRemainingInPhase,
                totalIdleDuration = uiState.idleDurationSeconds
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle status prompt
            Text(
                text = when (uiState.phase) {
                    JigglerPhase.ACTIVE -> stringResource(R.string.target_zone_active_sub)
                    JigglerPhase.SLEEP -> stringResource(
                        R.string.target_zone_sleep_sub,
                        uiState.secondsRemainingInPhase
                    )
                    JigglerPhase.IDLE -> stringResource(R.string.target_zone_idle_sub)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when (uiState.phase) {
                    JigglerPhase.ACTIVE -> CorporateGreenGlow
                    JigglerPhase.SLEEP -> ElectricLavender
                    JigglerPhase.IDLE -> TextMediumEmphasis
                },
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Hero Action: Large Expressive Start/Stop Button
            HeroActionButton(
                isRunning = uiState.isRunning,
                onToggle = {
                    if (uiState.isRunning) {
                        viewModel.stopJiggler()
                    } else {
                        viewModel.startJiggler()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Session Stats Strip (Cycles & Active Runtime)
            if (uiState.isRunning || uiState.currentCycleCount > 0) {
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
                onMotionPatternChange = viewModel::setMotionPattern
            )

            Spacer(modifier = Modifier.height(16.dp))

            // How To Place Visual Guide Card
            PlacementGuideCard()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Large expressive Start/Stop Hero Button.
 */
@Composable
private fun HeroActionButton(
    isRunning: Boolean,
    onToggle: () -> Unit
) {
    val buttonBgColor by animateColorAsState(
        targetValue = if (isRunning) ErrorRed else CorporateGreen,
        label = "HeroButtonBgColor"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (isRunning) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "HeroButtonScale"
    )

    Button(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .scale(buttonScale)
            .shadow(
                elevation = if (isRunning) 12.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isRunning) ErrorRed else CorporateGreenGlow
            ),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonBgColor,
            contentColor = if (isRunning) Color.White else Color.Black
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isRunning) {
                    stringResource(R.string.action_stop).uppercase()
                } else {
                    stringResource(R.string.action_start).uppercase()
                },
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

