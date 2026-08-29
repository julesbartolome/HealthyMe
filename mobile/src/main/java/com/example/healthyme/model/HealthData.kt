package com.example.healthyme.model

data class HealthData(
    val heartRate: Int = 0,
    val sleepHours: String = "--",
    val hydrationMl: Int = 0,
    val steps: Int = 0
)