package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RateDecrease
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PersianFormatters
import com.example.util.TelegramSupportHelper

@Composable
fun MarketRatesHeader(
    lastUpdated: Long?,
    isLoading: Boolean,
    isOffline: Boolean,
    onRefresh: () -> Unit,
    onToggleConverter: (() -> Unit)? = null,
    isConverterVisible: Boolean = false,
    onToggleChart: (() -> Unit)? = null,
    isChartVisible: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotationTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refreshRotation"
    )
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "قیمت بازار",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        isOffline -> "آفلاین • داده ذخیره‌شده"
                        lastUpdated != null && lastUpdated > 0 ->
                            "بروزرسانی ${PersianFormatters.formatTime(lastUpdated)}"
                        else -> "در حال دریافت..."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOffline) RateDecrease else TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.testTag(if (isOffline) "offline_badge" else "header_updated_at")
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { TelegramSupportHelper.openSupport(context) },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("support_button_telegram")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "پشتیبانی",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (onToggleChart != null) {
                    IconButton(
                        onClick = onToggleChart,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("header_chart_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "نمودار",
                            tint = if (isChartVisible) GoldAccent else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (onToggleConverter != null) {
                    IconButton(
                        onClick = onToggleConverter,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("header_converter_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "محاسبه‌گر",
                            tint = if (isConverterVisible) GoldAccent else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "بروزرسانی",
                        tint = if (isLoading) GoldAccent else TextSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .then(if (isLoading) Modifier.rotate(rotation) else Modifier)
                    )
                }
            }
        }
    }
}
