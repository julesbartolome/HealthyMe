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
import com.example.healthyme.WatchDataMessenger
import com.example.healthyme.model.DailyHydration
import com.example.healthyme.model.DailySleep
import com.example.healthyme.model.DailyHeartRate
import kotlin.math.roundToInt

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

            WatchDataMessenger(context).sendSleep("--")
            WatchDataMessenger(context).sendHydration(hydrationMl)

            return HealthData(
                heartRate = HeartRateListenerService.getLatestHeartRate(context),
                sleepHours = "--",
                hydrationMl = hydrationMl
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

        WatchDataMessenger(context).sendSleep(sleepText)
        WatchDataMessenger(context).sendHydration(hydrationMl)

        return HealthData(
            heartRate = HeartRateListenerService.getLatestHeartRate(context),
            sleepHours = sleepText,
            hydrationMl = hydrationMl
        )

    }

    suspend fun getWeeklyHydration(): List<DailyHydration> {

        val zoneId =
            java.time.ZoneId.systemDefault()

        val today =
            java.time.LocalDate.now()

        val history =
            mutableListOf<DailyHydration>()

        for (daysAgo in 6 downTo 0) {

            val date =
                today.minusDays(daysAgo.toLong())

            val startOfDay =
                date
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toEpochMilli()

            val endOfDay =
                date
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toEpochMilli()

            val amountMl =
                database
                    .hydrationEventDao()
                    .getTotalHydrationForDay(
                        startOfDay,
                        endOfDay
                    )

            history.add(
                DailyHydration(
                    date = date,
                    amountMl = amountMl
                )
            )
        }

        return history
    }

    suspend fun getWeeklySleep(): List<DailySleep> {

        val zoneId =
            java.time.ZoneId.systemDefault()

        val today =
            java.time.LocalDate.now()

        val startDate =
            today.minusDays(6)

        /*
         * Read slightly more than 7 days because an overnight
         * sleep session may begin on the previous calendar day.
         */
        val queryStart =
            startDate
                .minusDays(1)
                .atStartOfDay(zoneId)
                .toInstant()

        val queryEnd =
            today
                .plusDays(1)
                .atStartOfDay(zoneId)
                .toInstant()

        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        queryStart,
                        queryEnd
                    )
                )
            )

        /*
         * Group sleep records by the local date
         * on which the sleep session ended.
         *
         * Example:
         * Sleep from 11 PM Monday → 7 AM Tuesday
         * belongs to Tuesday's sleep result.
         */
        val minutesByDate =
            response.records
                .groupBy { record ->

                    record.endTime
                        .atZone(zoneId)
                        .toLocalDate()
                }
                .mapValues { (_, records) ->

                    records.sumOf { record ->

                        Duration
                            .between(
                                record.startTime,
                                record.endTime
                            )
                            .toMinutes()
                    }
                }

        val history =
            mutableListOf<DailySleep>()

        for (daysAgo in 6 downTo 0) {

            val date =
                today.minusDays(daysAgo.toLong())

            val totalMinutes =
                minutesByDate[date] ?: 0L

            history.add(
                DailySleep(
                    date = date,
                    totalMinutes = totalMinutes
                )
            )
        }

        android.util.Log.d(
            "HealthyMe",
            "Weekly sleep loaded: ${history.size} days"
        )

        return history
    }

    suspend fun getWeeklyRestingHeartRate():
            List<DailyHeartRate> {

        val zoneId =
            java.time.ZoneId.systemDefault()

        val today =
            java.time.LocalDate.now()

        val history =
            mutableListOf<DailyHeartRate>()

        for (daysAgo in 6 downTo 0) {

            val date =
                today.minusDays(
                    daysAgo.toLong()
                )

            val startOfDay =
                date
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toEpochMilli()

            val endOfDay =
                date
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toEpochMilli()

            val average =
                database
                    .heartRateDao()
                    .getAverageHeartRateForRange(
                        startOfDay,
                        endOfDay
                    )

            history.add(
                DailyHeartRate(
                    date = date,
                    averageBpm =
                        average?.roundToInt()
                )
            )
        }

        android.util.Log.d(
            "HealthyMe",
            "Weekly resting HR loaded: ${history.size} days"
        )

        return history
    }
}

