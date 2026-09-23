package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    // Dynamic flash colors on price changes
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

    // Live pulse animation for status indicator
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

    val isPositive = (rate.changePercent ?: 0.0) >= 0.0
    val trendColor = if (isPositive) RateIncrease else RateDecrease

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF161920),
                        Color(0xFF101217)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .border(
                width = 1.dp,
                brush = if (flashType != PriceFlashType.NONE) {
                    Brush.linearGradient(listOf(animatedBorderColor, animatedBorderColor))
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A2F3A),
                            Color(0xFF181B22)
                        )
                    )
                },
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldAccent.copy(alpha = 0.15f)),
                onClick = onClick
            )
            .testTag("rate_card_${rate.id.lowercase()}")
    ) {
        // Decorative background mini sparkline chart
        CardSparklineBackground(
            isPositive = isPositive,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .align(Alignment.BottomCenter)
        )

        // Flash overlay effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(animatedFlashOverlay)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Top Row: Asset Icon, Names, and Trend Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssetIconBadge(rateId = rate.id)

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rate.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Live pulse dot
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(RateIncrease.copy(alpha = pulseAlpha))
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        val subLabel = when (rate.id) {
                            "USD" -> "دلار بازار آزاد • نقدی"
                            "EUR" -> "یورو اتحادیه اروپا"
                            "AED" -> "درهم امارات متحده عربی"
                            "GBP" -> "پوند بریتانیا"
                            "TRY" -> "لیر ترکیه"
                            "CAD" -> "دلار کانادا"
                            "GOLD_18K" -> "طلای ۱۸ عیار • هر گرم"
                            "GOLD_MESGHAL" -> "مثقال طلا (مظنه)"
                            "COIN_EMAMI" -> "سکه تمام طرح جدید (امامی)"
                            "COIN_BAHAR" -> "سکه تمام طرح قدیم (بهار آزادی)"
                            "COIN_NIM" -> "نیم سکه بهار آزادی"
                            "COIN_ROB" -> "ربع سکه بهار آزادی"
                            "COIN_GERAMI" -> "سکه گرمی بانک مرکزی"
                            "USDT" -> "تتر دیجیتال • دلاری"
                            "BTC" -> "بیت‌کوین • پادشاه کریپتو"
                            "ETH" -> "اتریوم • شبکه قرارداد هوشمند"
                            "SOL" -> "سولانا • بلاکچین نسل ۳"
                            "BNB" -> "بایننس کوین"
                            "DOGE" -> "دوج‌کوین"
                            else -> rate.symbol
                        }
                        Text(
                            text = subLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Trend percentage pill
                if (rate.changePercent != null && rate.changePercent != 0.0) {
                    val changeColor = if (rate.changePercent > 0) RateIncrease else RateDecrease
                    val changeBg = if (rate.changePercent > 0) RateIncreaseBg else RateDecreaseBg
                    val changeIcon = if (rate.changePercent > 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(changeBg)
                            .border(1.dp, changeColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = changeIcon,
                            contentDescription = null,
                            tint = changeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
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

            Spacer(modifier = Modifier.height(22.dp))

            // Bottom Row: Price value and 24-hour status indicator badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = PersianFormatters.formatPrice(rate.price),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 23.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = rate.unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Status Indicator: 24h Trend (Bullish / Bearish with Green/Red Color & Percentage)
                    PriceTrend24hBadge(
                        changePercent = rate.changePercent,
                        showTimeframeTag = true
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onShareClick != null) {
                        IconButton(
                            onClick = { onShareClick(rate) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1C222E))
                                .border(1.dp, Color(0xFF2C3648), CircleShape)
                                .testTag("share_rate_${rate.id.lowercase()}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "اشتراک‌گذاری قیمت ${rate.name}",
                                tint = GoldAccent,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
 * Elegant minimalist graphical sparkline rendered directly on Canvas
 */
@Composable
private fun CardSparklineBackground(
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val strokeColor = if (isPositive) RateIncrease.copy(alpha = 0.45f) else RateDecrease.copy(alpha = 0.45f)
    val gradientColor = if (isPositive) RateIncrease.copy(alpha = 0.12f) else RateDecrease.copy(alpha = 0.12f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path()
        val fillPath = Path()

        // Generate a smooth simulated chart curve reflecting market sentiment
        val startY = if (isPositive) h * 0.75f else h * 0.35f
        val cp1X = w * 0.25f
        val cp1Y = if (isPositive) h * 0.65f else h * 0.40f
        val cp2X = w * 0.55f
        val cp2Y = if (isPositive) h * 0.45f else h * 0.60f
        val endX = w
        val endY = if (isPositive) h * 0.25f else h * 0.80f

        path.moveTo(0f, startY)
        path.cubicTo(cp1X, cp1Y, cp2X, cp2Y, endX, endY)

        fillPath.addPath(path)
        fillPath.lineTo(w, h)
        fillPath.lineTo(0f, h)
        fillPath.close()

        // Draw soft vertical gradient area
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(gradientColor, Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        // Draw glowing sparkline
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw radiant dot at curve tip
        drawCircle(
            color = strokeColor,
            radius = 3.5.dp.toPx(),
            center = Offset(endX - 16.dp.toPx(), if (isPositive) h * 0.27f else h * 0.76f)
        )
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
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF161E28), Color(0xFF0F141B))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(Color(0xFF2E3D52), Color(0xFF1A222D))
                        ),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = flagMap[rateId] ?: "💵",
                    fontSize = 22.sp
                )
            }
        }
        cryptoSymbolMap.containsKey(rateId) -> {
            val (symbol, brandColor) = cryptoSymbolMap[rateId]!!
            Box(
                modifier = modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(brandColor.copy(alpha = 0.18f), Color(0xFF0F1116))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(brandColor.copy(alpha = 0.45f), Color(0xFF1E2129))
                        ),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    color = brandColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
        rateId == "GOLD_18K" || rateId == "GOLD_MESGHAL" -> {
            Box(
                modifier = modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF281F10), Color(0xFF18130A))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(GoldAccent.copy(alpha = 0.5f), Color(0xFF2B200E))
                        ),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(22.dp)) {
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
            // Coins (Emami, Bahar, Nim, Rob, Gerami)
            Box(
                modifier = modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2A200F), Color(0xFF1A1308))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(GoldAccent.copy(alpha = 0.5f), Color(0xFF332510))
                        ),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f * 0.85f
                    // Outer coin rim
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(GoldAccent, Color(0xFFFBE49C), GoldAccent, Color(0xFF986D1E), GoldAccent)
                        ),
                        radius = radius,
                        center = center
                    )
                    // Inner relief circle
                    drawCircle(
                        color = Color(0xFF1E170A),
                        radius = radius * 0.72f,
                        center = center
                    )
                    // Inner emblem
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
