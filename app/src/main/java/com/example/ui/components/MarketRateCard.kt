package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MarketRateEntity
import com.example.ui.PriceFlashType
import com.example.ui.theme.LocalAppPalette
import com.example.util.MarketAssetHelper
import com.example.util.PersianFormatters

@Composable
fun MarketRateCard(
    rate: MarketRateEntity,
    flashType: PriceFlashType,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val flashBg by animateColorAsState(
        targetValue = when (flashType) {
            PriceFlashType.INCREASE -> palette.increaseBg
            PriceFlashType.DECREASE -> palette.decreaseBg
            PriceFlashType.NONE -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 350),
        label = "rowFlash"
    )

    val changePercent = rate.changePercent
    val changeColor = when {
        changePercent == null || changePercent == 0.0 -> palette.textSecondary
        changePercent > 0 -> palette.increase
        else -> palette.decrease
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(flashBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = palette.accent.copy(alpha = 0.12f)),
                    onClick = onClick
                )
                .padding(vertical = 12.dp)
                .testTag("rate_card_${rate.id.lowercase()}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("favorite_${rate.id.lowercase()}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = if (isFavorite) "حذف از علاقه‌مندی" else "افزودن به علاقه‌مندی",
                        tint = if (isFavorite) palette.accent else palette.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(palette.cardSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = MarketAssetHelper.getAssetIcon(rate.id),
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = rate.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                    Text(
                        text = rate.symbol,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = PersianFormatters.formatPrice(rate.price),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (changePercent != null) {
                        PersianFormatters.formatPercentage(changePercent)
                    } else {
                        rate.unit
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = changeColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
        HorizontalDivider(color = palette.border.copy(alpha = 0.5f), thickness = 0.5.dp)
    }
}
