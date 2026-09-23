package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.MarketRateEntity
import com.example.data.repository.MarketRateRepository
import com.example.util.PersianFormatters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MarketRatesWidgetProvider : AppWidgetProvider() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidgetFromLocalDatabase(context, appWidgetManager, widgetId)
        }

        // Trigger background refresh in parallel
        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val repository = MarketRateRepository(db.marketRateDao())
                repository.refreshRates()
                // Update all widgets with fresh data
                for (widgetId in appWidgetIds) {
                    updateWidgetFromLocalDatabase(context, appWidgetManager, widgetId)
                }
            } catch (_: Exception) {
                // Ignore network errors in background widget refresh
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MarketRatesWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            scope.launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val repository = MarketRateRepository(db.marketRateDao())
                    repository.refreshRates()
                } catch (_: Exception) {
                } finally {
                    for (widgetId in appWidgetIds) {
                        updateWidgetFromLocalDatabase(context, appWidgetManager, widgetId)
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MarketRatesWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in appWidgetIds) {
                updateWidgetFromLocalDatabase(context, appWidgetManager, widgetId)
            }
        }

        private fun updateWidgetFromLocalDatabase(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_market_rates)

            // Setup click to launch app
            val mainIntent = Intent(context, MainActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)

            // Setup refresh button click
            val refreshIntent = Intent(context, MarketRatesWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_refresh, refreshPendingIntent)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val rates: List<MarketRateEntity> = db.marketRateDao().getAllRates().firstOrNull() ?: emptyList()

                    val dollar = rates.find { it.id == "USD" }
                    val gold18k = rates.find { it.id == "GOLD_18K" }
                    val coinEmami = rates.find { it.id == "COIN_EMAMI" }
                    val usdt = rates.find { it.id == "USDT" }

                    // Dollar display
                    if (dollar != null) {
                        views.setTextViewText(
                            R.id.tv_widget_dollar_price,
                            PersianFormatters.formatPrice(dollar.price) + " ت"
                        )
                        val pct = dollar.changePercent ?: 0.0
                        views.setTextViewText(
                            R.id.tv_widget_dollar_change,
                            PersianFormatters.formatPercentage(pct)
                        )
                        views.setTextColor(
                            R.id.tv_widget_dollar_change,
                            if (pct >= 0) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
                        )
                    }

                    // Gold display
                    if (gold18k != null) {
                        views.setTextViewText(
                            R.id.tv_widget_gold_price,
                            PersianFormatters.formatPrice(gold18k.price) + " ت"
                        )
                        val pct = gold18k.changePercent ?: 0.0
                        views.setTextViewText(
                            R.id.tv_widget_gold_change,
                            PersianFormatters.formatPercentage(pct)
                        )
                        views.setTextColor(
                            R.id.tv_widget_gold_change,
                            if (pct >= 0) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
                        )
                    }

                    // Sub row: Coin & USDT
                    if (coinEmami != null) {
                        views.setTextViewText(
                            R.id.tv_widget_coin_label,
                            "🪙 سکه: " + PersianFormatters.formatPrice(coinEmami.price)
                        )
                    }
                    if (usdt != null) {
                        views.setTextViewText(
                            R.id.tv_widget_usdt_label,
                            "₮ تتر: " + PersianFormatters.formatPrice(usdt.price)
                        )
                    }

                    // Updated timestamp
                    val latestTime = (dollar?.updatedAt ?: gold18k?.updatedAt ?: 0L)
                    val timeStr = if (latestTime > 0) {
                        "ساعت " + PersianFormatters.formatTime(latestTime)
                    } else {
                        "بروزرسانی زنده"
                    }
                    views.setTextViewText(R.id.tv_widget_updated_time, timeStr)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (_: Exception) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
