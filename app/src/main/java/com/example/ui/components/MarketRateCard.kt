package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MarketRateEntity
import com.example.ui.PriceFlashType
import com.example.ui.theme.LocalAppPalette
import com.example.util.MarketAssetHelper
import com.example.util.PersianFormatters
import kotlinx.coroutines.launch

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
    val enterAlpha = remember { Animatable(0f) }
    val enterOffset = remember { Animatable(10f) }
    LaunchedEffect(rate.id) {
        enterAlpha.snapTo(0f)
        enterOffset.snapTo(10f)
        launch {
            enterAlpha.animateTo(1f, tween(280, easing = FastOutSlowInEasing))
        }
        enterOffset.animateTo(0f, tween(280, easing = FastOutSlowInEasing))
    }
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
    val accentBar = when {
        isFavorite -> palette.accent
        changePercent != null && changePercent > 0 -> palette.increase.copy(alpha = 0.45f)
        changePercent != null && changePercent < 0 -> palette.decrease.copy(alpha = 0.45f)
        else -> Color.Transparent
    }

    val rowHeight: Dp = if (compact) 56.dp else 72.dp
    val iconSize: Dp = if (compact) 30.dp else 38.dp
    val nameSize = if (compact) 13.sp else 15.sp
    val priceSize = if (compact) 14.sp else 16.sp
    val corner = if (compact) 16.dp else 20.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = enterAlpha.value
                translationY = enterOffset.value
            }
            .clip(RoundedCornerShape(corner))
            .background(palette.card.copy(alpha = 0.88f))
            .background(flashBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = palette.accent.copy(alpha = 0.1f)),
                onClick = onClick
            )
            .testTag("rate_card_${rate.id.lowercase()}")
            .height(rowHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .padding(vertical = 14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentBar)
        )

        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .size(if (compact) 34.dp else 38.dp)
                .testTag("favorite_${rate.id.lowercase()}")
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = if (isFavorite) "حذف از علاقه‌مندی" else "افزودن به علاقه‌مندی",
                tint = if (isFavorite) palette.accent else palette.textSecondary.copy(alpha = 0.55f),
                modifier = Modifier.size(if (compact) 16.dp else 18.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .background(palette.cardSecondary.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = MarketAssetHelper.getAssetIcon(rate.id),
                fontSize = if (compact) 13.sp else 15.sp
            )
        }

        Spacer(modifier = Modifier.width(if (compact) 8.dp else 12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rate.name,
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = nameSize
            )
            if (!compact) {
                Text(
                    text = rate.symbol,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(end = if (compact) 14.dp else 16.dp)
        ) {
            Text(
                text = PersianFormatters.formatPrice(rate.price),
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = priceSize
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
                fontSize = if (compact) 10.sp else 11.sp
            )
        }
    }
}
