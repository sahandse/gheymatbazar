package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RateDecrease
import com.example.ui.theme.RateDecreaseBg
import com.example.ui.theme.RateIncrease
import com.example.ui.theme.RateIncreaseBg
import com.example.util.PersianFormatters

/**
 * 24-Hour Market Trend Status Indicator Badge
 * Displays whether the price has risen (green) or fallen (red) over the past 24 hours,
 * accompanied by directional icons, status text (صعودی / نزولی), and exact percentage.
 */
@Composable
fun PriceTrend24hBadge(
    changePercent: Double?,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false,
    showTimeframeTag: Boolean = true
) {
    val pct = changePercent ?: 0.0
    val isPositive = pct > 0.0001
    val isNegative = pct < -0.0001
    val isNeutral = !isPositive && !isNegative

    val tintColor = when {
        isPositive -> RateIncrease
        isNegative -> RateDecrease
        else -> Color(0xFF9EAABF)
    }

    val bgColor = when {
        isPositive -> RateIncreaseBg
        isNegative -> RateDecreaseBg
        else -> Color(0xFF1B202A)
    }

    val statusTitle = when {
        isPositive -> "صعودی"
        isNegative -> "نزولی"
        else -> "بدون تغییر"
    }

    val iconVector = when {
        isPositive -> Icons.AutoMirrored.Filled.TrendingUp
        isNegative -> Icons.AutoMirrored.Filled.TrendingDown
        else -> Icons.AutoMirrored.Filled.TrendingFlat
    }

    val horizontalPadding = if (isLarge) 12.dp else 8.dp
    val verticalPadding = if (isLarge) 6.dp else 4.dp
    val iconSize = if (isLarge) 18.dp else 14.dp
    val fontSize = if (isLarge) 13.sp else 11.5.sp

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, tintColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .testTag("price_trend_24h_badge"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Trend directional icon
        Icon(
            imageVector = iconVector,
            contentDescription = statusTitle,
            tint = tintColor,
            modifier = Modifier.size(iconSize)
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Status text: صعودی / نزولی
        Text(
            text = statusTitle,
            style = MaterialTheme.typography.labelSmall,
            color = tintColor,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Exact percentage
        Text(
            text = PersianFormatters.formatPercentage(pct),
            style = MaterialTheme.typography.labelSmall,
            color = tintColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize
        )

        if (showTimeframeTag) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(tintColor.copy(alpha = 0.18f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "۲۴س",
                    style = MaterialTheme.typography.labelSmall,
                    color = tintColor,
                    fontSize = if (isLarge) 10.sp else 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
