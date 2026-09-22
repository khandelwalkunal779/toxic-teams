package com.toxicteams.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toxicteams.app.R
import com.toxicteams.app.data.JigglerPhase
import com.toxicteams.app.ui.theme.CorporateGreen
import com.toxicteams.app.ui.theme.CorporateGreenDark
import com.toxicteams.app.ui.theme.CorporateGreenGlow
import com.toxicteams.app.ui.theme.ElectricLavender
import com.toxicteams.app.ui.theme.ElectricLavenderDark
import com.toxicteams.app.ui.theme.SlateBorder
import com.toxicteams.app.ui.theme.SlateSurfaceVariant
import com.toxicteams.app.ui.theme.TextHighEmphasis
import com.toxicteams.app.ui.theme.TextMediumEmphasis

@Composable
fun StatusBadge(
    modifier: Modifier = Modifier,
    phase: JigglerPhase
) {
    val infiniteTransition = rememberInfiniteTransition(label = "StatusGlow")

    // Pulsing halo scale for active state
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ActiveHaloScale"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ActiveHaloAlpha"
    )

    val badgeBgColor by animateColorAsState(
        targetValue = when (phase) {
            JigglerPhase.ACTIVE -> CorporateGreenDark.copy(alpha = 0.25f)
            JigglerPhase.SLEEP -> ElectricLavenderDark.copy(alpha = 0.25f)
            JigglerPhase.IDLE -> SlateSurfaceVariant
        },
        label = "BadgeBgColor"
    )

    val badgeBorderColor by animateColorAsState(
        targetValue = when (phase) {
            JigglerPhase.ACTIVE -> CorporateGreen.copy(alpha = 0.7f)
            JigglerPhase.SLEEP -> ElectricLavender.copy(alpha = 0.6f)
            JigglerPhase.IDLE -> SlateBorder
        },
        label = "BadgeBorderColor"
    )

    val dotColor by animateColorAsState(
        targetValue = when (phase) {
            JigglerPhase.ACTIVE -> CorporateGreenGlow
            JigglerPhase.SLEEP -> ElectricLavender
            JigglerPhase.IDLE -> Color(0xFF64748B)
        },
        label = "DotColor"
    )

    val statusText = when (phase) {
        JigglerPhase.ACTIVE -> stringResource(R.string.status_active)
        JigglerPhase.SLEEP -> stringResource(R.string.status_sleep)
        JigglerPhase.IDLE -> stringResource(R.string.status_idle)
    }

    Row(
        modifier = modifier
            .background(badgeBgColor, shape = RoundedCornerShape(20.dp))
            .border(1.dp, badgeBorderColor, shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing Indicator Dot
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(14.dp)
        ) {
            if (phase == JigglerPhase.ACTIVE) {
                // Expanding glow halo
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .scale(haloScale)
                        .background(CorporateGreenGlow.copy(alpha = haloAlpha), CircleShape)
                )
            }
            // Core indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = if (phase == JigglerPhase.ACTIVE) TextHighEmphasis else TextMediumEmphasis
        )
    }
}

