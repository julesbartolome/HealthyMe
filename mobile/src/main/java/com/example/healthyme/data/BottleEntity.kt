package com.example.healthyme.data

import androidx.room.Entity

@Entity(tableName = "bottles")
data class BottleEntity(
    @androidx.room.PrimaryKey
    val tagId: String,
    val name: String,
    val capacityMl: Int,
    val amountPerScanMl: Int
)