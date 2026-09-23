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

class DashboardViewModel : ViewModel() {

    private lateinit var repository: HealthRepository

    var healthData by mutableStateOf(HealthData())
        private set

    fun initialize(context: Context) {
        if (::repository.isInitialized) return

        repository = HealthRepository(context)
        refreshData()
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
    }
}