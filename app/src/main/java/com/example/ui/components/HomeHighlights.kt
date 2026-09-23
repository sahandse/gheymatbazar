package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MarketRateEntity
import com.example.ui.theme.LocalAppPalette
import com.example.util.MarketAssetHelper
import com.example.util.PersianFormatters

@Composable
fun PrimaryMarketHighlights(
    rates: List<MarketRateEntity>,
    onRateClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val preferred = listOf("USD", "GOLD_18K", "COIN_EMAMI")
    val selected = preferred.mapNotNull { id -> rates.firstOrNull { it.id == id } }
    if (selected.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "نبض بازار",
            color = LocalAppPalette.current.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selected.forEach { rate ->
                HighlightCard(
                    rate = rate,
                    onClick = { onRateClick(rate.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HighlightCard(
    rate: MarketRateEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val change = rate.changePercent
    val changeColor = when {
        change == null || change == 0.0 -> palette.textSecondary
        change > 0 -> palette.increase
        else -> palette.decrease
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(palette.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(palette.cardSecondary),
                contentAlignment = Alignment.Center
            ) {
                Text(MarketAssetHelper.getAssetIcon(rate.id), fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = rate.name,
                color = palette.textSecondary,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = PersianFormatters.formatPrice(rate.price),
            color = palette.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = change?.let { PersianFormatters.formatPercentage(it) } ?: rate.unit,
            color = changeColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun FavoritesStrip(
    rates: List<MarketRateEntity>,
    onRateClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (rates.isEmpty()) return
    val palette = LocalAppPalette.current
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "علاقه‌مندی‌ها",
            color = palette.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rates.forEach { rate ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.card)
                        .clickable { onRateClick(rate.id) }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(MarketAssetHelper.getAssetIcon(rate.id), fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(7.dp))
                    Column {
                        Text(
                            text = rate.name,
                            color = palette.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = PersianFormatters.formatPrice(rate.price),
                            color = palette.textSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
