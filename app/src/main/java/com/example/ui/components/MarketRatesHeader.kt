package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppPalette
import com.example.util.PersianFormatters
import com.example.util.TelegramSupportHelper

@Composable
fun MarketRatesHeader(
    lastUpdated: Long?,
    isLoading: Boolean,
    isOffline: Boolean,
    providerLabel: String,
    onRefresh: () -> Unit,
    onToggleConverter: () -> Unit,
    isConverterVisible: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
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
                    color = palette.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .width(28.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(palette.accent)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        isOffline -> "آفلاین • داده ذخیره‌شده"
                        lastUpdated != null && lastUpdated > 0 ->
                            "${PersianFormatters.formatTime(lastUpdated)} • $providerLabel"
                        else -> providerLabel
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOffline) palette.decrease else palette.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.testTag(if (isOffline) "offline_badge" else "header_updated_at")
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("header_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "تنظیمات",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { TelegramSupportHelper.openSupport(context) },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("support_button_telegram")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "پشتیبانی",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onToggleConverter,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("header_converter_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "محاسبه‌گر",
                        tint = if (isConverterVisible) palette.accent else palette.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
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
                        tint = if (isLoading) palette.accent else palette.textSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .then(if (isLoading) Modifier.rotate(rotation) else Modifier)
                    )
                }
            }
        }
    }
}
