package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MarketRateEntity
import com.example.data.model.MarketCategory
import com.example.ui.MarketRatesUiState
import com.example.ui.PriceFlashType
import com.example.ui.components.HorizontalRatesCarousel
import com.example.ui.components.MarketConverterCard
import com.example.ui.components.MarketRateCard
import com.example.ui.components.MarketRatesHeader
import com.example.ui.components.MarketTabs
import com.example.ui.components.MarketTrendChartCard
import com.example.ui.components.ShareOptionsBottomSheet
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSecondary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RateDecrease
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: MarketRatesUiState,
    onRefresh: () -> Unit,
    onCategorySelected: (MarketCategory) -> Unit,
    onRateClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()
    // Calculator/converter and chart stay off until the user opts in
    var isConverterVisible by remember { mutableStateOf(false) }
    var rateToShare by remember { mutableStateOf<MarketRateEntity?>(null) }
    var selectedChartRateId by remember { mutableStateOf<String?>(null) }
    var isChartVisible by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading && uiState.rates.isNotEmpty(),
        onRefresh = onRefresh,
        state = pullToRefreshState,
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = uiState.isLoading && uiState.rates.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .testTag("pull_to_refresh_indicator"),
                containerColor = CardSecondary,
                color = GoldAccent
            )
        }
    ) {
        // Ambient luxury glow in top corner
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(GoldAccent.copy(alpha = 0.05f), androidx.compose.ui.graphics.Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.12f),
                        radius = size.width * 0.65f
                    ),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.12f),
                    radius = size.width * 0.65f
                )
            }
        }

        when {
            // Empty cache + Error state
            uiState.rates.isEmpty() && uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    MarketRatesHeader(
                        lastUpdated = uiState.lastUpdated,
                        isLoading = uiState.isLoading,
                        isOffline = uiState.isOffline,
                        onRefresh = onRefresh
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(CardBackground)
                                .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                                .padding(28.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(DarkBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = RateDecrease,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "لطفاً اتصال اینترنت خود را بررسی نمایید.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onRefresh,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldAccent,
                                    contentColor = DarkBackground
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("retry_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "تلاش مجدد",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Initial loading state before cache or network
            uiState.rates.isEmpty() && uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = GoldAccent,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "در حال بارگذاری نرخ‌ها...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Active list with 3 Tabs & filtered items
            else -> {
                val displayedRates = uiState.rates
                    .filter { it.category == uiState.selectedCategory.code }
                    .sortedBy { it.orderIndex }
                    .ifEmpty {
                        // If selected category has no items yet, fallback to all rates
                        uiState.rates.sortedBy { it.orderIndex }
                    }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(key = "header") {
                        MarketRatesHeader(
                            lastUpdated = uiState.lastUpdated,
                            isLoading = uiState.isLoading,
                            isOffline = uiState.isOffline,
                            onRefresh = onRefresh,
                            onToggleConverter = { isConverterVisible = !isConverterVisible },
                            isConverterVisible = isConverterVisible
                        )
                    }

                    item(key = "tabs") {
                        MarketTabs(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = { cat ->
                                selectedChartRateId = null
                                isChartVisible = false
                                onCategorySelected(cat)
                            }
                        )
                    }

                    // Horizontal Rates Carousel for the selected category (Gold, Currencies, Cryptos)
                    val (carouselTitle, carouselIcon) = when (uiState.selectedCategory) {
                        MarketCategory.GOLD -> "طلا و انواع سکه (بهار آزادی، امامی، گرمی، پارسیان)" to "🪙"
                        MarketCategory.CURRENCY -> "دلار و تمامی ارزهای جهان (پوند، یورو، درهم، لیر...)" to "💵"
                        MarketCategory.CRYPTO -> "رمزارزها و ارزهای دیجیتال برتر (بیت‌کوین، اتریوم...)" to "⚡"
                    }

                    item(key = "horizontal_rates_${uiState.selectedCategory.code}") {
                        HorizontalRatesCarousel(
                            title = carouselTitle,
                            categoryIcon = carouselIcon,
                            rates = displayedRates,
                            selectedRateId = selectedChartRateId,
                            onRateSelected = { rateId ->
                                selectedChartRateId = rateId
                                isChartVisible = true
                            },
                            onNavigateDetail = onRateClick,
                            onShareRate = { r -> rateToShare = r }
                        )
                    }

                    // Interactive trend chart (opens on clicking any horizontal rate card or tab)
                    if (isChartVisible) {
                        item(key = "chart_${uiState.selectedCategory.code}") {
                            MarketTrendChartCard(
                                category = uiState.selectedCategory,
                                rates = uiState.rates,
                                onRateClick = onRateClick,
                                activeRateId = selectedChartRateId,
                                onClose = { isChartVisible = false }
                            )
                        }
                    }

                    if (isConverterVisible) {
                        item(key = "market_converter_card") {
                            MarketConverterCard(
                                rates = uiState.rates
                            )
                        }
                    }

                    items(
                        items = displayedRates,
                        key = { it.id }
                    ) { rate ->
                        val flashType = uiState.priceFlashMap[rate.id] ?: PriceFlashType.NONE
                        MarketRateCard(
                            rate = rate,
                            flashType = flashType,
                            onClick = { onRateClick(rate.id) },
                            onShareClick = { r -> rateToShare = r }
                        )
                    }
                }
            }
        }

        rateToShare?.let { rate ->
            ShareOptionsBottomSheet(
                rate = rate,
                onDismiss = { rateToShare = null }
            )
        }
    }
}
