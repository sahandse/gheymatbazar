package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppPalette
import com.example.util.PersianFormatters

@Composable
fun MarketRatesHeader(
    lastUpdated: Long?,
    isLoading: Boolean,
    isOffline: Boolean,
    onRefresh: () -> Unit,
    onToggleConverter: () -> Unit,
    isConverterVisible: Boolean,
    onOpenSettings: () -> Unit,
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "قیمت بازار",
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isOffline) palette.decrease else palette.increase)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = when {
                        isOffline && lastUpdated != null && lastUpdated > 0 ->
                            "آخرین داده ذخیره‌شده · ${PersianFormatters.formatTime(lastUpdated)}"
                        isOffline -> "آفلاین"
                        lastUpdated != null && lastUpdated > 0 ->
                            "زنده · ${PersianFormatters.formatTime(lastUpdated)}"
                        else -> "زنده"
                    },
                    color = if (isOffline) palette.textSecondary else palette.textSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.testTag(if (isOffline) "offline_badge" else "header_updated_at")
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(40.dp).testTag("header_search_button")
            ) {
                Icon(Icons.Default.Search, "جستجو", tint = palette.textSecondary, modifier = Modifier.size(19.dp))
            }
            IconButton(
                onClick = onToggleConverter,
                modifier = Modifier.size(40.dp).testTag("header_converter_toggle_button")
            ) {
                Icon(
                    Icons.Default.Calculate,
                    "محاسبه‌گر",
                    tint = if (isConverterVisible) palette.accent else palette.textSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(40.dp).testTag("header_settings_button")
            ) {
                Icon(Icons.Default.Settings, "تنظیمات", tint = palette.textSecondary, modifier = Modifier.size(19.dp))
            }
        }
    }
}
