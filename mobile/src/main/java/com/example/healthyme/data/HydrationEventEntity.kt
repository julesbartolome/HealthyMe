package com.example.healthyme.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_events")
data class HydrationEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tagId: String,
    val amountMl: Int,
    val timestamp: Long
)