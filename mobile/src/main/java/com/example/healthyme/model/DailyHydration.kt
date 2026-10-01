package com.example.healthyme.model

import java.time.LocalDate

data class DailyHydration(
    val date: LocalDate,
    val amountMl: Int
)