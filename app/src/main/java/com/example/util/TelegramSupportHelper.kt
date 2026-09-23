package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object TelegramSupportHelper {

    const val TELEGRAM_USERNAME = "sahandse"
    const val TELEGRAM_URL = "https://t.me/sahandse"

    fun openSupport(context: Context) {
        try {
            // First attempt to open directly in Telegram app
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=$TELEGRAM_USERNAME")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(appIntent)
        } catch (_: Exception) {
            // Fallback to web browser URL
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_URL)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "امکان باز کردن تلگرام وجود ندارد (@$TELEGRAM_USERNAME)", Toast.LENGTH_LONG).show()
            }
        }
    }
}
