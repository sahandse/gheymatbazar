package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
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
                        text = "${MarketAssetHelper.getAssetIcon(rate.id)} ${rate.symbol}",
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

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = PersianFormatters.formatPrice(rate.price),
            style = MaterialTheme.typography.displayLarge,
            color = palette.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = rate.unit, color = palette.textSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.size(12.dp))
            if (rate.changePercent != null) {
                Text(
                    text = PersianFormatters.formatPercentage(rate.changePercent),
                    color = trendColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        val diff = rate.changeAmount ?: rate.previousPrice?.let { rate.price - it }
        if (diff != null && abs(diff) > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            val sign = if (diff >= 0) "+" else "−"
            Text(
                text = "$sign${PersianFormatters.formatPrice(abs(diff))} تومان",
                color = if (diff >= 0) palette.increase else palette.decrease,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = palette.border)
        Spacer(modifier = Modifier.height(16.dp))

        if (rate.highPrice != null && rate.lowPrice != null) {
            DetailStatRow("بالاترین روز", "${PersianFormatters.formatPrice(rate.highPrice)} تومان")
            DetailStatRow("پایین‌ترین روز", "${PersianFormatters.formatPrice(rate.lowPrice)} تومان")
        }
        if (rate.previousPrice != null) {
            DetailStatRow("قیمت قبلی", "${PersianFormatters.formatPrice(rate.previousPrice)} تومان")
        }
        DetailStatRow("منبع", providerLabel.removePrefix("منبع: ").ifBlank { rate.provider })
        DetailStatRow(
            "بروزرسانی",
            if (rate.updatedAt > 0) PersianFormatters.formatFullDateTime(rate.updatedAt) else "—"
        )

        Spacer(modifier = Modifier.height(24.dp))
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

    if (showShareSheet) {
        ShareOptionsBottomSheet(
            rate = rate,
            onDismiss = { showShareSheet = false }
        )
    }
}

@Composable
private fun DetailStatRow(label: String, value: String) {
    val palette = LocalAppPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = palette.textSecondary, fontSize = 13.sp)
        Text(
            text = value,
            color = palette.textPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
    HorizontalDivider(color = palette.border.copy(alpha = 0.4f), thickness = 0.5.dp)
}
