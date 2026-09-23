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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketCategory
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MarketTabs(
    selectedCategory: MarketCategory,
    onCategorySelected: (MarketCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF13151B))
            .border(
                1.dp,
                Brush.verticalGradient(listOf(Color(0xFF262B36), Color(0xFF181B22))),
                RoundedCornerShape(20.dp)
            )
            .padding(5.dp)
            .testTag("market_category_tabs")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarketTabItem(
                title = "طلا و سکه",
                icon = Icons.Default.Savings,
                isSelected = selectedCategory == MarketCategory.GOLD,
                onClick = { onCategorySelected(MarketCategory.GOLD) },
                modifier = Modifier.weight(1f),
                testTag = "tab_gold"
            )

            Spacer(modifier = Modifier.width(4.dp))

            MarketTabItem(
                title = "دلار و ارزها",
                icon = Icons.Default.AccountBalance,
                isSelected = selectedCategory == MarketCategory.CURRENCY,
                onClick = { onCategorySelected(MarketCategory.CURRENCY) },
                modifier = Modifier.weight(1f),
                testTag = "tab_currency"
            )

            Spacer(modifier = Modifier.width(4.dp))

            MarketTabItem(
                title = "ارز دیجیتال",
                icon = Icons.Default.CurrencyBitcoin,
                isSelected = selectedCategory == MarketCategory.CRYPTO,
                onClick = { onCategorySelected(MarketCategory.CRYPTO) },
                modifier = Modifier.weight(1f),
                testTag = "tab_crypto"
            )
        }
    }
}

@Composable
private fun MarketTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) GoldAccent else Color.Transparent,
        animationSpec = tween(durationMillis = 250),
        label = "tabBg"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = if (isSelected) DarkBackground else TextSecondary,
        animationSpec = tween(durationMillis = 250),
        label = "tabText"
    )

    val animatedIconColor by animateColorAsState(
        targetValue = if (isSelected) DarkBackground else TextSecondary,
        animationSpec = tween(durationMillis = 250),
        label = "tabIcon"
    )

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(animatedBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldAccent.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = animatedIconColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = animatedTextColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.5.sp
            )
        }
    }
}
