package com.example.healthyme.model

import java.time.LocalDate

data class DailySleep(
    val date: LocalDate,
    val totalMinutes: Long
)