package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
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
import com.example.ui.components.ShareOptionsBottomSheet
import com.example.ui.theme.LocalAppPalette
import com.example.util.MarketAssetHelper
import com.example.util.PersianFormatters
import kotlin.math.abs

@Composable
fun DetailScreen(
    rate: MarketRateEntity,
    isFavorite: Boolean,
    isAlertEnabled: Boolean,
    providerLabel: String,
    onBackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val scrollState = rememberScrollState()
    var showShareSheet by remember { mutableStateOf(false) }
    val isPositive = (rate.changePercent ?: 0.0) >= 0.0
    val trendColor = if (isPositive) palette.increase else palette.decrease
    val rangeProgress by animateFloatAsState(
        targetValue = if (rate.highPrice != null && rate.lowPrice != null && rate.highPrice > rate.lowPrice) {
            ((rate.price - rate.lowPrice) / (rate.highPrice - rate.lowPrice)).toFloat().coerceIn(0f, 1f)
        } else {
            0.5f
        },
        animationSpec = tween(600),
        label = "rangeProgress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.accent.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.12f),
                    radius = size.width * 0.7f
                ),
                center = Offset(size.width * 0.2f, size.height * 0.12f),
                radius = size.width * 0.7f
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
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = palette.textPrimary
                        )
                    }
                    Column {
                        Text(
                            text = rate.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = rate.symbol,
                            color = palette.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.testTag("detail_favorite")) {
                        Icon(
                            if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "علاقه‌مندی",
                            tint = if (isFavorite) palette.accent else palette.textSecondary
                        )
                    }
                    IconButton(onClick = onToggleAlert, modifier = Modifier.testTag("detail_alert")) {
                        Icon(
                            if (isAlertEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsNone,
                            contentDescription = "اعلان",
                            tint = if (isAlertEnabled) palette.accent else palette.textSecondary
                        )
                    }
                    IconButton(
                        onClick = { showShareSheet = true },
                        modifier = Modifier.testTag("detail_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "اشتراک", tint = palette.textSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(palette.card, palette.cardSecondary)
                        )
                    )
                    .border(1.dp, palette.border, RoundedCornerShape(24.dp))
                    .padding(22.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(palette.accent.copy(alpha = 0.14f))
                            .border(1.dp, palette.accent.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = MarketAssetHelper.getAssetIcon(rate.id),
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "قیمت لحظه‌ای",
                            color = palette.textSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (rate.changePercent != null) {
                            Text(
                                text = PersianFormatters.formatPercentage(rate.changePercent),
                                color = trendColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = PersianFormatters.formatPrice(rate.price),
                    style = MaterialTheme.typography.displayLarge,
                    color = palette.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp
                )
                Text(
                    text = rate.unit,
                    color = palette.textSecondary,
                    fontSize = 14.sp
                )

                val diff = rate.changeAmount ?: rate.previousPrice?.let { rate.price - it }
                if (diff != null && abs(diff) > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val sign = if (diff >= 0) "+" else "−"
                    Text(
                        text = "$sign${PersianFormatters.formatPrice(abs(diff))} تومان",
                        color = if (diff >= 0) palette.increase else palette.decrease,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (rate.highPrice != null && rate.lowPrice != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("بازه روز", color = palette.textSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(palette.border)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(rangeProgress)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(palette.decrease.copy(alpha = 0.7f), palette.accent, palette.increase)
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            PersianFormatters.formatPrice(rate.lowPrice),
                            color = palette.textSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            PersianFormatters.formatPrice(rate.highPrice),
                            color = palette.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DetailMetaCard("منبع", providerLabel.removePrefix("منبع: ").ifBlank { rate.provider })
            DetailMetaCard(
                "بروزرسانی",
                if (rate.updatedAt > 0) PersianFormatters.formatFullDateTime(rate.updatedAt) else "—"
            )
            if (rate.previousPrice != null) {
                DetailMetaCard(
                    "قیمت قبلی",
                    "${PersianFormatters.formatPrice(rate.previousPrice)} تومان"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isAlertEnabled) {
                    "اعلان نوسان این نرخ فعال است"
                } else {
                    "برای دریافت اعلان، زنگوله را فعال کنید"
                },
                color = palette.textSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showShareSheet) {
        ShareOptionsBottomSheet(
            rate = rate,
            onDismiss = { showShareSheet = false }
        )
    }
}

@Composable
private fun DetailMetaCard(label: String, value: String) {
    val palette = LocalAppPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.card)
            .border(1.dp, palette.border.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = palette.textSecondary, fontSize = 13.sp)
        Text(
            text = value,
            color = palette.textPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}
