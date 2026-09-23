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

    /**
     * Primary source: muchToman (https://rates.muchtoman.com/rates)
     */
    suspend fun fetchFromMuchToman(): FetchedRatesResult = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://rates.muchtoman.com/rates")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) MarketRates/1.0")
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("muchToman HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty body from muchToman")
            val json = JSONObject(body)
            val toman = json.getJSONObject("toman")
            val updatedAt = json.optLong("updatedAt", System.currentTimeMillis())

            val items = mutableListOf<RemoteItemData>()

            // 1. GOLD & COINS (قیمت طلا)
            fun addGold(id: String, key: String, name: String, symbol: String, order: Int) {
                if (toman.has(key)) {
                    val price = toman.optDouble(key, 0.0)
                    if (price > 0) {
                        items.add(
                            RemoteItemData(
                                id = id,
                                name = name,
                                symbol = symbol,
                                category = MarketCategory.GOLD.code,
                                price = price,
                                orderIndex = order
                            )
                        )
                    }
                }
            }

            addGold("GOLD_18K", "gold18", "طلای ۱۸ عیار", "GOLD 18K", 1)
            addGold("COIN_EMAMI", "coin_emami", "سکه امامی", "COIN EMAMI", 2)
            addGold("COIN_BAHAR", "coin_bahar", "سکه بهار آزادی", "COIN BAHAR", 3)
            addGold("COIN_NIM", "coin_nim", "نیم سکه بهار آزادی", "HALF COIN", 4)
            addGold("COIN_ROB", "coin_rob", "ربع سکه بهار آزادی", "QUARTER COIN", 5)
            addGold("COIN_GERAMI", "coin_gerami", "سکه گرمی", "GERAMI COIN", 6)
            addGold("GOLD_MESGHAL", "gold_mesghal", "مثقال طلا", "MESGHAL", 7)

            // 2. CURRENCIES (قیمت دلار و ارزها)
            fun addCurrency(id: String, key: String, name: String, symbol: String, order: Int) {
                if (toman.has(key)) {
                    val price = toman.optDouble(key, 0.0)
                    if (price > 0) {
                        items.add(
                            RemoteItemData(
                                id = id,
                                name = name,
                                symbol = symbol,
                                category = MarketCategory.CURRENCY.code,
                                price = price,
                                orderIndex = order
                            )
                        )
                    }
                }
            }

            addCurrency("USD", "usd", "دلار آمریکا", "USD", 1)
            addCurrency("EUR", "eur", "یورو اروپا", "EUR", 2)
            addCurrency("AED", "aed", "درهم امارات", "AED", 3)
            addCurrency("GBP", "gbp", "پوند انگلیس", "GBP", 4)
            addCurrency("TRY", "try", "لیر ترکیه", "TRY", 5)
            addCurrency("CAD", "cad", "دلار کانادا", "CAD", 6)

            // 3. CRYPTOCURRENCIES (ارز دیجیتال)
            fun addCrypto(id: String, key: String, name: String, symbol: String, order: Int) {
                if (toman.has(key)) {
                    val price = toman.optDouble(key, 0.0)
                    if (price > 0) {
                        items.add(
                            RemoteItemData(
                                id = id,
                                name = name,
                                symbol = symbol,
                                category = MarketCategory.CRYPTO.code,
                                price = price,
                                orderIndex = order
                            )
                        )
                    }
                }
            }

            addCrypto("USDT", "usdt", "تتر", "USDT", 1)
            addCrypto("BTC", "btc", "بیت‌کوین", "BTC", 2)
            addCrypto("ETH", "eth", "اتریوم", "ETH", 3)
            addCrypto("SOL", "sol", "سولانا", "SOL", 4)
            addCrypto("BNB", "bnb", "بایننس کوین", "BNB", 5)
            addCrypto("DOGE", "doge", "دوج‌کوین", "DOGE", 6)

            FetchedRatesResult(
                items = items,
                provider = "muchToman",
                updatedAt = if (updatedAt > 0) updatedAt else System.currentTimeMillis()
            )
        }
    }

    /**
     * Fallback source: TGJU (https://call.tgju.org/ajax.json)
     */
    suspend fun fetchFromTgju(): FetchedRatesResult = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://call.tgju.org/ajax.json")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) MarketRates/1.0")
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("TGJU HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty body from TGJU")
            val json = JSONObject(body)
            val current = json.getJSONObject("current")

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
                if (current.has(key)) {
                    val obj = current.optJSONObject(key) ?: return
                    val pStr = obj.optString("p", "").replace(",", "")
                    val priceRial = pStr.toDoubleOrNull() ?: return
                    val price = priceRial / divisor

                    val dp = obj.optDouble("dp", 0.0)
                    val dt = obj.optString("dt", "")
                    val changePct = if (dt == "low") -Math.abs(dp) else dp
                    val diffRial = obj.optString("d", "").replace(",", "").toDoubleOrNull()
                    val changeAmount = diffRial?.let { if (dt == "low") -Math.abs(it / divisor) else (it / divisor) }
                    val highRial = obj.optString("h", "").replace(",", "").toDoubleOrNull()
                    val highPrice = highRial?.let { it / divisor }
                    val lowRial = obj.optString("l", "").replace(",", "").toDoubleOrNull()
                    val lowPrice = lowRial?.let { it / divisor }

                    items.add(
                        RemoteItemData(
                            id = id,
                            name = name,
                            symbol = symbol,
                            category = category,
                            price = price,
                            changePct = changePct,
                            changeAmount = changeAmount,
                            highPrice = highPrice,
                            lowPrice = lowPrice,
                            orderIndex = order
                        )
                    )
                }
            }

            // Gold
            parseTgjuItem("GOLD_18K", "geram18", "طلای ۱۸ عیار", "GOLD 18K", MarketCategory.GOLD.code, 1)
            parseTgjuItem("COIN_EMAMI", "sekee", "سکه امامی", "COIN EMAMI", MarketCategory.GOLD.code, 2)
            parseTgjuItem("COIN_BAHAR", "sekeb", "سکه بهار آزادی", "COIN BAHAR", MarketCategory.GOLD.code, 3)
            parseTgjuItem("COIN_NIM", "nim", "نیم سکه بهار آزادی", "HALF COIN", MarketCategory.GOLD.code, 4)
            parseTgjuItem("COIN_ROB", "rob", "ربع سکه بهار آزادی", "QUARTER COIN", MarketCategory.GOLD.code, 5)
            parseTgjuItem("COIN_GERAMI", "gerami", "سکه گرمی", "GERAMI COIN", MarketCategory.GOLD.code, 6)
            parseTgjuItem("GOLD_MESGHAL", "mesghal", "مثقال طلا", "MESGHAL", MarketCategory.GOLD.code, 7)

            // Currencies
            parseTgjuItem("USD", "price_dollar_rl", "دلار آمریکا", "USD", MarketCategory.CURRENCY.code, 1)
            parseTgjuItem("EUR", "price_eur", "یورو اروپا", "EUR", MarketCategory.CURRENCY.code, 2)
            parseTgjuItem("AED", "price_aed", "درهم امارات", "AED", MarketCategory.CURRENCY.code, 3)
            parseTgjuItem("GBP", "price_gbp", "پوند انگلیس", "GBP", MarketCategory.CURRENCY.code, 4)
            parseTgjuItem("TRY", "price_try", "لیر ترکیه", "TRY", MarketCategory.CURRENCY.code, 5)
            parseTgjuItem("CAD", "price_cad", "دلار کانادا", "CAD", MarketCategory.CURRENCY.code, 6)

            // Crypto
            parseTgjuItem("USDT", "usdt-irr", "تتر", "USDT", MarketCategory.CRYPTO.code, 1)
            parseTgjuItem("BTC", "crypto-bitcoin-irr", "بیت‌کوین", "BTC", MarketCategory.CRYPTO.code, 2)
            parseTgjuItem("ETH", "crypto-ethereum-irr", "اتریوم", "ETH", MarketCategory.CRYPTO.code, 3)

            FetchedRatesResult(
                items = items,
                provider = "TGJU",
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
