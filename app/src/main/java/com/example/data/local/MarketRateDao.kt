package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketRateDao {
    @Query("SELECT * FROM market_rates ORDER BY orderIndex ASC")
    fun getAllRates(): Flow<List<MarketRateEntity>>

    @Query("SELECT * FROM market_rates WHERE id = :id LIMIT 1")
    suspend fun getRateById(id: String): MarketRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<MarketRateEntity>)
}
