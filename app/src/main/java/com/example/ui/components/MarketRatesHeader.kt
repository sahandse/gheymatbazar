package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.util.TelegramSupportHelper
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RateDecrease
import com.example.ui.theme.RateDecreaseBg
import com.example.ui.theme.RateIncrease
import com.example.ui.theme.RateIncreaseBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PersianFormatters

@Composable
fun MarketRatesHeader(
    lastUpdated: Long?,
    isLoading: Boolean,
    isOffline: Boolean,
    onRefresh: () -> Unit,
    onToggleConverter: (() -> Unit)? = null,
    isConverterVisible: Boolean = false,
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

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadarAlpha"
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Top status pill (Live indicator / Offline badge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOffline) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(RateDecreaseBg)
                        .border(1.dp, RateDecrease.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("offline_badge"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "آفلاین",
                        tint = RateDecrease,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "حالت آفلاین • اطلاعات ذخیره‌شده",
                        style = MaterialTheme.typography.labelSmall,
                        color = RateDecrease,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(RateIncreaseBg)
                        .border(1.dp, RateIncrease.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(RateIncrease.copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "دیتای زنده و واقعی بازار",
                        style = MaterialTheme.typography.labelSmall,
                        color = RateIncrease,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Top action buttons: Telegram Support Button (Pill), Calculator & Refresh
            val context = LocalContext.current
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Telegram Support Button Pill (@sahandse)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF142435))
                        .border(
                            1.dp,
                            Color(0xFF229ED9),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { TelegramSupportHelper.openSupport(context) }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("support_button_telegram"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "پشتیبانی تلگرام (sahandse)",
                        tint = Color(0xFF229ED9),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "پشتیبانی",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF5AC8FA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }

                if (onToggleConverter != null) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isConverterVisible) GoldAccent.copy(alpha = 0.2f) else Color(0xFF161920))
                            .border(
                                1.dp,
                                if (isConverterVisible) GoldAccent else Color(0xFF2B303C),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onToggleConverter,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("header_converter_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "محاسبه‌گر تبدیل ارز و طلا",
                                tint = if (isConverterVisible) GoldAccent else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161920))
                        .border(
                            1.dp,
                            Brush.verticalGradient(listOf(Color(0xFF2B303C), Color(0xFF1B1E26))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isLoading,
                        modifier = Modifier
                            .size(38.dp)
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

        Spacer(modifier = Modifier.height(14.dp))

        // Main Title & Timestamp
        Text(
            text = "قیمت بازار",
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            val updateText = if (lastUpdated != null && lastUpdated > 0) {
                "آخرین بروزرسانی: ${PersianFormatters.formatTime(lastUpdated)}"
            } else {
                "در حال دریافت اطلاعات زنده..."
            }
            Text(
                text = updateText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}
