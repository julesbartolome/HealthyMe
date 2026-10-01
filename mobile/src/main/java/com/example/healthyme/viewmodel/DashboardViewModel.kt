package com.example.healthyme.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthyme.data.repository.HealthRepository
import com.example.healthyme.model.HealthData
import kotlinx.coroutines.launch
import com.example.healthyme.model.DailyHydration
import com.example.healthyme.model.DailySleep
import android.content.SharedPreferences
import com.example.healthyme.domain.ActivityContext
import com.example.healthyme.model.DailyHeartRate

class DashboardViewModel : ViewModel() {

    private lateinit var repository: HealthRepository
    private lateinit var preferences: SharedPreferences

    var activityContext by mutableStateOf(ActivityContext.RESTING)
        private set

    var healthData by mutableStateOf(HealthData())
        private set

    var weeklyHydration by mutableStateOf<List<DailyHydration>>(emptyList())
        private set

    var weeklySleep by mutableStateOf<List<DailySleep>>(emptyList())
        private set
    var weeklyHeartRate by
    mutableStateOf<List<DailyHeartRate>>(
        emptyList()
    )
        private set

    fun initialize(context: Context) {

        if (::repository.isInitialized) return

        repository =
            HealthRepository(context.applicationContext)

        preferences =
            context.applicationContext.getSharedPreferences(
                "healthyme_preferences",
                Context.MODE_PRIVATE
            )

        loadActivityContext()

        refreshData()
        refreshWeeklyHydration()
        refreshWeeklySleep()
        refreshWeeklyHeartRate()
    }

    fun refreshData() {
        if (!::repository.isInitialized) return

        viewModelScope.launch {
            try {
                healthData = repository.getTodayHealthData()

                android.util.Log.d(
                    "HealthyMe",
                    "Dashboard refreshed: ${healthData.heartRate} BPM"
                )

            } catch (e: SecurityException) {

                android.util.Log.e(
                    "HealthyMe",
                    "Health data permission error",
                    e
                )

            } catch (e: Exception) {

                android.util.Log.e(
                    "HealthyMe",
                    "Failed to refresh dashboard",
                    e
                )
            }
        }
    }

    fun updateHeartRate(heartRate: Int) {
        healthData = healthData.copy(
            heartRate = heartRate
        )
    }

    fun updateHydration(hydrationMl: Int) {

        healthData = healthData.copy(
            hydrationMl = hydrationMl
        )

        refreshWeeklyHydration()
    }

    fun refreshWeeklyHydration() {

        if (!::repository.isInitialized) return

        viewModelScope.launch {

            try {

                weeklyHydration =
                    repository.getWeeklyHydration()

                android.util.Log.d(
                    "HealthyMe",
                    "Weekly hydration loaded: ${weeklyHydration.size} days"
                )

            } catch (e: Exception) {

                android.util.Log.e(
                    "HealthyMe",
                    "Failed to load weekly hydration",
                    e
                )
            }
        }
    }

    fun refreshWeeklySleep() {

        if (!::repository.isInitialized) return

        viewModelScope.launch {

            try {

                weeklySleep =
                    repository.getWeeklySleep()

                android.util.Log.d(
                    "HealthyMe",
                    "Weekly sleep loaded: ${weeklySleep.size} days"
                )

            } catch (e: Exception) {

                android.util.Log.e(
                    "HealthyMe",
                    "Failed to load weekly sleep",
                    e
                )
            }
        }
    }

    private fun loadActivityContext() {

        val savedValue =
            preferences.getString(
                "activity_context",
                ActivityContext.RESTING.name
            )

        activityContext =
            try {
                ActivityContext.valueOf(
                    savedValue ?: ActivityContext.RESTING.name
                )
            } catch (e: Exception) {
                ActivityContext.RESTING
            }

        if (
            !preferences.contains(
                "activity_context_changed_at"
            )
        ) {
            preferences
                .edit()
                .putLong(
                    "activity_context_changed_at",
                    System.currentTimeMillis()
                )
                .apply()
        }
    }

    fun updateActivityContext(
        newContext: ActivityContext
    ) {

        if (newContext == ActivityContext.RESTING) {
            preferences
                .edit()
                .putLong(
                    "resting_hr_last_saved_at",
                    0L
                )
                .apply()
        }

        if (activityContext == newContext) {
            return
        }

        activityContext = newContext

        preferences
            .edit()
            .putString(
                "activity_context",
                newContext.name
            )
            .putLong(
                "activity_context_changed_at",
                System.currentTimeMillis()
            )
            .apply()

        android.util.Log.d(
            "HealthyMe",
            "Activity context changed to: ${newContext.name}"
        )
    }

    fun refreshWeeklyHeartRate() {

        if (!::repository.isInitialized) return

        viewModelScope.launch {

            try {

                weeklyHeartRate =
                    repository
                        .getWeeklyRestingHeartRate()

                android.util.Log.d(
                    "HealthyMe",
                    "Weekly resting HR loaded: ${weeklyHeartRate.size} days"
                )

            } catch (e: Exception) {

                android.util.Log.e(
                    "HealthyMe",
                    "Failed to load weekly resting HR",
                    e
                )
            }
        }
    }
}