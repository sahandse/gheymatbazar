package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_rates")
data class MarketRateEntity(
    @PrimaryKey
    val id: String, // "USD", "GOLD_18K", "EMAMI_COIN"
    val name: String,
    val symbol: String,
    val price: Double,
    val previousPrice: Double? = null,
    val changePercent: Double? = null,
    val changeAmount: Double? = null,
    val highPrice: Double? = null,
    val lowPrice: Double? = null,
    val category: String = "GOLD",
    val provider: String,
    val updatedAt: Long,
    val unit: String = "تومان",
    val orderIndex: Int = 0
)
