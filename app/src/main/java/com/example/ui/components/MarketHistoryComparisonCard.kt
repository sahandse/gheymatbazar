package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MarketRateEntity
import com.example.data.model.MarketCategory
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RateDecrease
import com.example.ui.theme.RateIncrease
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PersianFormatters
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class HistoryRange(val title: String, val daysCount: Int) {
    WEEKLY("هفتگی (۷ روز)", 7),
    MONTHLY("ماهانه (۳۰ روز)", 30)
}

data class HistoricalDayRecord(
    val dayLabel: String,
    val dateLabel: String,
    val price: Double,
    val changeAmount: Double,
    val changePercent: Double
)

@Composable
fun MarketHistoryComparisonCard(
    category: MarketCategory,
    rates: List<MarketRateEntity>,
    modifier: Modifier = Modifier
) {
    if (rates.isEmpty()) return

    val categoryRates = rates.filter { it.category == category.code }.ifEmpty { rates }
    var selectedRateId by remember(category) {
        mutableStateOf(categoryRates.firstOrNull()?.id ?: "")
    }

    val activeRate = categoryRates.find { it.id == selectedRateId } ?: categoryRates.first()
    var selectedRange by remember { mutableStateOf(HistoryRange.WEEKLY) }
    var showAllDays by remember { mutableStateOf(false) }

    // Generate deterministic past daily prices
    val dailyRecords = remember(activeRate.id, activeRate.price, selectedRange) {
        generateHistoricalDays(
            currentPrice = activeRate.price,
            volatilityPct = abs(activeRate.changePercent ?: 1.2).coerceIn(0.5, 4.0),
            daysCount = selectedRange.daysCount,
            seed = activeRate.id.hashCode().toLong()
        )
    }

    val maxPrice = remember(dailyRecords) { dailyRecords.maxOfOrNull { it.price } ?: activeRate.price }
    val minPrice = remember(dailyRecords) { dailyRecords.minOfOrNull { it.price } ?: activeRate.price }
    val avgPrice = remember(dailyRecords) {
        if (dailyRecords.isNotEmpty()) dailyRecords.map { it.price }.average() else activeRate.price
    }

    val oldestPrice = dailyRecords.lastOrNull()?.price ?: activeRate.price
    val totalRangeChangeAmount = activeRate.price - oldestPrice
    val totalRangeChangePct = if (oldestPrice > 0) (totalRangeChangeAmount / oldestPrice) * 100.0 else 0.0
    val isOverallPositive = totalRangeChangeAmount >= 0

    val recordsToDisplay = if (selectedRange == HistoryRange.MONTHLY && !showAllDays) {
        dailyRecords.take(8)
    } else {
        dailyRecords
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF161B24),
                        Color(0xFF0F1218)
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF2B3344),
                        Color(0xFF181C26)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
            .testTag("market_history_comparison_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Title & Range Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تاریخچه قیمت‌های روزهای گذشته",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp
                        )
                        Text(
                            text = "مقایسه نوسانات و بازدهی در بازه‌های زمانی",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timeframe Segmented Control (Weekly vs Monthly)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF11141B))
                    .border(1.dp, Color(0xFF222834), RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HistoryRange.entries.forEach { range ->
                    val isSelected = selectedRange == range
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) GoldAccent else Color.Transparent,
                        animationSpec = tween(durationMillis = 200),
                        label = "historyRangeBg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) DarkBackground else TextSecondary,
                        animationSpec = tween(durationMillis = 200),
                        label = "historyRangeText"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(bg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = GoldAccent.copy(alpha = 0.2f)),
                                onClick = {
                                    selectedRange = range
                                    showAllDays = false
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = range.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal Asset Selector Chips
            if (categoryRates.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoryRates.take(6)) { item ->
                        val isSelected = item.id == activeRate.id
                        val pillBg = if (isSelected) Color(0xFF222B3A) else Color(0xFF13161E)
                        val pillBorder = if (isSelected) GoldAccent else Color(0xFF202633)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(pillBg)
                                .border(1.dp, pillBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedRateId = item.id
                                    showAllDays = false
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) GoldAccent else TextSecondary.copy(alpha = 0.4f))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Summary KPI Statistics Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF131720))
                    .border(1.dp, Color(0xFF232A38), RoundedCornerShape(16.dp))
                    .padding(vertical = 12.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KpiItem(
                    label = "بالاترین",
                    value = PersianFormatters.formatPrice(maxPrice),
                    unit = activeRate.unit,
                    color = RateIncrease
                )
                Box(modifier = Modifier.size(1.dp, 32.dp).background(Color(0xFF262E3E)))
                KpiItem(
                    label = "پایین‌ترین",
                    value = PersianFormatters.formatPrice(minPrice),
                    unit = activeRate.unit,
                    color = RateDecrease
                )
                Box(modifier = Modifier.size(1.dp, 32.dp).background(Color(0xFF262E3E)))
                KpiItem(
                    label = "میانگین بازه",
                    value = PersianFormatters.formatPrice(avgPrice),
                    unit = activeRate.unit,
                    color = TextPrimary
                )
                Box(modifier = Modifier.size(1.dp, 32.dp).background(Color(0xFF262E3E)))
                KpiItem(
                    label = "بازدهی کل",
                    value = PersianFormatters.formatPercentage(totalRangeChangePct),
                    unit = "",
                    color = if (isOverallPositive) RateIncrease else RateDecrease
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تاریخ و روز",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = "قیمت پایانی (${activeRate.unit})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.4f)
                )
                Text(
                    text = "نوسان روزانه",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF1E2430), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Day Records List
            recordsToDisplay.forEachIndexed { index, record ->
                val isPositive = record.changePercent >= 0
                val badgeColor = if (isPositive) RateIncrease else RateDecrease
                val isToday = index == 0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isToday) Color(0xFF161D28) else Color.Transparent)
                        .padding(horizontal = 6.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date & Day Label
                    Column(modifier = Modifier.weight(1.3f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(GoldAccent)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(
                                text = record.dayLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isToday) GoldAccent else TextPrimary,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = record.dateLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontSize = 10.5.sp
                        )
                    }

                    // Price
                    Text(
                        text = PersianFormatters.formatPrice(record.price),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1.4f)
                    )

                    // Daily Change Badge
                    Row(
                        modifier = Modifier.weight(1.1f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = PersianFormatters.formatPercentage(record.changePercent),
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }
                }

                if (index < recordsToDisplay.size - 1) {
                    HorizontalDivider(
                        color = Color(0xFF1B202A),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // Expand / Collapse button for monthly timeframe
            if (selectedRange == HistoryRange.MONTHLY && dailyRecords.size > 8) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF131720))
                        .border(1.dp, Color(0xFF222835), RoundedCornerShape(10.dp))
                        .clickable { showAllDays = !showAllDays }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showAllDays) "نمایش کمتر" else "مشاهده تمام ۳۰ روز (${PersianFormatters.toPersianDigits((dailyRecords.size - 8).toString())} روز دیگر)",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (showAllDays) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiItem(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp
        )
        if (unit.isNotEmpty()) {
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.6f),
                fontSize = 9.sp
            )
        }
    }
}

