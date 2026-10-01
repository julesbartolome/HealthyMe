package com.example.healthyme.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HydrationEventDao {

    @Insert
    suspend fun insertEvent(
        event: HydrationEventEntity
    ): Long

    @Query("""
        SELECT * FROM hydration_events
        WHERE synced = 0
        ORDER BY timestamp ASC
    """)
    suspend fun getUnsyncedEvents(): List<HydrationEventEntity>

    @Query("""
        UPDATE hydration_events
        SET synced = 1
        WHERE id = :eventId
    """)
    suspend fun markAsSynced(eventId: Int)

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

    @Query("""
        SELECT * FROM hydration_events
        ORDER BY timestamp DESC
        LIMIT 1
    """)
    suspend fun getLatestEvent(): HydrationEventEntity?
}