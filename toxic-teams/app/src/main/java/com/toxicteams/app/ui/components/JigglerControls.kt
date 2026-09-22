package com.toxicteams.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toxicteams.app.R
import com.toxicteams.app.data.MotionPattern
import com.toxicteams.app.data.VibrationIntensity
import com.toxicteams.app.ui.theme.CorporateGreen
import com.toxicteams.app.ui.theme.ElectricLavender
import com.toxicteams.app.ui.theme.SlateBorder
import com.toxicteams.app.ui.theme.SlateSurfaceContainerHigh
import com.toxicteams.app.ui.theme.SlateSurfaceVariant
import com.toxicteams.app.ui.theme.TextHighEmphasis
import com.toxicteams.app.ui.theme.TextMediumEmphasis
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JigglerControls(
    modifier: Modifier = Modifier,
    activeDurationSeconds: Int,
    idleDurationSeconds: Int,
    vibrationIntensity: VibrationIntensity,
    motionPattern: MotionPattern,
    onActiveDurationChange: (Int) -> Unit,
    onIdleDurationChange: (Int) -> Unit,
    onVibrationIntensityChange: (VibrationIntensity) -> Unit,
    onMotionPatternChange: (MotionPattern) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(SlateBorder)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = CorporateGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_header),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextHighEmphasis
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Active Duration Slider (5s - 20s)
            ControlSlider(
                icon = Icons.Default.Timer,
                title = stringResource(R.string.active_duration_label),
                value = activeDurationSeconds.toFloat(),
                valueRange = 5f..20f,
                steps = 14, // integer steps 5 to 20
                displayValue = "${activeDurationSeconds}s",
                accentColor = CorporateGreen,
                onValueChange = { onActiveDurationChange(it.roundToInt()) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Idle Interval Slider (20s - 120s)
            ControlSlider(
                icon = Icons.Default.Timer,
                title = stringResource(R.string.idle_interval_label),
                value = idleDurationSeconds.toFloat(),
                valueRange = 20f..120f,
                steps = 19, // steps of 5 seconds
                displayValue = "${idleDurationSeconds}s",
                accentColor = ElectricLavender,
                onValueChange = { onIdleDurationChange(it.roundToInt()) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Vibration Intensity Selector
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = null,
                    tint = CorporateGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.vibration_intensity_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextHighEmphasis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VibrationIntensity.entries.forEach { intensity ->
                    val selected = intensity == vibrationIntensity
                    val label = when (intensity) {
                        VibrationIntensity.OFF -> stringResource(R.string.vibration_off)
                        VibrationIntensity.GENTLE -> stringResource(R.string.vibration_gentle)
                        VibrationIntensity.STANDARD -> stringResource(R.string.vibration_standard)
                    }

                    FilterChip(
                        selected = selected,
                        onClick = { onVibrationIntensityChange(intensity) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SlateSurfaceContainerHigh,
                            labelColor = TextMediumEmphasis,
                            selectedContainerColor = CorporateGreen,
                            selectedLabelColor = Color.Black
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = SlateBorder,
                            selectedBorderColor = CorporateGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Motion Pattern Selector
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Animation,
                    contentDescription = null,
                    tint = ElectricLavender,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.pattern_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextHighEmphasis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MotionPattern.entries.forEach { pattern ->
                    val selected = pattern == motionPattern
                    val label = when (pattern) {
                        MotionPattern.MOIRE_RINGS -> stringResource(R.string.pattern_moire)
                        MotionPattern.NOISE_GRID -> stringResource(R.string.pattern_noise)
                        MotionPattern.RADAR_PULSE -> stringResource(R.string.pattern_radar)
                    }

                    FilterChip(
                        selected = selected,
                        onClick = { onMotionPatternChange(pattern) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SlateSurfaceContainerHigh,
                            labelColor = TextMediumEmphasis,
                            selectedContainerColor = ElectricLavender,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = SlateBorder,
                            selectedBorderColor = ElectricLavender
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlSlider(
    icon: ImageVector,
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    displayValue: String,
    accentColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextHighEmphasis
                )
            }

            // Pill Badge showing current value
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = SlateSurfaceContainerHigh
            )
        )
    }
}

