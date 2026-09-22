package com.toxicteams.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toxicteams.app.R
import com.toxicteams.app.ui.theme.CorporateGreen
import com.toxicteams.app.ui.theme.ElectricLavender
import com.toxicteams.app.ui.theme.SlateBorder
import com.toxicteams.app.ui.theme.SlateSurfaceVariant
import com.toxicteams.app.ui.theme.TextHighEmphasis
import com.toxicteams.app.ui.theme.TextLowEmphasis
import com.toxicteams.app.ui.theme.TextMediumEmphasis

@Composable
fun PlacementGuideCard(
    modifier: Modifier = Modifier
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElectricLavender.copy(alpha = 0.15f))
                        .border(1.dp, ElectricLavender.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mouse,
                        contentDescription = null,
                        tint = ElectricLavender,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = stringResource(R.string.guide_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextHighEmphasis
                    )
                    Text(
                        text = "Physical hardware alignment",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMediumEmphasis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step items
            GuideStepItem(
                number = "1",
                text = stringResource(R.string.guide_step_1),
                tint = CorporateGreen
            )

            Spacer(modifier = Modifier.height(10.dp))

            GuideStepItem(
                number = "2",
                text = stringResource(R.string.guide_step_2),
                tint = ElectricLavender
            )

            Spacer(modifier = Modifier.height(10.dp))

            GuideStepItem(
                number = "3",
                text = stringResource(R.string.guide_step_3),
                tint = CorporateGreen
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tip footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141923))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextLowEmphasis,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.guide_note),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextMediumEmphasis
                )
            }
        }
    }
}

@Composable
private fun GuideStepItem(
    number: String,
    text: String,
    tint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.2f))
                .border(1.dp, tint.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = tint
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextMediumEmphasis,
            modifier = Modifier.weight(1f)
        )
    }
}

