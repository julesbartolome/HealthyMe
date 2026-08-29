package com.example.healthyme.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        HeartRateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HealthyMeDatabase : RoomDatabase() {

    abstract fun heartRateDao(): HeartRateDao

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
                ).build()

                INSTANCE = instance

                instance
            }
        }
    }
}