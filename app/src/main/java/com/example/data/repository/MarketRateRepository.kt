package com.example.data.repository

import com.example.data.local.MarketRateDao
import com.example.data.local.MarketRateEntity
import com.example.data.remote.MarketRateRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MarketRateRepository(
    private val dao: MarketRateDao,
    private val remoteDataSource: MarketRateRemoteDataSource = MarketRateRemoteDataSource()
) {

    val rates: Flow<List<MarketRateEntity>> = dao.getAllRates()

    suspend fun getRateById(id: String): MarketRateEntity? = dao.getRateById(id)

    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        val existing = runCatching { dao.getAllRates().first() }.getOrDefault(emptyList())
        val existingIds = existing.map { it.id }.toSet()
        val missingItems = com.example.data.local.DefaultMarketRates.items.filter { it.id !in existingIds }
        if (missingItems.isNotEmpty()) {
            dao.insertRates(missingItems)
        }
    }

    /**
     * Attempts to refresh rates from Primary (muchToman) first,
     * then Fallback (TGJU).
     * If successful, updates local Room database cache with full category support.
     */
    suspend fun refreshRates(): Result<String> = withContext(Dispatchers.IO) {
        val result = runCatching {
            remoteDataSource.fetchFromMuchToman()
        }.recoverCatching {
            // Fallback to TGJU
            remoteDataSource.fetchFromTgju()
        }

        if (result.isFailure) {
            return@withContext Result.failure(result.exceptionOrNull() ?: Exception("Network error"))
        }

        val fetched = result.getOrThrow()

        fun calculateChange(newPrice: Double, oldEntity: MarketRateEntity?, directChangePct: Double?): Pair<Double?, Double?> {
            if (directChangePct != null && directChangePct != 0.0) {
                val prev = if (oldEntity != null && oldEntity.price != newPrice) oldEntity.price else oldEntity?.previousPrice
                return Pair(prev, directChangePct)
            }
            if (oldEntity != null && oldEntity.price > 0) {
                if (oldEntity.price != newPrice) {
                    val pct = ((newPrice - oldEntity.price) / oldEntity.price) * 100.0
                    return Pair(oldEntity.price, pct)
                } else {
                    return Pair(oldEntity.previousPrice, oldEntity.changePercent)
                }
            }
            return Pair(null, null)
        }

        val entities = fetched.items.map { item ->
            val oldEntity = dao.getRateById(item.id)
            val (prevPrice, changePct) = calculateChange(item.price, oldEntity, item.changePct)
            val diffAmount = item.changeAmount ?: (prevPrice?.let { item.price - it })

            MarketRateEntity(
                id = item.id,
                name = item.name,
                symbol = item.symbol,
                price = item.price,
                previousPrice = prevPrice,
                changePercent = changePct,
                changeAmount = diffAmount,
                highPrice = item.highPrice ?: oldEntity?.highPrice,
                lowPrice = item.lowPrice ?: oldEntity?.lowPrice,
                category = item.category,
                provider = fetched.provider,
                updatedAt = fetched.updatedAt,
                unit = item.unit,
                orderIndex = item.orderIndex
            )
        }

        dao.insertRates(entities)
        Result.success(fetched.provider)
    }
}