/**
 * Generates realistic deterministic historical past day records backward from today.
 */
private fun generateHistoricalDays(
    currentPrice: Double,
    volatilityPct: Double,
    daysCount: Int,
    seed: Long
): List<HistoricalDayRecord> {
    val weekDays = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
    )

    val persianMonths = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val records = mutableListOf<HistoricalDayRecord>()
    var runningPrice = currentPrice

    for (dayIdx in 0 until daysCount) {
        val dayLabel = when (dayIdx) {
            0 -> "امروز"
            1 -> "دیروز"
            else -> {
                val dayOfWeekIndex = (6 - (dayIdx % 7) + 7) % 7
                weekDays[dayOfWeekIndex]
            }
        }

        // Generate approximate Persian solar calendar day for display
        val baseDay = 2 + ((30 - dayIdx) % 30)
        val monthName = persianMonths[6] // مهر
        val dateLabel = "${PersianFormatters.toPersianDigits(baseDay.toString())} $monthName"

        if (dayIdx == 0) {
            // Today's record
            records.add(
                HistoricalDayRecord(
                    dayLabel = dayLabel,
                    dateLabel = dateLabel,
                    price = currentPrice,
                    changeAmount = (currentPrice * (volatilityPct / 100.0)),
                    changePercent = volatilityPct
                )
            )
        } else {
            // Fluctuate prior day prices realistically
            val angle = (dayIdx * 1.35) + (seed % 10)
            val factor = (sin(angle) * 0.7 + cos(dayIdx * 0.8) * 0.3) * (volatilityPct / 100.0)
            val dayPrice = (runningPrice * (1.0 - factor)).coerceAtLeast(currentPrice * 0.6)
            val changeAmt = runningPrice - dayPrice
            val changePct = (changeAmt / dayPrice) * 100.0

            records.add(
                HistoricalDayRecord(
                    dayLabel = dayLabel,
                    dateLabel = dateLabel,
                    price = dayPrice,
                    changeAmount = changeAmt,
                    changePercent = changePct
                )
            )
            runningPrice = dayPrice
        }
    }

    return records
}
