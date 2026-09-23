package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
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
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val flashBg by animateColorAsState(
        targetValue = when (flashType) {
            PriceFlashType.INCREASE -> palette.increaseBg
            PriceFlashType.DECREASE -> palette.decreaseBg
            PriceFlashType.NONE -> Color.Transparent
        },
        animationSpec = tween(280),
        label = "rowFlash"
    )
    val change = rate.changePercent
    val changeColor = when {
        change == null || change == 0.0 -> palette.textSecondary
        change > 0 -> palette.increase
        else -> palette.decrease
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (compact) 14.dp else 16.dp))
            .background(palette.card)
            .background(flashBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = palette.accent.copy(alpha = 0.08f)),
                onClick = onClick
            )
            .testTag("rate_card_${rate.id.lowercase()}")
            .height(if (compact) 54.dp else 66.dp)
            .padding(start = 12.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(if (compact) 30.dp else 36.dp)
                .clip(CircleShape)
                .background(palette.cardSecondary),
            contentAlignment = Alignment.Center
        ) {
            Text(MarketAssetHelper.getAssetIcon(rate.id), fontSize = if (compact) 13.sp else 15.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rate.name,
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (compact) 13.sp else 14.sp,
                maxLines = 1
            )
            if (!compact) {
                Text(
                    text = rate.symbol,
                    color = palette.textSecondary.copy(alpha = 0.75f),
                    fontSize = 10.sp
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = PersianFormatters.formatPrice(rate.price),
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 14.sp else 15.sp
            )
            Text(
                text = change?.let { PersianFormatters.formatPercentage(it) } ?: rate.unit,
                color = changeColor,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            )
        }

        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .size(34.dp)
                .testTag("favorite_${rate.id.lowercase()}")
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = if (isFavorite) "حذف از علاقه‌مندی" else "افزودن به علاقه‌مندی",
                tint = if (isFavorite) palette.accent else palette.textSecondary.copy(alpha = 0.35f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
