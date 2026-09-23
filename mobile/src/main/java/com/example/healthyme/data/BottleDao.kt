package com.example.healthyme.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BottleDao {

    @Insert
    suspend fun insertBottle(bottle: BottleEntity)

    @Query("SELECT * FROM bottles WHERE tagId = :tagId LIMIT 1")
    suspend fun getBottle(tagId: String): BottleEntity?

    @Query("SELECT * FROM bottles")
    suspend fun getAllBottles(): List<BottleEntity>
}