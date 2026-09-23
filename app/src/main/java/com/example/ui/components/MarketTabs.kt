package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketCategory
import com.example.ui.theme.CardBorder
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MarketTabs(
    selectedCategory: MarketCategory,
    onCategorySelected: (MarketCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("market_category_tabs")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MarketTabItem(
                title = "طلا",
                isSelected = selectedCategory == MarketCategory.GOLD,
                onClick = { onCategorySelected(MarketCategory.GOLD) },
                modifier = Modifier.weight(1f),
                testTag = "tab_gold"
            )
            MarketTabItem(
                title = "ارز",
                isSelected = selectedCategory == MarketCategory.CURRENCY,
                onClick = { onCategorySelected(MarketCategory.CURRENCY) },
                modifier = Modifier.weight(1f),
                testTag = "tab_currency"
            )
            MarketTabItem(
                title = "کریپتو",
                isSelected = selectedCategory == MarketCategory.CRYPTO,
                onClick = { onCategorySelected(MarketCategory.CRYPTO) },
                modifier = Modifier.weight(1f),
                testTag = "tab_crypto"
            )
        }
        HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), thickness = 1.dp)
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
    val textColor by animateColorAsState(
        targetValue = if (isSelected) TextPrimary else TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "tabText"
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (isSelected) GoldAccent else CardBorder.copy(alpha = 0f),
        animationSpec = tween(durationMillis = 200),
        label = "tabIndicator"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldAccent.copy(alpha = 0.15f)),
                onClick = onClick
            )
            .testTag(testTag)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 10.dp)
        )
        HorizontalDivider(color = indicatorColor, thickness = 2.dp)
    }
}
