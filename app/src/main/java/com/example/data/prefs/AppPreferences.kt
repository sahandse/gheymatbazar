package com.example.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "market_prefs")

data class AppUserPrefs(
    val favoriteIds: Set<String> = DEFAULT_FAVORITES,
    val alertRateIds: Set<String> = DEFAULT_FAVORITES,
    val alertsEnabled: Boolean = true,
    val isDarkTheme: Boolean = true,
    val compactList: Boolean = false,
    val widgetSlot1: String = "USD",
    val widgetSlot2: String = "GOLD_18K",
    val widgetSlot3: String = "COIN_EMAMI",
    val widgetSlot4: String = "USDT"
) {
    companion object {
        val DEFAULT_FAVORITES = setOf("USD", "GOLD_18K", "COIN_EMAMI")
    }
}

class AppPreferences(private val context: Context) {

    val prefsFlow: Flow<AppUserPrefs> = context.dataStore.data.map { prefs ->
        AppUserPrefs(
            favoriteIds = prefs[Keys.FAVORITES] ?: AppUserPrefs.DEFAULT_FAVORITES,
            alertRateIds = prefs[Keys.ALERT_RATES] ?: AppUserPrefs.DEFAULT_FAVORITES,
            alertsEnabled = prefs[Keys.ALERTS_ENABLED] ?: true,
            isDarkTheme = prefs[Keys.DARK_THEME] ?: true,
            compactList = prefs[Keys.COMPACT_LIST] ?: false,
            widgetSlot1 = prefs[Keys.WIDGET_1] ?: "USD",
            widgetSlot2 = prefs[Keys.WIDGET_2] ?: "GOLD_18K",
            widgetSlot3 = prefs[Keys.WIDGET_3] ?: "COIN_EMAMI",
            widgetSlot4 = prefs[Keys.WIDGET_4] ?: "USDT"
        )
    }

    suspend fun current(): AppUserPrefs = prefsFlow.first()

    suspend fun toggleFavorite(rateId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.toMutableSet()
                ?: AppUserPrefs.DEFAULT_FAVORITES.toMutableSet()
            if (!current.add(rateId)) current.remove(rateId)
            prefs[Keys.FAVORITES] = current
        }
    }

    suspend fun setAlertRate(rateId: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.ALERT_RATES]?.toMutableSet()
                ?: AppUserPrefs.DEFAULT_FAVORITES.toMutableSet()
            if (enabled) current.add(rateId) else current.remove(rateId)
            prefs[Keys.ALERT_RATES] = current
        }
    }

    suspend fun toggleAlertRate(rateId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.ALERT_RATES]?.toMutableSet()
                ?: AppUserPrefs.DEFAULT_FAVORITES.toMutableSet()
            if (!current.add(rateId)) current.remove(rateId)
            prefs[Keys.ALERT_RATES] = current
        }
    }

    suspend fun setAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ALERTS_ENABLED] = enabled }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setCompactList(enabled: Boolean) {
        context.dataStore.edit { it[Keys.COMPACT_LIST] = enabled }
    }

    suspend fun setWidgetSlots(slot1: String, slot2: String, slot3: String, slot4: String) {
        context.dataStore.edit {
            it[Keys.WIDGET_1] = slot1
            it[Keys.WIDGET_2] = slot2
            it[Keys.WIDGET_3] = slot3
            it[Keys.WIDGET_4] = slot4
        }
    }

    private object Keys {
        val FAVORITES = stringSetPreferencesKey("favorites")
        val ALERT_RATES = stringSetPreferencesKey("alert_rates")
        val ALERTS_ENABLED = booleanPreferencesKey("alerts_enabled")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val COMPACT_LIST = booleanPreferencesKey("compact_list")
        val WIDGET_1 = stringPreferencesKey("widget_slot_1")
        val WIDGET_2 = stringPreferencesKey("widget_slot_2")
        val WIDGET_3 = stringPreferencesKey("widget_slot_3")
        val WIDGET_4 = stringPreferencesKey("widget_slot_4")
    }
}
