package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.MarketRateEntity
import com.example.data.model.MarketCategory
import com.example.data.repository.MarketRateRepository
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
    val selectedRateId: String? = null
)

class MarketRatesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MarketRateRepository
    private val _uiState = MutableStateFlow(MarketRatesUiState(isLoading = true))
    val uiState: StateFlow<MarketRatesUiState> = _uiState.asStateFlow()

    private var previousPrices = mutableMapOf<String, Double>()
    private var autoRefreshJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MarketRateRepository(database.marketRateDao())

        // Ensure baseline catalog exists in database
        viewModelScope.launch {
            repository.ensureInitialized()
        }

        // Collect Room database rates
        viewModelScope.launch {
            repository.rates.collectLatest { rateList ->
                if (rateList.isNotEmpty()) {
                    // Update home screen widget
                    com.example.widget.MarketRatesWidgetProvider.updateAllWidgets(application)

                    // Determine price flash animations
                    val newFlashes = mutableMapOf<String, PriceFlashType>()
                    for (rate in rateList) {
                        val prev = previousPrices[rate.id]
                        if (prev != null && prev > 0.0 && prev != rate.price) {
                            if (rate.price > prev) {
                                newFlashes[rate.id] = PriceFlashType.INCREASE
                            } else {
                                newFlashes[rate.id] = PriceFlashType.DECREASE
                            }
                        }
                        previousPrices[rate.id] = rate.price
                    }

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
                        // Reset flash after 1.5 seconds
                        viewModelScope.launch {
                            delay(1500)
                            _uiState.update { it.copy(priceFlashMap = emptyMap()) }
                        }
                    }
                }
            }
        }

        // Initial fetch
        refresh()

        // Start 5-minute auto-refresh cycle
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
                // If failed, check if we have cached rates
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
                delay(5 * 60 * 1000L) // 5 minutes
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
}
