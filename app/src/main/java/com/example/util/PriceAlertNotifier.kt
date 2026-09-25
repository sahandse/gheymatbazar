package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.MarketRateEntity
import kotlin.math.abs

object PriceAlertNotifier {

    private const val CHANNEL_ID = "price_alerts"
    private const val CHANNEL_NAME = "اعلان تغییر قیمت"
    private const val MIN_CHANGE_PERCENT = 0.8

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "اطلاع از نوسان قیمت‌های منتخب"
        }
        manager.createNotificationChannel(channel)
    }

    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun notifySignificantChanges(
        context: Context,
        rates: List<MarketRateEntity>,
        previousPrices: Map<String, Double>,
        alertRateIds: Set<String>
    ) {
        if (!canPost(context) || alertRateIds.isEmpty()) return
        ensureChannel(context)

        for (rate in rates) {
            if (rate.id !in alertRateIds) continue
            val prev = previousPrices[rate.id] ?: continue
            if (prev <= 0.0 || rate.price == prev) continue
            val changePct = ((rate.price - prev) / prev) * 100.0
            if (abs(changePct) < MIN_CHANGE_PERCENT) continue

            val direction = if (changePct > 0) "افزایش" else "کاهش"
            val title = "${rate.name}: $direction قیمت"
            val body =
                "${PersianFormatters.formatPrice(rate.price)} ${rate.unit} (${PersianFormatters.formatPercentage(changePct)})"

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_RATE_ID, rate.id)
            }
            val pending = PendingIntent.getActivity(
                context,
                rate.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_brand_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            NotificationManagerCompat.from(context)
                .notify(rate.id.hashCode(), notification)
        }
    }

    const val EXTRA_OPEN_RATE_ID = "open_rate_id"
}
