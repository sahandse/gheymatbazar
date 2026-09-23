package com.example.ui.screens

import androidx.compose.foundation.background
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
                    .padding(top = 8.dp)
                    .testTag("pull_to_refresh_indicator"),
                containerColor = CardBackground,
                color = GoldAccent
            )
        }
    ) {
        when {
            uiState.rates.isEmpty() && uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(CardBackground)
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = RateDecrease,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "دریافت قیمت‌ها ناموفق بود",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "اتصال اینترنت را بررسی کنید. اگر قبلاً نرخ دریافت شده باشد، اطلاعات ذخیره‌شده نمایش داده می‌شود.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = onRefresh,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = DarkBackground
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.size(8.dp))
                            Text("تلاش مجدد", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            uiState.rates.isEmpty() && uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = GoldAccent,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(14.dp))
                        Text("در حال دریافت قیمت‌های بازار…", color = TextSecondary)
                    }
                }
            }

            else -> {
                val displayedRates = uiState.rates
                    .filter { it.category == uiState.selectedCategory.code }
                    .sortedBy { it.orderIndex }
                    .ifEmpty { uiState.rates.sortedBy { it.orderIndex } }

                val carouselTitle = when (uiState.selectedCategory) {
                    MarketCategory.GOLD -> "منتخب طلا و سکه"
                    MarketCategory.CURRENCY -> "منتخب ارزها"
                    MarketCategory.CRYPTO -> "منتخب رمزارزها"
                }

                val carouselIcon = when (uiState.selectedCategory) {
                    MarketCategory.GOLD -> "●"
                    MarketCategory.CURRENCY -> "＄"
                    MarketCategory.CRYPTO -> "₿"
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item("header") {
                        MarketRatesHeader(
                            lastUpdated = uiState.lastUpdated,
                            isLoading = uiState.isLoading,
                            isOffline = uiState.isOffline,
                            onRefresh = onRefresh,
                            onToggleConverter = { isConverterVisible = !isConverterVisible },
                            isConverterVisible = isConverterVisible
                        )
                    }

                    item("tabs") {
                        MarketTabs(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = { category ->
                                selectedChartRateId = null
                                isChartVisible = false
                                onCategorySelected(category)
                            }
                        )
                    }

                    item("featured_${uiState.selectedCategory.code}") {
                        HorizontalRatesCarousel(
                            title = carouselTitle,
                            categoryIcon = carouselIcon,
                            rates = displayedRates.take(6),
                            selectedRateId = selectedChartRateId,
                            onRateSelected = { rateId ->
                                selectedChartRateId = rateId
                                isChartVisible = true
                            },
                            onNavigateDetail = onRateClick,
                            onShareRate = { rateToShare = it }
                        )
                    }

                    if (isChartVisible) {
                        item("chart_${selectedChartRateId ?: uiState.selectedCategory.code}") {
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
                        item("converter") {
                            MarketConverterCard(rates = uiState.rates)
                        }
                    }

                    items(displayedRates, key = { it.id }) { rate ->
                        MarketRateCard(
                            rate = rate,
                            flashType = uiState.priceFlashMap[rate.id] ?: PriceFlashType.NONE,
                            onClick = { onRateClick(rate.id) },
                            onShareClick = { rateToShare = it }
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
