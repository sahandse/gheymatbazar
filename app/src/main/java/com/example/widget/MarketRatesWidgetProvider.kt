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
import com.example.data.prefs.AppPreferences
import com.example.data.repository.MarketRateRepository
import com.example.util.MarketAssetHelper
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

        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val repository = MarketRateRepository(db.marketRateDao())
                repository.refreshRates()
                for (widgetId in appWidgetIds) {
                    updateWidgetFromLocalDatabase(context, appWidgetManager, widgetId)
                }
            } catch (_: Exception) {
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

            val mainIntent = Intent(context, MainActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)

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
                    val rates: List<MarketRateEntity> =
                        db.marketRateDao().getAllRates().firstOrNull() ?: emptyList()
                    val prefs = AppPreferences(context).current()

                    bindPrimarySlot(
                        views,
                        rates.find { it.id == prefs.widgetSlot1 },
                        prefs.widgetSlot1,
                        R.id.tv_widget_slot1_icon,
                        R.id.tv_widget_slot1_name,
                        R.id.tv_widget_slot1_price,
                        R.id.tv_widget_slot1_change
                    )
                    bindPrimarySlot(
                        views,
                        rates.find { it.id == prefs.widgetSlot2 },
                        prefs.widgetSlot2,
                        R.id.tv_widget_slot2_icon,
                        R.id.tv_widget_slot2_name,
                        R.id.tv_widget_slot2_price,
                        R.id.tv_widget_slot2_change
                    )

                    val slot3 = rates.find { it.id == prefs.widgetSlot3 }
                    val slot4 = rates.find { it.id == prefs.widgetSlot4 }
                    views.setTextViewText(
                        R.id.tv_widget_slot3_label,
                        formatSubLabel(slot3, prefs.widgetSlot3)
                    )
                    views.setTextViewText(
                        R.id.tv_widget_slot4_label,
                        formatSubLabel(slot4, prefs.widgetSlot4)
                    )

                    val latestTime = listOfNotNull(slot3?.updatedAt, slot4?.updatedAt, rates.maxOfOrNull { it.updatedAt })
                        .maxOrNull() ?: 0L
                    views.setTextViewText(
                        R.id.tv_widget_updated_time,
                        if (latestTime > 0) "ساعت ${PersianFormatters.formatTime(latestTime)}" else "—"
                    )

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (_: Exception) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        private fun bindPrimarySlot(
            views: RemoteViews,
            rate: MarketRateEntity?,
            fallbackId: String,
            iconId: Int,
            nameId: Int,
            priceId: Int,
            changeId: Int
        ) {
            views.setTextViewText(iconId, MarketAssetHelper.getAssetIcon(fallbackId))
            views.setTextViewText(nameId, rate?.name ?: fallbackId)
            if (rate != null) {
                views.setTextViewText(priceId, PersianFormatters.formatPrice(rate.price) + " ت")
                val pct = rate.changePercent ?: 0.0
                views.setTextViewText(changeId, PersianFormatters.formatPercentage(pct))
                views.setTextColor(
                    changeId,
                    if (pct >= 0) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
                )
            } else {
                views.setTextViewText(priceId, "---")
                views.setTextViewText(changeId, "—")
            }
        }

        private fun formatSubLabel(rate: MarketRateEntity?, fallbackId: String): String {
            val icon = MarketAssetHelper.getAssetIcon(fallbackId)
            val name = rate?.name ?: fallbackId
            val price = rate?.let { PersianFormatters.formatPrice(it.price) } ?: "---"
            return "$icon $name: $price"
        }
    }
}
