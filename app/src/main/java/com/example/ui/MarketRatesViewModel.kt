package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.MarketRateEntity
import com.example.data.model.MarketCategory
import com.example.data.prefs.AppPreferences
import com.example.data.prefs.AppUserPrefs
import com.example.data.repository.MarketRateRepository
import com.example.util.PriceAlertNotifier
import com.example.widget.MarketRatesWidgetProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PriceFlashType {
    NONE, INCREASE, DECREASE
}

data class MarketRatesUiState(
    val rates: List<MarketRateEntity> = emptyList(),
    val selectedCategory: MarketCategory = MarketCategory.GOLD,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdated: Long? = null,
    val provider: String? = null,
    val priceFlashMap: Map<String, PriceFlashType> = emptyMap(),
    val selectedRateId: String? = null,
    val searchQuery: String = "",
    val favoriteIds: Set<String> = AppUserPrefs.DEFAULT_FAVORITES,
    val alertRateIds: Set<String> = AppUserPrefs.DEFAULT_FAVORITES,
    val alertsEnabled: Boolean = true,
    val isDarkTheme: Boolean = true,
    val compactList: Boolean = false,
    val widgetSlot1: String = "USD",
    val widgetSlot2: String = "GOLD_18K",
    val widgetSlot3: String = "COIN_EMAMI",
    val widgetSlot4: String = "USDT",
    val showConverter: Boolean = false,
    val showSettings: Boolean = false
)

class MarketRatesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MarketRateRepository
    private val preferences = AppPreferences(application)
    private val _uiState = MutableStateFlow(MarketRatesUiState(isLoading = true))
    val uiState: StateFlow<MarketRatesUiState> = _uiState.asStateFlow()

    private var previousPrices = mutableMapOf<String, Double>()
    private var autoRefreshJob: Job? = null
    private var alertsReady = false

    init {
        PriceAlertNotifier.ensureChannel(application)
        val database = AppDatabase.getDatabase(application)
        repository = MarketRateRepository(database.marketRateDao())

        viewModelScope.launch {
            repository.ensureInitialized()
        }

        viewModelScope.launch {
            preferences.prefsFlow.collectLatest { prefs ->
                _uiState.update {
                    it.copy(
                        favoriteIds = prefs.favoriteIds,
                        alertRateIds = prefs.alertRateIds,
                        alertsEnabled = prefs.alertsEnabled,
                        isDarkTheme = prefs.isDarkTheme,
                        compactList = prefs.compactList,
                        widgetSlot1 = prefs.widgetSlot1,
                        widgetSlot2 = prefs.widgetSlot2,
                        widgetSlot3 = prefs.widgetSlot3,
                        widgetSlot4 = prefs.widgetSlot4
                    )
                }
                MarketRatesWidgetProvider.updateAllWidgets(application)
            }
        }

        viewModelScope.launch {
            repository.rates.collectLatest { rateList ->
                if (rateList.isNotEmpty()) {
                    MarketRatesWidgetProvider.updateAllWidgets(application)

                    val newFlashes = mutableMapOf<String, PriceFlashType>()
                    for (rate in rateList) {
                        val prev = previousPrices[rate.id]
                        if (prev != null && prev > 0.0 && prev != rate.price) {
                            newFlashes[rate.id] =
                                if (rate.price > prev) PriceFlashType.INCREASE else PriceFlashType.DECREASE
                        }
                    }

                    val state = _uiState.value
                    if (alertsReady && state.alertsEnabled) {
                        PriceAlertNotifier.notifySignificantChanges(
                            context = application,
                            rates = rateList,
                            previousPrices = previousPrices.toMap(),
                            alertRateIds = state.alertRateIds
                        )
                    }

                    for (rate in rateList) {
                        previousPrices[rate.id] = rate.price
                    }
                    alertsReady = true

                    val latestUpdate = rateList.maxOfOrNull { it.updatedAt }
                    val currentProvider = rateList.firstOrNull { it.provider.isNotEmpty() }?.provider

                    _uiState.update { current ->
                        current.copy(
                            rates = rateList,
                            lastUpdated = latestUpdate,
                            provider = currentProvider,
                            priceFlashMap = if (newFlashes.isNotEmpty()) newFlashes else current.priceFlashMap,
                            errorMessage = null
                        )
                    }

                    if (newFlashes.isNotEmpty()) {
                        viewModelScope.launch {
                            delay(1500)
                            _uiState.update { it.copy(priceFlashMap = emptyMap()) }
                        }
                    }
                }
            }
        }

        refresh()
        startAutoRefresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.refreshRates()
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOffline = false,
                        errorMessage = null,
                        provider = result.getOrNull() ?: it.provider
                    )
                }
            } else {
                val hasCache = _uiState.value.rates.isNotEmpty()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOffline = true,
                        errorMessage = if (hasCache) null else "دریافت قیمت جدید امکان‌پذیر نیست"
                    )
                }
            }
        }
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(5 * 60 * 1000L)
                refresh()
            }
        }
    }

    fun selectCategory(category: MarketCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectRate(rateId: String?) {
        _uiState.update { it.copy(selectedRateId = rateId) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleFavorite(rateId: String) {
        viewModelScope.launch { preferences.toggleFavorite(rateId) }
    }

    fun toggleAlertRate(rateId: String) {
        viewModelScope.launch { preferences.toggleAlertRate(rateId) }
    }

    fun setAlertForRate(rateId: String, enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) preferences.setAlertsEnabled(true)
            preferences.setAlertRate(rateId, enabled)
        }
    }

    fun setAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setAlertsEnabled(enabled) }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { preferences.setDarkTheme(enabled) }
    }

    fun setCompactList(enabled: Boolean) {
        viewModelScope.launch { preferences.setCompactList(enabled) }
    }

    fun setWidgetSlots(slot1: String, slot2: String, slot3: String, slot4: String) {
        viewModelScope.launch {
            preferences.setWidgetSlots(slot1, slot2, slot3, slot4)
            MarketRatesWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun setShowConverter(show: Boolean) {
        _uiState.update { it.copy(showConverter = show) }
    }


    fun setShowSettings(show: Boolean) {
        _uiState.update { it.copy(showSettings = show) }
    }

    fun providerLabel(): String {
        return when (_uiState.value.provider?.lowercase()) {
            "muchtoman" -> "منبع: muchToman"
            "tgju" -> "منبع: TGJU"
            null, "" -> "منبع: —"
            else -> "منبع: ${_uiState.value.provider}"
        }
    }
}
