package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
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
    val infiniteTransition = rememberInfiniteTransition(label = "headerMotion")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refreshRotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "livePulse"
    )
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    palette.accent.copy(alpha = 0.35f),
                                    palette.cardSecondary
                                )
                            )
                        )
                        .border(
                            1.dp,
                            palette.accent.copy(alpha = 0.35f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("◈", color = palette.accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "قیمت بازار",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isOffline) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(palette.increase.copy(alpha = pulse))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
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
                            modifier = Modifier.testTag(
                                if (isOffline) "offline_badge" else "header_updated_at"
                            )
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HeaderActionChip(
                    onClick = onOpenSettings,
                    active = false,
                    testTag = "header_settings_button"
                ) {
                    Icon(Icons.Default.Settings, "تنظیمات", tint = palette.textSecondary, modifier = Modifier.size(17.dp))
                }
                HeaderActionChip(
                    onClick = { TelegramSupportHelper.openSupport(context) },
                    active = false,
                    testTag = "support_button_telegram"
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        "پشتیبانی",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                HeaderActionChip(
                    onClick = onToggleConverter,
                    active = isConverterVisible,
                    testTag = "header_converter_toggle_button"
                ) {
                    Icon(
                        Icons.Default.Calculate,
                        "محاسبه‌گر",
                        tint = if (isConverterVisible) palette.accent else palette.textSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                HeaderActionChip(
                    onClick = onRefresh,
                    active = isLoading,
                    enabled = !isLoading,
                    testTag = "refresh_button"
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        "بروزرسانی",
                        tint = if (isLoading) palette.accent else palette.textSecondary,
                        modifier = Modifier
                            .size(17.dp)
                            .then(if (isLoading) Modifier.rotate(rotation) else Modifier)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderActionChip(
    onClick: () -> Unit,
    active: Boolean,
    testTag: String,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val palette = LocalAppPalette.current
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
                if (active) palette.accent.copy(alpha = 0.16f) else palette.card
            )
            .border(
                1.dp,
                if (active) palette.accent.copy(alpha = 0.45f) else palette.border,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(38.dp)
                .testTag(testTag)
        ) {
            content()
        }
    }
}
