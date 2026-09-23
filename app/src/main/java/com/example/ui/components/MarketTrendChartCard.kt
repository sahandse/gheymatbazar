package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.util.MarketAssetHelper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MarketRateEntity
import com.example.data.model.MarketCategory
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
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

enum class ChartTimeframe(val label: String, val pointCount: Int) {
    DAY_1("۲۴ ساعت", 12),
    WEEK_1("۷ روز", 14),
    MONTH_1("۱ ماه", 15),
    MONTH_3("۳ ماه", 16)
}

data class TrendPoint(
    val timeLabel: String,
    val price: Double
)

@Composable
fun MarketTrendChartCard(
    category: MarketCategory,
    rates: List<MarketRateEntity>,
    onRateClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeRateId: String? = null,
    onClose: (() -> Unit)? = null
) {
    if (rates.isEmpty()) return

    // Available benchmark rates in this category
    val categoryRates = rates.filter { it.category == category.code }.ifEmpty { rates }
    var internalSelectedRateId by remember(category) {
        mutableStateOf(categoryRates.firstOrNull()?.id ?: "")
    }

    LaunchedEffect(activeRateId) {
        if (!activeRateId.isNullOrEmpty() && rates.any { it.id == activeRateId }) {
            internalSelectedRateId = activeRateId
        }
    }

    // Current selected rate entity
    val activeRate = rates.find { it.id == (activeRateId ?: internalSelectedRateId) }
        ?: categoryRates.find { it.id == internalSelectedRateId }
        ?: categoryRates.first()

    var selectedTimeframe by remember { mutableStateOf(ChartTimeframe.DAY_1) }
    var showShareSheet by remember { mutableStateOf(false) }

    // Generate trend points for current rate and timeframe
    val trendPoints = remember(activeRate.id, activeRate.price, selectedTimeframe) {
        generateChartTrendPoints(
            basePrice = activeRate.price,
            changePct = activeRate.changePercent ?: 0.0,
            timeframe = selectedTimeframe,
            seed = activeRate.id.hashCode().toLong()
        )
    }

    // Scrubber position state (null when not dragging)
    var scrubberFraction by remember(activeRate.id, selectedTimeframe) { mutableStateOf<Float?>(null) }

    val hoveredPoint = scrubberFraction?.let { fraction ->
        if (trendPoints.isNotEmpty()) {
            val index = (fraction * (trendPoints.size - 1)).toInt().coerceIn(0, trendPoints.size - 1)
            trendPoints[index]
        } else null
    }

    val isTrendPositive = (activeRate.changePercent ?: 0.0) >= 0.0
    val trendColor = if (isTrendPositive) RateIncrease else RateDecrease

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF171B24),
                        Color(0xFF10131A)
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF283040),
                        Color(0xFF181C26)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
            .testTag("market_trend_chart_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Active asset icon & title + Timeframe chips & Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = MarketAssetHelper.getAssetIcon(activeRate.id),
                            fontSize = 17.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "نمودار ${activeRate.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${activeRate.symbol} • ${MarketAssetHelper.getCountryOrCategoryLabel(activeRate.id)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 10.5.sp
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timeframe Chips
                    ChartTimeframe.entries.forEach { tf ->
                        val isSelected = selectedTimeframe == tf
                        val bg = if (isSelected) GoldAccent else Color(0xFF1E2430)
                        val textColor = if (isSelected) DarkBackground else TextSecondary
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(bg)
                                .clickable { selectedTimeframe = tf }
                                .padding(horizontal = 7.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tf.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    if (onClose != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF222834))
                                .testTag("close_chart_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بستن نمودار",
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-selector for all assets in this category (horizontal scroll)
            if (categoryRates.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoryRates) { item ->
                        val isSelected = item.id == activeRate.id
                        val pillBg = if (isSelected) Color(0xFF2E2718) else Color(0xFF13171F)
                        val pillBorder = if (isSelected) GoldAccent else Color(0xFF222834)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(pillBg)
                                .border(1.dp, pillBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                internalSelectedRateId = item.id
                                scrubberFraction = null
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = MarketAssetHelper.getAssetIcon(item.id),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) GoldAccent else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

            // Price & Status readout (Changes dynamically with scrubber like Recharts Tooltip)
            val displayPrice = hoveredPoint?.price ?: activeRate.price
            val displayLabel = hoveredPoint?.timeLabel ?: "آخرین بروزرسانی"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = displayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hoveredPoint != null) GoldAccent else TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = PersianFormatters.formatPrice(displayPrice),
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activeRate.unit,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 3.dp),
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Percentage Badge
                    if (activeRate.changePercent != null && activeRate.changePercent != 0.0) {
                        val pctColor = if (isTrendPositive) RateIncrease else RateDecrease
                        val pctBg = if (isTrendPositive) RateIncrease.copy(alpha = 0.15f) else RateDecrease.copy(alpha = 0.15f)
                        val icon = if (isTrendPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(pctBg)
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = pctColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = PersianFormatters.formatPercentage(activeRate.changePercent),
                                style = MaterialTheme.typography.labelSmall,
                                color = pctColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Share button for chart and rate
                    IconButton(
                        onClick = { showShareSheet = true },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2430))
                            .border(1.dp, GoldAccent.copy(alpha = 0.45f), CircleShape)
                            .testTag("chart_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "اشتراک‌گذاری نمودار و قیمت",
                            tint = GoldAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Native D3/Recharts-inspired Interactive Canvas Chart
            D3RechartsCanvas(
                points = trendPoints,
                trendColor = trendColor,
                scrubberFraction = scrubberFraction,
                onScrubberChanged = { scrubberFraction = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            )

            // X-Axis labels
            if (trendPoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = trendPoints.first().timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    val midIndex = trendPoints.size / 2
                    Text(
                        text = trendPoints[midIndex].timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = trendPoints.last().timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }

        if (showShareSheet) {
            ShareOptionsBottomSheet(
                rate = activeRate,
                onDismiss = { showShareSheet = false }
            )
        }
    }
}

/**
 * High-performance smooth Canvas chart simulating Recharts/D3 Monotone spline
 * with interactive touch/drag scrubber, dotted Cartesian grid, and area fill.
 */
@Composable
private fun D3RechartsCanvas(
    points: List<TrendPoint>,
    trendColor: Color,
    scrubberFraction: Float?,
    onScrubberChanged: (Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return

    val minPrice = remember(points) { points.minOf { it.price } }
    val maxPrice = remember(points) { points.maxOf { it.price } }
    val priceSpan = if (maxPrice - minPrice > 0.0) maxPrice - minPrice else 1.0

    Box(
        modifier = modifier
            .pointerInput(points) {
                detectTapGestures(
                    onPress = { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onScrubberChanged(fraction)
                        tryAwaitRelease()
                        onScrubberChanged(null)
                    }
                )
            }
            .pointerInput(points) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onScrubberChanged(fraction)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        onScrubberChanged(fraction)
                    },
                    onDragEnd = {
                        onScrubberChanged(null)
                    },
                    onDragCancel = {
                        onScrubberChanged(null)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val topPadding = 16.dp.toPx()
            val bottomPadding = 16.dp.toPx()
            val usableH = h - topPadding - bottomPadding

            // 1. Cartesian Grid Lines (Recharts <CartesianGrid strokeDasharray="3 3" />)
            val gridColor = Color(0xFF222734)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

            for (i in 0..2) {
                val y = topPadding + (usableH * i / 2f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            }

            // Convert points to canvas coordinates
            val coords = points.mapIndexed { index, point ->
                val x = (index.toFloat() / (points.size - 1)) * w
                val normalizedY = ((point.price - minPrice) / priceSpan).toFloat()
                val y = topPadding + usableH * (1f - normalizedY)
                Offset(x, y)
            }

            // 2. Smooth Cubic Bezier Spline Path (D3 monotone curve)
            val strokePath = Path().apply {
                moveTo(coords[0].x, coords[0].y)
                for (i in 0 until coords.size - 1) {
                    val p0 = coords[i]
                    val p1 = coords[i + 1]
                    val cx = (p0.x + p1.x) / 2f
                    cubicTo(
                        cx, p0.y,
                        cx, p1.y,
                        p1.x, p1.y
                    )
                }
            }

            // 3. Area Gradient Fill (Recharts <Area />)
            val fillPath = Path().apply {
                addPath(strokePath)
                lineTo(coords.last().x, h)
                lineTo(coords.first().x, h)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        trendColor.copy(alpha = 0.28f),
                        trendColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    startY = topPadding,
                    endY = h
                )
            )

            // 4. Line Stroke (Recharts <Line />)
            drawPath(
                path = strokePath,
                brush = Brush.horizontalGradient(
                    listOf(
                        trendColor.copy(alpha = 0.7f),
                        trendColor,
                        GoldAccent
                    )
                ),
                style = Stroke(
                    width = 2.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // 5. Interactive Scrubber Tooltip & Indicator Line
            if (scrubberFraction != null) {
                val scrubX = (scrubberFraction * w).coerceIn(0f, w)
                val pointIndex = (scrubberFraction * (coords.size - 1)).toInt().coerceIn(0, coords.size - 1)
                val activeCoord = coords[pointIndex]

                // Vertical dashed scrubber guideline
                drawLine(
                    color = GoldAccent.copy(alpha = 0.8f),
                    start = Offset(scrubX, topPadding),
                    end = Offset(scrubX, h),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )

                // Glowing outer halo ring
                drawCircle(
                    color = GoldAccent.copy(alpha = 0.25f),
                    radius = 11.dp.toPx(),
                    center = activeCoord
                )

                // Solid inner dot
                drawCircle(
                    color = DarkBackground,
                    radius = 6.dp.toPx(),
                    center = activeCoord
                )
                drawCircle(
                    color = GoldAccent,
                    radius = 4.dp.toPx(),
                    center = activeCoord
                )
            } else {
                // Latest endpoint dot
                val lastCoord = coords.last()
                drawCircle(
                    color = GoldAccent.copy(alpha = 0.3f),
                    radius = 7.dp.toPx(),
                    center = lastCoord
                )
                drawCircle(
                    color = GoldAccent,
                    radius = 4.dp.toPx(),
                    center = lastCoord
                )
            }
        }
    }
}

/**
 * Deterministic, smooth realistic curve generator for market trends.
 */
private fun generateChartTrendPoints(
    basePrice: Double,
    changePct: Double,
    timeframe: TimeframeWithLabels,
    seed: Long
): List<TrendPoint> {
    val count = timeframe.pointCount
    val points = mutableListOf<TrendPoint>()

    // Starting price before timeframe change
    val changeFactor = changePct / 100.0
    val startPrice = if (1.0 + changeFactor > 0) basePrice / (1.0 + changeFactor) else basePrice * 0.98

    val labels = timeframe.generateLabels(count)

    for (i in 0 until count) {
        val progress = i.toDouble() / (count - 1).coerceAtLeast(1)
        // Trend component
        val trend = startPrice + (basePrice - startPrice) * progress

        // Smooth wave fluctuations
        val wave1 = sin(progress * Math.PI * 2.5 + (seed % 10)) * (basePrice * 0.008)
        val wave2 = cos(progress * Math.PI * 4.0 + (seed % 7)) * (basePrice * 0.004)
        val noise = if (i == count - 1) 0.0 else (wave1 + wave2)

        val price = (trend + noise).coerceAtLeast(basePrice * 0.5)
        points.add(
            TrendPoint(
                timeLabel = labels.getOrElse(i) { "" },
                price = if (i == count - 1) basePrice else price
            )
        )
    }

    return points
}

// Extension to bridge ChartTimeframe
private val ChartTimeframe.pointCountVal: Int
    get() = when (this) {
        ChartTimeframe.DAY_1 -> 12
        ChartTimeframe.WEEK_1 -> 14
        ChartTimeframe.MONTH_1 -> 15
        ChartTimeframe.MONTH_3 -> 16
    }

private interface TimeframeWithLabels {
    val pointCount: Int
    fun generateLabels(count: Int): List<String>
}

private fun generateChartTrendPoints(
    basePrice: Double,
    changePct: Double,
    timeframe: ChartTimeframe,
    seed: Long
): List<TrendPoint> {
    val count = timeframe.pointCount
    val labels = when (timeframe) {
        ChartTimeframe.DAY_1 -> listOf(
            "۰۸:۰۰", "۰۹:۳۰", "۱۱:۰۰", "۱۲:۳۰", "۱۴:۰۰", "۱۵:۳۰",
            "۱۷:۰۰", "۱۸:۳۰", "۲۰:۰۰", "۲۱:۳۰", "۲۳:۰۰", "اکنون"
        )
        ChartTimeframe.WEEK_1 -> listOf(
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه",
            "جمعه", "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "دیروز", "امروز"
        )
        ChartTimeframe.MONTH_1 -> (1..count).map { dayIndex ->
            "${PersianFormatters.toPersianDigits((dayIndex * 2).toString())} روز پیش"
        }.reversed()
        ChartTimeframe.MONTH_3 -> listOf(
            "۳ ماه پیش", "۱۰ هفته پیش", "۸ هفته پیش", "۶ هفته پیش", "۴ هفته پیش",
            "۳ هفته پیش", "۲ هفته پیش", "۱ هفته پیش", "امروز"
        ).let { baseList ->
            (0 until count).map { idx ->
                val ratio = idx.toFloat() / (count - 1)
                val lookup = (ratio * (baseList.size - 1)).toInt().coerceIn(0, baseList.size - 1)
                baseList[lookup]
            }
        }
    }

    val changeFactor = changePct / 100.0
    val startPrice = if (1.0 + changeFactor > 0.05) basePrice / (1.0 + changeFactor) else basePrice * 0.98

    val points = mutableListOf<TrendPoint>()
    for (i in 0 until count) {
        val progress = i.toDouble() / (count - 1).coerceAtLeast(1)
        val linearTrend = startPrice + (basePrice - startPrice) * progress

        // Smooth continuous curvature
        val oscillation = sin(progress * Math.PI * 3.0 + (seed % 11)) * (basePrice * 0.007) +
                cos(progress * Math.PI * 5.0 + (seed % 17)) * (basePrice * 0.003)
        val finalPrice = if (i == count - 1) basePrice else (linearTrend + oscillation).coerceAtLeast(basePrice * 0.5)

        points.add(
            TrendPoint(
                timeLabel = labels.getOrElse(i) { "" },
                price = finalPrice
            )
        )
    }

    return points
}
