package com.example.healthyme.model

import java.time.LocalDate

data class DailyHeartRate(
    val date: LocalDate,
    val averageBpm: Int?
)