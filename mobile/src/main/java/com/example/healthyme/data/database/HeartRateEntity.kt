package com.example.healthyme.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heart_rate")
data class HeartRateEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val bpm: Int,

    val timestamp: Long
)