package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketCategory
import com.example.ui.MarketRatesUiState
import com.example.ui.PriceFlashType
import com.example.ui.components.ConverterBottomSheet
import com.example.ui.components.MarketRateCard
import com.example.ui.components.MarketRatesHeader
import com.example.ui.components.MarketTabs
import com.example.ui.components.MarketTrendChartCard
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.theme.LocalAppPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: MarketRatesUiState,
    providerLabel: String,
    onRefresh: () -> Unit,
    onCategorySelected: (MarketCategory) -> Unit,
    onRateClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleConverter: () -> Unit,
    onToggleChart: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onCloseConverter: () -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    onAlertsEnabledChange: (Boolean) -> Unit,
    onWidgetSlotsChange: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = uiState.isLoading && uiState.rates.isNotEmpty(),
        onRefresh = onRefresh,
        state = pullToRefreshState,
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
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
                containerColor = palette.cardSecondary,
                color = palette.accent
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
                        providerLabel = providerLabel,
                        onRefresh = onRefresh,
                        onToggleConverter = onToggleConverter,
                        isConverterVisible = uiState.showConverter,
                        onToggleChart = onToggleChart,
                        isChartVisible = uiState.showChart,
                        onOpenSettings = onOpenSettings
                    )
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "اتصال اینترنت را بررسی کنید.",
                                color = palette.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onRefresh,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.accent,
                                    contentColor = palette.background
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("retry_button")
                            ) {
                                Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("تلاش مجدد", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            uiState.rates.isEmpty() && uiState.isLoading -> {
                Box(Modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = palette.accent,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("بارگذاری نرخ‌ها...", color = palette.textSecondary)
                    }
                }
            }

            else -> {
                val query = uiState.searchQuery.trim()
                val categoryRates = uiState.rates
                    .filter { it.category == uiState.selectedCategory.code }
                    .sortedBy { it.orderIndex }
                    .ifEmpty { uiState.rates.sortedBy { it.orderIndex } }

                val filtered = if (query.isEmpty()) {
                    categoryRates
                } else {
                    categoryRates.filter {
                        it.name.contains(query, ignoreCase = true) ||
                            it.symbol.contains(query, ignoreCase = true) ||
                            it.id.contains(query, ignoreCase = true)
                    }
                }

                val favorites = filtered
                    .filter { it.id in uiState.favoriteIds }
                    .sortedBy { uiState.favoriteIds.toList().indexOf(it.id).takeIf { i -> i >= 0 } ?: 99 }
                val others = filtered.filter { it.id !in uiState.favoriteIds }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(key = "header") {
                        MarketRatesHeader(
                            lastUpdated = uiState.lastUpdated,
                            isLoading = uiState.isLoading,
                            isOffline = uiState.isOffline,
                            providerLabel = providerLabel,
                            onRefresh = onRefresh,
                            onToggleConverter = onToggleConverter,
                            isConverterVisible = uiState.showConverter,
                            onToggleChart = onToggleChart,
                            isChartVisible = uiState.showChart,
                            onOpenSettings = onOpenSettings
                        )
                    }

                    item(key = "search") {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_field"),
                            placeholder = {
                                Text("جستجوی طلا، ارز یا رمز‌ارز…", color = palette.textSecondary)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, null, tint = palette.textSecondary)
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Close, "پاک کردن", tint = palette.textSecondary)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.accent,
                                unfocusedBorderColor = palette.border,
                                focusedTextColor = palette.textPrimary,
                                unfocusedTextColor = palette.textPrimary,
                                cursorColor = palette.accent,
                                focusedContainerColor = palette.card,
                                unfocusedContainerColor = palette.card
                            )
                        )
                    }

                    item(key = "tabs") {
                        MarketTabs(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = onCategorySelected
                        )
                    }

                    if (uiState.showChart) {
                        item(key = "chart") {
                            MarketTrendChartCard(
                                category = uiState.selectedCategory,
                                rates = uiState.rates,
                                onRateClick = onRateClick,
                                activeRateId = null,
                                onClose = onToggleChart
                            )
                        }
                    }

                    if (query.isNotEmpty()) {
                        items(filtered, key = { it.id }) { rate ->
                            MarketRateCard(
                                rate = rate,
                                flashType = uiState.priceFlashMap[rate.id] ?: PriceFlashType.NONE,
                                isFavorite = rate.id in uiState.favoriteIds,
                                onClick = { onRateClick(rate.id) },
                                onToggleFavorite = { onToggleFavorite(rate.id) }
                            )
                        }
                    } else {
                        if (favorites.isNotEmpty()) {
                            item(key = "fav_header") {
                                Text(
                                    text = "علاقه‌مندی‌ها",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.textSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            items(favorites, key = { "fav_${it.id}" }) { rate ->
                                MarketRateCard(
                                    rate = rate,
                                    flashType = uiState.priceFlashMap[rate.id] ?: PriceFlashType.NONE,
                                    isFavorite = true,
                                    onClick = { onRateClick(rate.id) },
                                    onToggleFavorite = { onToggleFavorite(rate.id) }
                                )
                            }
                            if (others.isNotEmpty()) {
                                item(key = "all_header") {
                                    Text(
                                        text = "همه نرخ‌ها",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = palette.textSecondary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }

                        items(others, key = { it.id }) { rate ->
                            MarketRateCard(
                                rate = rate,
                                flashType = uiState.priceFlashMap[rate.id] ?: PriceFlashType.NONE,
                                isFavorite = rate.id in uiState.favoriteIds,
                                onClick = { onRateClick(rate.id) },
                                onToggleFavorite = { onToggleFavorite(rate.id) }
                            )
                        }
                    }

                    if (filtered.isEmpty()) {
                        item(key = "empty_search") {
                            Text(
                                text = "نتیجه‌ای پیدا نشد",
                                color = palette.textSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        if (uiState.showConverter) {
            ConverterBottomSheet(
                rates = uiState.rates,
                onDismiss = onCloseConverter
            )
        }

        if (uiState.showSettings) {
            SettingsBottomSheet(
                rates = uiState.rates,
                isDarkTheme = uiState.isDarkTheme,
                alertsEnabled = uiState.alertsEnabled,
                widgetSlot1 = uiState.widgetSlot1,
                widgetSlot2 = uiState.widgetSlot2,
                widgetSlot3 = uiState.widgetSlot3,
                widgetSlot4 = uiState.widgetSlot4,
                onDarkThemeChange = onDarkThemeChange,
                onAlertsEnabledChange = onAlertsEnabledChange,
                onWidgetSlotsChange = onWidgetSlotsChange,
                onDismiss = onCloseSettings
            )
        }
    }
}
