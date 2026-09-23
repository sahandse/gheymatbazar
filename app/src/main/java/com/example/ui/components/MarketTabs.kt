package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketCategory
import com.example.ui.theme.LocalAppPalette

@Composable
fun MarketTabs(
    selectedCategory: MarketCategory,
    onCategorySelected: (MarketCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.card)
            .border(1.dp, palette.border.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(4.dp)
            .testTag("market_category_tabs"),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MarketTabItem(
            title = "🪙 طلا",
            isSelected = selectedCategory == MarketCategory.GOLD,
            onClick = { onCategorySelected(MarketCategory.GOLD) },
            modifier = Modifier.weight(1f),
            testTag = "tab_gold"
        )
        MarketTabItem(
            title = "💵 ارز",
            isSelected = selectedCategory == MarketCategory.CURRENCY,
            onClick = { onCategorySelected(MarketCategory.CURRENCY) },
            modifier = Modifier.weight(1f),
            testTag = "tab_currency"
        )
        MarketTabItem(
            title = "⚡ کریپتو",
            isSelected = selectedCategory == MarketCategory.CRYPTO,
            onClick = { onCategorySelected(MarketCategory.CRYPTO) },
            modifier = Modifier.weight(1f),
            testTag = "tab_crypto"
        )
    }
}

@Composable
private fun MarketTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val palette = LocalAppPalette.current
    val bg by animateColorAsState(
        targetValue = if (isSelected) palette.accent else palette.card.copy(alpha = 0f),
        animationSpec = tween(220),
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) palette.background else palette.textSecondary,
        animationSpec = tween(220),
        label = "tabText"
    )

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    Brush.verticalGradient(
                        listOf(palette.accent, palette.accent.copy(alpha = 0.85f))
                    )
                } else {
                    Brush.verticalGradient(listOf(bg, bg))
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = palette.accent.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
