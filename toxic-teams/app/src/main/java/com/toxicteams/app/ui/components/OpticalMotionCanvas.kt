package com.toxicteams.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toxicteams.app.R
import com.toxicteams.app.data.JigglerPhase
import com.toxicteams.app.data.MotionPattern
import com.toxicteams.app.ui.theme.CorporateGreen
import com.toxicteams.app.ui.theme.CorporateGreenGlow
import com.toxicteams.app.ui.theme.ElectricLavender
import com.toxicteams.app.ui.theme.OpticalPureBlack
import com.toxicteams.app.ui.theme.OpticalPureWhite
import com.toxicteams.app.ui.theme.SlateBorder
import com.toxicteams.app.ui.theme.TextMediumEmphasis
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Optical motion canvas capable of rendering in both target circle mode and full-screen mode.
 *
 * Mouse sensors track micro-features through normalized 2D cross-correlation.
 * High contrast (black/white), high spatial frequency, and continuous 60fps displacement
 * reliably trigger optical and laser tracking sensors across the entire glass surface.
 */
@Composable
fun OpticalMotionCanvas(
    modifier: Modifier = Modifier,
    phase: JigglerPhase,
    pattern: MotionPattern,
    secondsRemaining: Int,
    totalIdleDuration: Int,
    isFullScreen: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OpticalMotion")

    // Rotation angle: continuous 360 loop
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ContinuousRotation"
    )

    // Secondary counter-rotation
    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CounterRotation"
    )

    // Translational oscillation: creates horizontal and vertical micro-displacement
    val oscillationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Oscillation"
    )

    // Pulse scale for idle target
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdlePulse"
    )

    val boxModifier = if (isFullScreen) {
        modifier
            .fillMaxSize()
            .background(Color.Black)
    } else {
        modifier
            .size(260.dp)
            .clip(CircleShape)
            .border(
                width = if (phase == JigglerPhase.ACTIVE) 4.dp else 2.dp,
                color = when (phase) {
                    JigglerPhase.ACTIVE -> CorporateGreen
                    JigglerPhase.SLEEP -> ElectricLavender.copy(alpha = 0.6f)
                    JigglerPhase.IDLE -> SlateBorder
                },
                shape = CircleShape
            )
            .background(Color.Black)
    }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        when (phase) {
            JigglerPhase.ACTIVE -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val radius = if (isFullScreen) {
                        hypot(size.width, size.height) / 2f
                    } else {
                        min(size.width, size.height) / 2f
                    }

                    // Continuous translational micro-jitter
                    val shiftX = sin(oscillationPhase) * 20f
                    val shiftY = cos(oscillationPhase * 1.5f) * 20f

                    withTransform({
                        translate(shiftX, shiftY)
                    }) {
                        when (pattern) {
                            MotionPattern.MOIRE_RINGS -> {
                                drawMoirePattern(
                                    center = centerOffset,
                                    radius = radius,
                                    rotation = rotationAngle,
                                    counterRotation = counterRotationAngle,
                                    oscillation = oscillationPhase,
                                    isFullScreen = isFullScreen
                                )
                            }
                            MotionPattern.NOISE_GRID -> {
                                drawCheckerboardNoise(
                                    center = centerOffset,
                                    radius = radius,
                                    rotation = rotationAngle,
                                    oscillation = oscillationPhase,
                                    canvasSize = size,
                                    isFullScreen = isFullScreen
                                )
                            }
                            MotionPattern.RADAR_PULSE -> {
                                drawRadarPattern(
                                    center = centerOffset,
                                    radius = radius,
                                    rotation = counterRotationAngle,
                                    isFullScreen = isFullScreen
                                )
                            }
                        }
                    }

                    // In both modes, draw optical center crosshairs
                    drawSensorCrosshairs(centerOffset)
                }
            }

            JigglerPhase.SLEEP -> {
                // Battery saver mode: minimalist dim circular countdown progress
                val progress = if (totalIdleDuration > 0) {
                    (totalIdleDuration - secondsRemaining).toFloat() / totalIdleDuration
                } else 0f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val radius = if (isFullScreen) {
                        min(size.width, size.height) * 0.32f
                    } else {
                        (min(size.width, size.height) / 2f) - 16.dp.toPx()
                    }

                    // Background track
                    drawCircle(
                        color = Color(0xFF141924),
                        radius = radius,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Lavender sleep progress arc
                    drawArc(
                        color = ElectricLavender,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "${secondsRemaining}s",
                        style = if (isFullScreen) MaterialTheme.typography.displayLarge else MaterialTheme.typography.displayMedium,
                        color = ElectricLavender,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.status_sleep),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMediumEmphasis,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1% Screen Brightness • Battery Saver",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            JigglerPhase.IDLE -> {
                // Idle state: interactive target zone with alignment crosshairs
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val baseRadius = min(size.width, size.height) / 2f

                    // Concentric guide rings
                    for (i in 1..4) {
                        drawCircle(
                            color = SlateBorder.copy(alpha = 0.35f),
                            radius = (baseRadius / 4f) * i * idlePulse,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // Crosshairs
                    drawLine(
                        color = SlateBorder.copy(alpha = 0.5f),
                        start = Offset(centerOffset.x, 20f),
                        end = Offset(centerOffset.x, size.height - 20f),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = SlateBorder.copy(alpha = 0.5f),
                        start = Offset(20f, centerOffset.y),
                        end = Offset(size.width - 20f, centerOffset.y),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Center target dot
                    drawCircle(
                        color = CorporateGreenGlow.copy(alpha = 0.8f),
                        radius = 8.dp.toPx()
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "FULL-SCREEN MOTION",
                        style = MaterialTheme.typography.labelMedium,
                        color = CorporateGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.target_zone_prompt),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumEmphasis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Concentric moiré rings and rotating radial spokes creating high-contrast interference.
 */
private fun DrawScope.drawMoirePattern(
    center: Offset,
    radius: Float,
    rotation: Float,
    counterRotation: Float,
    oscillation: Float,
    isFullScreen: Boolean
) {
    // 1. Concentric alternating black/white rings
    val ringCount = if (isFullScreen) 28 else 14
    val ringStep = radius / ringCount
    for (i in ringCount downTo 1) {
        val ringRadius = i * ringStep + sin(oscillation + i) * 6f
        val color = if (i % 2 == 0) OpticalPureWhite else OpticalPureBlack
        drawCircle(
            color = color,
            radius = ringRadius.coerceAtLeast(0f),
            center = center
        )
    }

    // 2. Rotating radial grating spokes (creates moiré interference fringes)
    rotate(degrees = rotation, pivot = center) {
        val spokes = if (isFullScreen) 32 else 24
        val sweepAngle = 360f / spokes
        for (i in 0 until spokes step 2) {
            drawArc(
                color = OpticalPureWhite.copy(alpha = 0.9f),
                startAngle = i * sweepAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
        }
    }

    // 3. Counter-rotating high-frequency grating
    rotate(degrees = counterRotation, pivot = center) {
        val innerRadius = radius * (if (isFullScreen) 0.65f else 0.45f)
        val innerSpokes = if (isFullScreen) 40 else 32
        val sweepAngle = 360f / innerSpokes
        for (i in 0 until innerSpokes step 2) {
            drawArc(
                color = OpticalPureBlack,
                startAngle = i * sweepAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                size = Size(innerRadius * 2, innerRadius * 2)
            )
        }
    }
}

/**
 * High-density checkerboard pattern with continuous orbital jitter and rotation.
 */
private fun DrawScope.drawCheckerboardNoise(
    center: Offset,
    radius: Float,
    rotation: Float,
    oscillation: Float,
    canvasSize: Size,
    isFullScreen: Boolean
) {
    rotate(degrees = rotation, pivot = center) {
        val cellSize = if (isFullScreen) 28f else 18f
        val gridExtent = if (isFullScreen) {
            hypot(canvasSize.width, canvasSize.height) * 1.2f
        } else {
            radius * 2.2f
        }

        val cols = (gridExtent / cellSize).toInt()
        val rows = (gridExtent / cellSize).toInt()
        val startX = center.x - (cols * cellSize / 2f)
        val startY = center.y - (rows * cellSize / 2f)

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val isWhite = (row + col) % 2 == 0
                val color = if (isWhite) OpticalPureWhite else OpticalPureBlack
                val cellLeft = startX + col * cellSize
                val cellTop = startY + row * cellSize

                drawRect(
                    color = color,
                    topLeft = Offset(cellLeft, cellTop),
                    size = Size(cellSize, cellSize)
                )
            }
        }
    }

    // Overlaid oscillating circular pulses
    val pulseRadius = (radius * 0.5f) + sin(oscillation * 2f) * 24f
    drawCircle(
        color = OpticalPureWhite,
        radius = pulseRadius.coerceAtLeast(10f),
        center = center,
        style = Stroke(width = 10.dp.toPx())
    )
}

/**
 * Radar pulse beam with sharp contrast edge and high-frequency radial tick marks.
 */
private fun DrawScope.drawRadarPattern(
    center: Offset,
    radius: Float,
    rotation: Float,
    isFullScreen: Boolean
) {
    drawCircle(color = OpticalPureBlack, radius = radius, center = center)

    // Rotating sweeping radar beam with sharp 100% white gradient to pure black
    rotate(degrees = rotation, pivot = center) {
        val path = Path().apply {
            moveTo(center.x, center.y)
            arcTo(
                rect = Rect(
                    center.x - radius,
                    center.y - radius,
                    center.x + radius,
                    center.y + radius
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 120f,
                forceMoveTo = false
            )
            close()
        }

        drawPath(
            path = path,
            brush = Brush.sweepGradient(
                colors = listOf(
                    OpticalPureBlack,
                    OpticalPureWhite,
                    OpticalPureBlack
                ),
                center = center
            )
        )
    }

    // Concentric radial tick marks
    val rings = if (isFullScreen) 12 else 6
    for (r in 1..rings) {
        val ringR = (radius / rings) * r
        drawCircle(
            color = OpticalPureWhite,
            radius = ringR,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        val ticks = 36
        val angleStep = (2 * PI / ticks).toFloat()
        for (t in 0 until ticks) {
            val angle = t * angleStep
            val innerX = center.x + (ringR - 10f) * cos(angle)
            val innerY = center.y + (ringR - 10f) * sin(angle)
            val outerX = center.x + (ringR + 10f) * cos(angle)
            val outerY = center.y + (ringR + 10f) * sin(angle)
            drawLine(
                color = OpticalPureWhite,
                start = Offset(innerX, innerY),
                end = Offset(outerX, outerY),
                strokeWidth = 3f
            )
        }
    }
}

/**
 * Draw high-contrast crosshairs to center the optical sensor.
 */
private fun DrawScope.drawSensorCrosshairs(center: Offset) {
    val crosshairColor = CorporateGreen
    val crosshairLen = 32.dp.toPx()
    val gap = 14.dp.toPx()

    // Up
    drawLine(
        color = crosshairColor,
        start = Offset(center.x, center.y - gap - crosshairLen),
        end = Offset(center.x, center.y - gap),
        strokeWidth = 3.5.dp.toPx()
    )
    // Down
    drawLine(
        color = crosshairColor,
        start = Offset(center.x, center.y + gap),
        end = Offset(center.x, center.y + gap + crosshairLen),
        strokeWidth = 3.5.dp.toPx()
    )
    // Left
    drawLine(
        color = crosshairColor,
        start = Offset(center.x - gap - crosshairLen, center.y),
        end = Offset(center.x - gap, center.y),
        strokeWidth = 3.5.dp.toPx()
    )
    // Right
    drawLine(
        color = crosshairColor,
        start = Offset(center.x + gap, center.y),
        end = Offset(center.x + gap + crosshairLen, center.y),
        strokeWidth = 3.5.dp.toPx()
    )

    // Center targeting ring
    drawCircle(
        color = crosshairColor,
        radius = gap,
        center = center,
        style = Stroke(width = 3.dp.toPx())
    )
}
