package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MarketRateEntity
import com.example.ui.components.PriceTrend24hBadge
import com.example.ui.components.ShareOptionsBottomSheet
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RateDecrease
import com.example.ui.theme.RateDecreaseBg
import com.example.ui.theme.RateIncrease
import com.example.ui.theme.RateIncreaseBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PersianFormatters
import kotlin.math.abs

@Composable
fun DetailScreen(
    rate: MarketRateEntity,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showShareSheet by remember { mutableStateOf(false) }

    val isPositive = (rate.changePercent ?: 0.0) >= 0.0
    val trendColor = if (isPositive) RateIncrease else RateDecrease
    val glowColor = if (rate.id == "USD") RateIncrease else GoldAccent

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Ambient radial light glow in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.07f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.18f),
                    radius = size.width * 0.75f
                ),
                center = Offset(size.width * 0.5f, size.height * 0.18f),
                radius = size.width * 0.75f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF161920))
                            .border(
                                1.dp,
                                Brush.verticalGradient(listOf(Color(0xFF2B303C), Color(0xFF1B1E26))),
                                CircleShape
                            )
                            .testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = rate.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "اطلاعات آماری و تحلیلی بازار",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                // Share button in top bar
                IconButton(
                    onClick = { showShareSheet = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161920))
                        .border(
                            1.dp,
                            Brush.verticalGradient(listOf(Color(0xFF2B303C), Color(0xFF1B1E26))),
                            CircleShape
                        )
                        .testTag("detail_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "اشتراک‌گذاری ${rate.name}",
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grand Hero Price Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF171A22), Color(0xFF111319))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2E3442), Color(0xFF1A1D24))
                        ),
                        RoundedCornerShape(26.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (rate.id) {
                                    "USD" -> "🇺🇸"
                                    "EUR" -> "🇪🇺"
                                    "AED" -> "🇦🇪"
                                    "GBP" -> "🇬🇧"
                                    "TRY" -> "🇹🇷"
                                    "CAD" -> "🇨🇦"
                                    "USDT" -> "₮"
                                    "BTC" -> "₿"
                                    "ETH" -> "Ξ"
                                    "SOL" -> "◎"
                                    "BNB" -> "B"
                                    "DOGE" -> "Ð"
                                    "GOLD_18K", "GOLD_MESGHAL" -> "🥇"
                                    else -> "🪙"
                                },
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = rate.symbol,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "واحد محاسبه: ${rate.unit}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Trend Badge
                        if (rate.changePercent != null && rate.changePercent != 0.0) {
                            val changeBg = if (isPositive) RateIncreaseBg else RateDecreaseBg
                            val changeIcon = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(changeBg)
                                    .border(1.dp, trendColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = changeIcon,
                                    contentDescription = null,
                                    tint = trendColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = PersianFormatters.formatPercentage(rate.changePercent),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = trendColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    Text(
                        text = "قیمت لحظه‌ای بازار",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = PersianFormatters.formatPrice(rate.price),
                                style = MaterialTheme.typography.displayLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = rate.unit,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            // 24H Status Indicator Badge right next to the price
                            PriceTrend24hBadge(
                                changePercent = rate.changePercent,
                                isLarge = true,
                                showTimeframeTag = true
                            )
                        }

                        // Share button directly beside price
                        IconButton(
                            onClick = { showShareSheet = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F2532))
                                .border(1.dp, GoldAccent.copy(alpha = 0.45f), CircleShape)
                                .testTag("detail_price_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "اشتراک‌گذاری نرخ",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Real Toman change difference if available
                    val diff = rate.changeAmount ?: (rate.previousPrice?.let { rate.price - it })
                    if (diff != null && diff != 0.0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        val isDiffUp = diff > 0
                        val diffColor = if (isDiffUp) RateIncrease else RateDecrease
                        val diffSign = if (isDiffUp) "+" else "-"

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1C2028))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تغییر مبلغ: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "$diffSign${PersianFormatters.formatPrice(abs(diff))} تومان",
                                style = MaterialTheme.typography.labelSmall,
                                color = diffColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Day's Range Visual Bar (Apple Stocks / Revolut style)
            if (rate.highPrice != null && rate.lowPrice != null && rate.highPrice > rate.lowPrice) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF14171E))
                        .border(1.dp, Color(0xFF222630), RoundedCornerShape(22.dp))
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "دامنه نوسان روزانه",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom graphical range slider track
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            val w = size.width
                            val h = size.height

                            // Background track
                            drawRoundRect(
                                color = Color(0xFF232732),
                                size = size
                            )

                            // Fraction of current price in [lowPrice, highPrice]
                            val range = rate.highPrice - rate.lowPrice
                            val fraction = ((rate.price - rate.lowPrice) / range).coerceIn(0.0, 1.0).toFloat()

                            // Active range fill
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    listOf(RateDecrease.copy(alpha = 0.6f), RateIncrease.copy(alpha = 0.9f))
                                ),
                                size = size.copy(width = w * fraction)
                            )

                            // Indicator dot
                            drawCircle(
                                color = GoldAccent,
                                radius = 6.dp.toPx(),
                                center = Offset(w * fraction, h / 2f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "کمترین روز",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${PersianFormatters.formatPrice(rate.lowPrice)} تومان",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "بیشترین روز",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${PersianFormatters.formatPrice(rate.highPrice)} تومان",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Technical Specifications Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF14171E))
                    .border(1.dp, Color(0xFF222630), RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مشخصات و متاداده ثبت‌شده",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailItemRow(label = "عنوان دارایی", value = rate.name)
                    HorizontalDivider(color = Color(0xFF20242D), thickness = 0.8.dp, modifier = Modifier.padding(vertical = 12.dp))

                    DetailItemRow(label = "نماد بازار", value = rate.symbol)
                    HorizontalDivider(color = Color(0xFF20242D), thickness = 0.8.dp, modifier = Modifier.padding(vertical = 12.dp))

                    DetailItemRow(label = "منبع استعلام", value = rate.provider)
                    HorizontalDivider(color = Color(0xFF20242D), thickness = 0.8.dp, modifier = Modifier.padding(vertical = 12.dp))

                    DetailItemRow(label = "زمان بروزرسانی", value = PersianFormatters.formatFullDateTime(rate.updatedAt))

                    if (rate.previousPrice != null && rate.previousPrice > 0) {
                        HorizontalDivider(color = Color(0xFF20242D), thickness = 0.8.dp, modifier = Modifier.padding(vertical = 12.dp))
                        DetailItemRow(
                            label = "نرخ قبلی در حافظه",
                            value = "${PersianFormatters.formatPrice(rate.previousPrice)} تومان"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showShareSheet) {
            ShareOptionsBottomSheet(
                rate = rate,
                onDismiss = { showShareSheet = false }
            )
        }
    }
}

@Composable
private fun DetailItemRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 13.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}
