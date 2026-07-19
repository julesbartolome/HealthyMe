package com.example.healthyme.data.repository

import com.example.healthyme.model.HealthData

class HealthRepository {

    fun getTodayHealthData(): HealthData {

        return HealthData(
            heartRate = 78,
            sleepHours = 7.5,
            hydration = 1.2,
            steps = 6542,
            lifestyleScore = 84
        )

    }
}