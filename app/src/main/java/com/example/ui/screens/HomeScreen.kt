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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.MarketCategory
import com.example.ui.MarketRatesUiState
import com.example.ui.PriceFlashType
import com.example.ui.components.MarketConverterCard
import com.example.ui.components.MarketRateCard
import com.example.ui.components.MarketRatesHeader
import com.example.ui.components.MarketTabs
import com.example.ui.components.MarketTrendChartCard
import com.example.ui.theme.CardSecondary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GoldAccent
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
        when {
            uiState.rates.isEmpty() && uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    MarketRatesHeader(
                        lastUpdated = uiState.lastUpdated,
                        isLoading = uiState.isLoading,
                        isOffline = uiState.isOffline,
                        onRefresh = onRefresh
                    )
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "اتصال اینترنت را بررسی کنید.",
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
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("retry_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
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

            uiState.rates.isEmpty() && uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = GoldAccent,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "بارگذاری نرخ‌ها...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            else -> {
                val displayedRates = uiState.rates
                    .filter { it.category == uiState.selectedCategory.code }
                    .sortedBy { it.orderIndex }
                    .ifEmpty { uiState.rates.sortedBy { it.orderIndex } }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "header") {
                        MarketRatesHeader(
                            lastUpdated = uiState.lastUpdated,
                            isLoading = uiState.isLoading,
                            isOffline = uiState.isOffline,
                            onRefresh = onRefresh,
                            onToggleConverter = { isConverterVisible = !isConverterVisible },
                            isConverterVisible = isConverterVisible,
                            onToggleChart = { isChartVisible = !isChartVisible },
                            isChartVisible = isChartVisible
                        )
                    }

                    item(key = "tabs") {
                        MarketTabs(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = { cat ->
                                isChartVisible = false
                                onCategorySelected(cat)
                            }
                        )
                    }

                    if (isChartVisible) {
                        item(key = "chart_${uiState.selectedCategory.code}") {
                            MarketTrendChartCard(
                                category = uiState.selectedCategory,
                                rates = uiState.rates,
                                onRateClick = onRateClick,
                                activeRateId = null,
                                onClose = { isChartVisible = false }
                            )
                        }
                    }

                    if (isConverterVisible) {
                        item(key = "market_converter_card") {
                            MarketConverterCard(rates = uiState.rates)
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
                            onClick = { onRateClick(rate.id) }
                        )
                    }
                }
            }
        }
    }
}
