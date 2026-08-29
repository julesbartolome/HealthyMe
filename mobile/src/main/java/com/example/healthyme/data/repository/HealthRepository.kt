package com.example.healthyme.data.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.healthyme.model.HealthData
import java.time.Duration
import java.time.Instant

class HealthRepository(
    private val context: Context
) {

    private val healthConnectClient =
        HealthConnectClient.getOrCreate(context)

    suspend fun getTodayHealthData(): HealthData {

        val end = Instant.now()

        // Look back 24 hours
        val start = end.minus(Duration.ofDays(1))

        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )

        var totalMinutes = 0L

        for (record in response.records) {
            totalMinutes += Duration
                .between(record.startTime, record.endTime)
                .toMinutes()
        }

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        val sleepText = if (totalMinutes > 0) {
            String.format("%d:%02d", hours, minutes)
        } else {
            "--"
        }

        return HealthData(
            heartRate = 78,
            sleepHours = sleepText,
            hydrationMl = 1200,
            steps = 6542
        )
    }
}