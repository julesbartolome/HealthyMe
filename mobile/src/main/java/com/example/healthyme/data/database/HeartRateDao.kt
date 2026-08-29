package com.example.healthyme.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateDao {

    @Insert
    suspend fun insertHeartRate(
        heartRate: HeartRateEntity
    )

    @Query(
        "SELECT * FROM heart_rate ORDER BY timestamp DESC"
    )
    fun getAllHeartRates(): Flow<List<HeartRateEntity>>

    @Query(
        "SELECT * FROM heart_rate ORDER BY timestamp DESC LIMIT 1"
    )
    fun getLatestHeartRate(): Flow<HeartRateEntity?>
}