package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MarketRateEntity
import com.example.ui.PriceFlashType
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RateDecrease
import com.example.ui.theme.RateDecreaseBg
import com.example.ui.theme.RateIncrease
import com.example.ui.theme.RateIncreaseBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PersianFormatters

@Composable
fun MarketRateCard(
    rate: MarketRateEntity,
    flashType: PriceFlashType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onShareClick: ((MarketRateEntity) -> Unit)? = null
) {
    val animatedBorderColor by animateColorAsState(
        targetValue = when (flashType) {
            PriceFlashType.INCREASE -> RateIncrease
            PriceFlashType.DECREASE -> RateDecrease
            PriceFlashType.NONE -> Color(0xFF232730)
        },
        animationSpec = tween(durationMillis = 400),
        label = "cardBorderColor"
    )

    val animatedFlashOverlay by animateColorAsState(
        targetValue = when (flashType) {
            PriceFlashType.INCREASE -> RateIncreaseBg.copy(alpha = 0.35f)
            PriceFlashType.DECREASE -> RateDecreaseBg.copy(alpha = 0.35f)
            PriceFlashType.NONE -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 400),
        label = "cardFlashOverlay"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF14171C))
            .border(
                width = 1.dp,
                color = if (flashType != PriceFlashType.NONE) {
                    animatedBorderColor
                } else {
                    Color(0xFF232830)
                },
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldAccent.copy(alpha = 0.15f)),
                onClick = onClick
            )
            .testTag("rate_card_${rate.id.lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(animatedFlashOverlay)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssetIconBadge(rateId = rate.id)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rate.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(RateIncrease.copy(alpha = pulseAlpha))
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = rate.symbol,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                if (rate.changePercent != null && rate.changePercent != 0.0) {
                    val changeColor = if (rate.changePercent > 0) RateIncrease else RateDecrease
                    val changeBg = if (rate.changePercent > 0) RateIncreaseBg else RateDecreaseBg
                    val changeIcon = if (rate.changePercent > 0) {
                        Icons.AutoMirrored.Filled.TrendingUp
                    } else {
                        Icons.AutoMirrored.Filled.TrendingDown
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(changeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = changeIcon,
                            contentDescription = null,
                            tint = changeColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = PersianFormatters.formatPercentage(rate.changePercent),
                            style = MaterialTheme.typography.labelMedium,
                            color = changeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = PersianFormatters.formatPrice(rate.price),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = rate.unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onShareClick != null) {
                        IconButton(
                            onClick = { onShareClick(rate) },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1C222E))
                                .border(1.dp, Color(0xFF2C3648), CircleShape)
                                .testTag("share_rate_${rate.id.lowercase()}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "اشتراک‌گذاری قیمت ${rate.name}",
                                tint = GoldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "جزئیات",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary.copy(alpha = 0.8f)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Distinctive, high-craft graphical asset icons
 */
@Composable
private fun AssetIconBadge(
    rateId: String,
    modifier: Modifier = Modifier
) {
    val flagMap = mapOf(
        "USD" to "🇺🇸",
        "EUR" to "🇪🇺",
        "AED" to "🇦🇪",
        "GBP" to "🇬🇧",
        "TRY" to "🇹🇷",
        "CAD" to "🇨🇦"
    )

    val cryptoSymbolMap = mapOf(
        "USDT" to Pair("₮", Color(0xFF26A17B)),
        "BTC" to Pair("₿", Color(0xFFF7931A)),
        "ETH" to Pair("Ξ", Color(0xFF627EEA)),
        "SOL" to Pair("◎", Color(0xFF14F195)),
        "BNB" to Pair("B", Color(0xFFF3BA2F)),
        "DOGE" to Pair("Ð", Color(0xFFC2A633))
    )

    when {
        flagMap.containsKey(rateId) -> {
            Box(
                modifier = modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF10141A))
                    .border(1.dp, Color(0xFF243040), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = flagMap[rateId] ?: "💵",
                    fontSize = 20.sp
                )
            }
        }
        cryptoSymbolMap.containsKey(rateId) -> {
            val (symbol, brandColor) = cryptoSymbolMap[rateId]!!
            Box(
                modifier = modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brandColor.copy(alpha = 0.14f))
                    .border(1.dp, brandColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    color = brandColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
        rateId == "GOLD_18K" || rateId == "GOLD_MESGHAL" -> {
            Box(
                modifier = modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A150C))
                    .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    val w = size.width
                    val h = size.height
                    val ingotPath = Path().apply {
                        moveTo(w * 0.15f, h * 0.85f)
                        lineTo(w * 0.85f, h * 0.85f)
                        lineTo(w * 0.72f, h * 0.25f)
                        lineTo(w * 0.28f, h * 0.25f)
                        close()
                    }
                    drawPath(
                        path = ingotPath,
                        brush = Brush.linearGradient(
                            listOf(GoldAccent, Color(0xFFF3C769), GoldAccent, Color(0xFFB07F24))
                        )
                    )
                }
            }
        }
        else -> {
            Box(
                modifier = modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A150C))
                    .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(22.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f * 0.85f
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(GoldAccent, Color(0xFFFBE49C), GoldAccent, Color(0xFF986D1E), GoldAccent)
                        ),
                        radius = radius,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF1E170A),
                        radius = radius * 0.72f,
                        center = center
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFFFBE49C), GoldAccent)
                        ),
                        radius = radius * 0.42f,
                        center = center
                    )
                }
            }
        }
    }
}
