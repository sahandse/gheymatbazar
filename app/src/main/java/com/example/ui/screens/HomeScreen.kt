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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.ui.components.FavoritesStrip
import com.example.ui.components.MarketRateCard
import com.example.ui.components.MarketRatesHeader
import com.example.ui.components.MarketTabs
import com.example.ui.components.PrimaryMarketHighlights
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.theme.LocalAppPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: MarketRatesUiState,
    onRefresh: () -> Unit,
    onCategorySelected: (MarketCategory) -> Unit,
    onRateClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleConverter: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onCloseConverter: () -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    onCompactListChange: (Boolean) -> Unit,
    onAlertsEnabledChange: (Boolean) -> Unit,
    onWidgetSlotsChange: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val pullToRefreshState = rememberPullToRefreshState()
    var showSearch by remember { mutableStateOf(uiState.searchQuery.isNotEmpty()) }

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
                    .padding(top = 8.dp)
                    .testTag("pull_to_refresh_indicator"),
                containerColor = palette.card,
                color = palette.accent
            )
        }
    ) {
        when {
            uiState.rates.isEmpty() && uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    MarketRatesHeader(
                        lastUpdated = uiState.lastUpdated,
                        isLoading = uiState.isLoading,
                        isOffline = uiState.isOffline,
                        onRefresh = onRefresh,
                        onToggleConverter = onToggleConverter,
                        isConverterVisible = uiState.showConverter,
                        onOpenSettings = onOpenSettings,
                        onSearchClick = { showSearch = !showSearch }
                    )
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("اتصال اینترنت را بررسی کنید.", color = palette.textSecondary)
                            Spacer(modifier = Modifier.height(18.dp))
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
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = palette.accent, strokeWidth = 2.dp, modifier = Modifier.size(30.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("در حال دریافت بازار…", color = palette.textSecondary)
                }
            }

            else -> {
                val query = uiState.searchQuery.trim()
                val allSorted = uiState.rates.sortedBy { it.orderIndex }
                val categoryRates = allSorted
                    .filter { it.category == uiState.selectedCategory.code }
                    .ifEmpty { allSorted }
                val filtered = if (query.isEmpty()) categoryRates else categoryRates.filter {
                    it.name.contains(query, true) || it.symbol.contains(query, true) || it.id.contains(query, true)
                }
                val favoritesAll = allSorted.filter { it.id in uiState.favoriteIds }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(if (uiState.compactList) 7.dp else 9.dp)
                ) {
                    item(key = "header") {
                        MarketRatesHeader(
                            lastUpdated = uiState.lastUpdated,
                            isLoading = uiState.isLoading,
                            isOffline = uiState.isOffline,
                            onRefresh = onRefresh,
                            onToggleConverter = onToggleConverter,
                            isConverterVisible = uiState.showConverter,
                            onOpenSettings = onOpenSettings,
                            onSearchClick = {
                                showSearch = !showSearch
                                if (!showSearch) onSearchQueryChange("")
                            }
                        )
                    }

                    item(key = "highlights") {
                        PrimaryMarketHighlights(
                            rates = uiState.rates,
                            onRateClick = onRateClick,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (favoritesAll.isNotEmpty() && query.isEmpty()) {
                        item(key = "favorites_strip") {
                            FavoritesStrip(
                                rates = favoritesAll,
                                onRateClick = onRateClick,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    if (showSearch) {
                        item(key = "search") {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = onSearchQueryChange,
                                modifier = Modifier.fillMaxWidth().testTag("search_field"),
                                placeholder = { Text("جستجوی نرخ…", color = palette.textSecondary.copy(alpha = 0.7f)) },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = palette.textSecondary) },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        onSearchQueryChange("")
                                        showSearch = false
                                    }) {
                                        Icon(Icons.Default.Close, "بستن", tint = palette.textSecondary)
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = palette.accent.copy(alpha = 0.35f),
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = palette.textPrimary,
                                    unfocusedTextColor = palette.textPrimary,
                                    cursorColor = palette.accent,
                                    focusedContainerColor = palette.card,
                                    unfocusedContainerColor = palette.card
                                )
                            )
                        }
                    }

                    item(key = "tabs") {
                        MarketTabs(selectedCategory = uiState.selectedCategory, onCategorySelected = onCategorySelected)
                    }

                    item(key = "section_label") {
                        Text(
                            text = when (uiState.selectedCategory) {
                                MarketCategory.GOLD -> "طلا و سکه"
                                MarketCategory.CURRENCY -> "ارزها"
                                MarketCategory.CRYPTO -> "رمزارزها"
                            },
                            color = palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }

                    items(filtered, key = { it.id }) { rate ->
                        MarketRateCard(
                            rate = rate,
                            flashType = uiState.priceFlashMap[rate.id] ?: PriceFlashType.NONE,
                            isFavorite = rate.id in uiState.favoriteIds,
                            onClick = { onRateClick(rate.id) },
                            onToggleFavorite = { onToggleFavorite(rate.id) },
                            compact = uiState.compactList
                        )
                    }

                    if (filtered.isEmpty()) {
                        item(key = "empty_search") {
                            Text(
                                text = "نتیجه‌ای پیدا نشد",
                                color = palette.textSecondary,
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        if (uiState.showConverter) {
            ConverterBottomSheet(rates = uiState.rates, onDismiss = onCloseConverter)
        }

        if (uiState.showSettings) {
            SettingsBottomSheet(
                rates = uiState.rates,
                isDarkTheme = uiState.isDarkTheme,
                compactList = uiState.compactList,
                alertsEnabled = uiState.alertsEnabled,
                widgetSlot1 = uiState.widgetSlot1,
                widgetSlot2 = uiState.widgetSlot2,
                widgetSlot3 = uiState.widgetSlot3,
                widgetSlot4 = uiState.widgetSlot4,
                onDarkThemeChange = onDarkThemeChange,
                onCompactListChange = onCompactListChange,
                onAlertsEnabledChange = onAlertsEnabledChange,
                onWidgetSlotsChange = onWidgetSlotsChange,
                onDismiss = onCloseSettings
            )
        }
    }
}
