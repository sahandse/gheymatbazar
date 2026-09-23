package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
        } else 0.5f,
        animationSpec = tween(500),
        label = "rangeProgress"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.testTag("detail_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت", tint = palette.textPrimary)
            }
            Row {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.testTag("detail_favorite")) {
                    Icon(
                        if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        "علاقه‌مندی",
                        tint = if (isFavorite) palette.accent else palette.textSecondary
                    )
                }
                IconButton(onClick = onToggleAlert, modifier = Modifier.testTag("detail_alert")) {
                    Icon(
                        if (isAlertEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsNone,
                        "اعلان",
                        tint = if (isAlertEnabled) palette.accent else palette.textSecondary
                    )
                }
                IconButton(onClick = { showShareSheet = true }, modifier = Modifier.testTag("detail_share_button")) {
                    Icon(Icons.Default.Share, "اشتراک", tint = palette.textSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(palette.card)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(palette.cardSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(MarketAssetHelper.getAssetIcon(rate.id), fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = rate.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(rate.symbol, color = palette.textSecondary, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = PersianFormatters.formatPrice(rate.price),
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                letterSpacing = (-0.7).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(rate.unit, color = palette.textSecondary, fontSize = 12.sp)

            val diff = rate.changeAmount ?: rate.previousPrice?.let { rate.price - it }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rate.changePercent?.let {
                    Text(
                        text = PersianFormatters.formatPercentage(it),
                        color = trendColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                if (diff != null && abs(diff) > 0) {
                    val sign = if (diff >= 0) "+" else "−"
                    Text(
                        text = "$sign${PersianFormatters.formatPrice(abs(diff))}",
                        color = trendColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (rate.highPrice != null && rate.lowPrice != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailStatCard(
                    title = "کمترین امروز",
                    value = PersianFormatters.formatPrice(rate.lowPrice),
                    modifier = Modifier.weight(1f)
                )
                DetailStatCard(
                    title = "بیشترین امروز",
                    value = PersianFormatters.formatPrice(rate.highPrice),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.card)
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("موقعیت قیمت امروز", color = palette.textSecondary, fontSize = 12.sp)
                    Text("روز", color = palette.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.cardSecondary)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(rangeProgress)
                            .height(5.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(trendColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(palette.card)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (rate.updatedAt > 0) {
                DetailMetaRow("آخرین بروزرسانی", PersianFormatters.formatFullDateTime(rate.updatedAt))
            }
            rate.previousPrice?.let {
                DetailMetaRow("قیمت قبلی", "${PersianFormatters.formatPrice(it)} ${rate.unit}")
            }
            DetailMetaRow(
                "هشدار قیمت",
                if (isAlertEnabled) "فعال" else "غیرفعال"
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }

    if (showShareSheet) {
        ShareOptionsBottomSheet(rate = rate, onDismiss = { showShareSheet = false })
    }
}

@Composable
private fun DetailStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    val palette = LocalAppPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(palette.card)
            .padding(14.dp)
    ) {
        Text(title, color = palette.textSecondary, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(7.dp))
        Text(value, color = palette.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailMetaRow(label: String, value: String) {
    val palette = LocalAppPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = palette.textSecondary, fontSize = 12.sp)
        Text(text = value, color = palette.textPrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
    }
}
