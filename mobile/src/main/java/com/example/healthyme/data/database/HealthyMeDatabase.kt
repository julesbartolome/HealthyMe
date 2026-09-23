package com.example.healthyme.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.healthyme.data.BottleDao
import com.example.healthyme.data.BottleEntity
import com.example.healthyme.data.HydrationEventDao
import com.example.healthyme.data.HydrationEventEntity

@Database(
    entities = [
        HeartRateEntity::class,
        BottleEntity::class,
        HydrationEventEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HealthyMeDatabase : RoomDatabase() {

    abstract fun heartRateDao(): HeartRateDao

    abstract fun bottleDao(): BottleDao

    abstract fun hydrationEventDao(): HydrationEventDao

    companion object {

        @Volatile
        private var INSTANCE: HealthyMeDatabase? = null

        fun getDatabase(
            context: Context
        ): HealthyMeDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthyMeDatabase::class.java,
                    "healthyme_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}