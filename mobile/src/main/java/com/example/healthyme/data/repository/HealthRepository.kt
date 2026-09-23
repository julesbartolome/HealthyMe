package com.example.healthyme.data.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.healthyme.model.HealthData
import java.time.Duration
import java.time.Instant
import com.example.healthyme.HeartRateListenerService
import com.example.healthyme.data.database.HealthyMeDatabase

class HealthRepository(private val context: Context) {

    private val healthConnectClient =
        HealthConnectClient.getOrCreate(context)

    private val database =
        HealthyMeDatabase.getDatabase(context)

    suspend fun getTodayHealthData(): HealthData {

        val end = Instant.now()

        val startOfDay = java.time.LocalDate
            .now()
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()

        val endOfDay = startOfDay
            .plus(Duration.ofDays(1))

        val hydrationMl =
            database.hydrationEventDao()
                .getTotalHydrationForDay(
                    startOfDay.toEpochMilli(),
                    endOfDay.toEpochMilli()
                )

        android.util.Log.d(
            "HealthyMe",
            "Today's hydration: $hydrationMl ml"
        )

        // Look back 7 days so we can find the latest completed sleep.
        val start = end.minus(Duration.ofDays(7))

        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )

        android.util.Log.d(
            "HealthyMe",
            "Sleep records found: ${response.records.size}"
        )

        if (response.records.isEmpty()) {

            android.util.Log.d(
                "HealthyMe",
                "No sleep records found."
            )

            return HealthData(
                heartRate = HeartRateListenerService.getLatestHeartRate(context),
                sleepHours = "--",
                hydrationMl = hydrationMl,
                steps = 6542
            )
        }

        // Sort records from newest to oldest.
        val sortedRecords = response.records
            .sortedByDescending { it.endTime }

        // The newest sleep record tells us which night we want.
        val latestRecord = sortedRecords.first()

        android.util.Log.d(
            "HealthyMe",
            "Latest sleep record: " +
                    "${latestRecord.startTime} -> ${latestRecord.endTime}"
        )

        /*
         * Sleep can be split into multiple records.
         *
         * Example:
         *
         * 15:00 -> 23:00  = 8 hours
         * 23:00 -> 00:00  = 1 hour
         *
         * Together:
         *
         * 9 hours
         *
         * We therefore include records that overlap the latest
         * sleep period or are directly connected to it.
         */

        val latestEnd = latestRecord.endTime

        // A sleep night can reasonably start up to 18 hours
        // before the latest record ends.
        val nightStart = latestEnd.minus(Duration.ofHours(18))

        val latestNightRecords = response.records.filter { record ->

            record.endTime.isAfter(nightStart) &&
                    record.startTime.isBefore(latestEnd)
        }

        android.util.Log.d(
            "HealthyMe",
            "Records belonging to latest night: " +
                    latestNightRecords.size
        )

        var totalMinutes = 0L

        for (record in latestNightRecords) {

            val minutes = Duration
                .between(record.startTime, record.endTime)
                .toMinutes()

            android.util.Log.d(
                "HealthyMe",
                "Latest night record: " +
                        "${record.startTime} -> ${record.endTime} " +
                        "($minutes minutes)"
            )

            totalMinutes += minutes
        }

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        val sleepText = if (totalMinutes > 0) {
            String.format("%d:%02d", hours, minutes)
        } else {
            "--"
        }

        android.util.Log.d(
            "HealthyMe",
            "Latest night's total sleep: $sleepText"
        )

        return HealthData(
            heartRate = HeartRateListenerService.getLatestHeartRate(context),
            sleepHours = sleepText,
            hydrationMl = hydrationMl,
            steps = 6542
        )

    }
}

