package com.example.data.remote

import com.example.data.model.MarketCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class RemoteItemData(
    val id: String,
    val name: String,
    val symbol: String,
    val category: String,
    val price: Double,
    val changePct: Double? = null,
    val changeAmount: Double? = null,
    val highPrice: Double? = null,
    val lowPrice: Double? = null,
    val unit: String = "تومان",
    val orderIndex: Int = 0
)

data class FetchedRatesResult(
    val items: List<RemoteItemData>,
    val provider: String,
    val updatedAt: Long
)

class MarketRateRemoteDataSource(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
) {

    suspend fun fetchFromMuchToman(): FetchedRatesResult = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://rates.muchtoman.com/rates")
            .header("User-Agent", "GheymatBazar/1.3.2 (Android)")
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("muchToman HTTP ${response.code}")

            val body = response.body?.string() ?: throw IOException("Empty body from muchToman")
            val json = JSONObject(body)
            val toman = json.optJSONObject("toman") ?: throw IOException("Invalid muchToman payload")
            val updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
            val items = mutableListOf<RemoteItemData>()

            fun addItem(id: String, key: String, name: String, symbol: String, category: String, order: Int) {
                val price = toman.optDouble(key, 0.0)
                if (price.isFinite() && price > 0.0) {
                    items += RemoteItemData(
                        id = id,
                        name = name,
                        symbol = symbol,
                        category = category,
                        price = price,
                        orderIndex = order
                    )
                }
            }

            addItem("GOLD_18K", "gold18", "طلای ۱۸ عیار", "GOLD 18K", MarketCategory.GOLD.code, 1)
            addItem("COIN_EMAMI", "coin_emami", "سکه امامی", "COIN EMAMI", MarketCategory.GOLD.code, 2)
            addItem("COIN_BAHAR", "coin_bahar", "سکه بهار آزادی", "COIN BAHAR", MarketCategory.GOLD.code, 3)
            addItem("COIN_NIM", "coin_nim", "نیم سکه بهار آزادی", "HALF COIN", MarketCategory.GOLD.code, 4)
            addItem("COIN_ROB", "coin_rob", "ربع سکه بهار آزادی", "QUARTER COIN", MarketCategory.GOLD.code, 5)
            addItem("COIN_GERAMI", "coin_gerami", "سکه گرمی", "GERAMI COIN", MarketCategory.GOLD.code, 6)
            addItem("GOLD_MESGHAL", "gold_mesghal", "مثقال طلا", "MESGHAL", MarketCategory.GOLD.code, 7)

            addItem("USD", "usd", "دلار آمریکا", "USD", MarketCategory.CURRENCY.code, 1)
            addItem("EUR", "eur", "یورو اروپا", "EUR", MarketCategory.CURRENCY.code, 2)
            addItem("AED", "aed", "درهم امارات", "AED", MarketCategory.CURRENCY.code, 3)
            addItem("GBP", "gbp", "پوند انگلیس", "GBP", MarketCategory.CURRENCY.code, 4)
            addItem("TRY", "try", "لیر ترکیه", "TRY", MarketCategory.CURRENCY.code, 5)
            addItem("CAD", "cad", "دلار کانادا", "CAD", MarketCategory.CURRENCY.code, 6)

            addItem("USDT", "usdt", "تتر", "USDT", MarketCategory.CRYPTO.code, 1)
            addItem("BTC", "btc", "بیت‌کوین", "BTC", MarketCategory.CRYPTO.code, 2)
            addItem("ETH", "eth", "اتریوم", "ETH", MarketCategory.CRYPTO.code, 3)
            addItem("SOL", "sol", "سولانا", "SOL", MarketCategory.CRYPTO.code, 4)
            addItem("BNB", "bnb", "بایننس کوین", "BNB", MarketCategory.CRYPTO.code, 5)
            addItem("DOGE", "doge", "دوج‌کوین", "DOGE", MarketCategory.CRYPTO.code, 6)

            if (items.size < 3) throw IOException("muchToman returned insufficient live rates")

            FetchedRatesResult(
                items = items,
                provider = "muchToman",
                updatedAt = if (updatedAt > 0) updatedAt else System.currentTimeMillis()
            )
        }
    }

    suspend fun fetchFromTgju(): FetchedRatesResult = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://call.tgju.org/ajax.json")
            .header("User-Agent", "GheymatBazar/1.3.2 (Android)")
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("TGJU HTTP ${response.code}")

            val body = response.body?.string() ?: throw IOException("Empty body from TGJU")
            val json = JSONObject(body)
            val current = json.optJSONObject("current") ?: throw IOException("Invalid TGJU payload")
            val items = mutableListOf<RemoteItemData>()

            fun parseTgjuItem(
                id: String,
                key: String,
                name: String,
                symbol: String,
                category: String,
                order: Int,
                divisor: Double = 10.0
            ) {
                val obj = current.optJSONObject(key) ?: return
                val rawPrice = obj.optString("p", "").replace(",", "").toDoubleOrNull() ?: return
                val price = rawPrice / divisor
                if (!price.isFinite() || price <= 0.0) return

                val dp = obj.optDouble("dp", 0.0)
                val direction = obj.optString("dt", "")
                val changePct = if (direction == "low") -kotlin.math.abs(dp) else dp
                val diff = obj.optString("d", "").replace(",", "").toDoubleOrNull()?.div(divisor)
                val high = obj.optString("h", "").replace(",", "").toDoubleOrNull()?.div(divisor)
                val low = obj.optString("l", "").replace(",", "").toDoubleOrNull()?.div(divisor)

                items += RemoteItemData(
                    id = id,
                    name = name,
                    symbol = symbol,
                    category = category,
                    price = price,
                    changePct = changePct,
                    changeAmount = diff?.let { if (direction == "low") -kotlin.math.abs(it) else it },
                    highPrice = high,
                    lowPrice = low,
                    orderIndex = order
                )
            }

            parseTgjuItem("GOLD_18K", "geram18", "طلای ۱۸ عیار", "GOLD 18K", MarketCategory.GOLD.code, 1)
            parseTgjuItem("COIN_EMAMI", "sekee", "سکه امامی", "COIN EMAMI", MarketCategory.GOLD.code, 2)
            parseTgjuItem("COIN_BAHAR", "sekeb", "سکه بهار آزادی", "COIN BAHAR", MarketCategory.GOLD.code, 3)
            parseTgjuItem("COIN_NIM", "nim", "نیم سکه بهار آزادی", "HALF COIN", MarketCategory.GOLD.code, 4)
            parseTgjuItem("COIN_ROB", "rob", "ربع سکه بهار آزادی", "QUARTER COIN", MarketCategory.GOLD.code, 5)
            parseTgjuItem("COIN_GERAMI", "gerami", "سکه گرمی", "GERAMI COIN", MarketCategory.GOLD.code, 6)
            parseTgjuItem("GOLD_MESGHAL", "mesghal", "مثقال طلا", "MESGHAL", MarketCategory.GOLD.code, 7)

            parseTgjuItem("USD", "price_dollar_rl", "دلار آمریکا", "USD", MarketCategory.CURRENCY.code, 1)
            parseTgjuItem("EUR", "price_eur", "یورو اروپا", "EUR", MarketCategory.CURRENCY.code, 2)
            parseTgjuItem("AED", "price_aed", "درهم امارات", "AED", MarketCategory.CURRENCY.code, 3)
            parseTgjuItem("GBP", "price_gbp", "پوند انگلیس", "GBP", MarketCategory.CURRENCY.code, 4)
            parseTgjuItem("TRY", "price_try", "لیر ترکیه", "TRY", MarketCategory.CURRENCY.code, 5)
            parseTgjuItem("CAD", "price_cad", "دلار کانادا", "CAD", MarketCategory.CURRENCY.code, 6)

            parseTgjuItem("USDT", "usdt-irr", "تتر", "USDT", MarketCategory.CRYPTO.code, 1)
            parseTgjuItem("BTC", "crypto-bitcoin-irr", "بیت‌کوین", "BTC", MarketCategory.CRYPTO.code, 2)
            parseTgjuItem("ETH", "crypto-ethereum-irr", "اتریوم", "ETH", MarketCategory.CRYPTO.code, 3)

            if (items.size < 3) throw IOException("TGJU returned insufficient live rates")

            FetchedRatesResult(
                items = items,
                provider = "TGJU",
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
