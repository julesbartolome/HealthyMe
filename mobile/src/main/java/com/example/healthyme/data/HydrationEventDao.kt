package com.example.healthyme.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HydrationEventDao {

    @Insert
    suspend fun insertEvent(event: HydrationEventEntity)

    @Query("""
        SELECT * FROM hydration_events
        WHERE timestamp >= :startOfDay
        AND timestamp < :endOfDay
        ORDER BY timestamp DESC
    """)
    suspend fun getEventsForDay(
        startOfDay: Long,
        endOfDay: Long
    ): List<HydrationEventEntity>

    @Query("""
        SELECT COALESCE(SUM(amountMl), 0)
        FROM hydration_events
        WHERE timestamp >= :startOfDay
        AND timestamp < :endOfDay
    """)
    suspend fun getTotalHydrationForDay(
        startOfDay: Long,
        endOfDay: Long
    ): Int
}