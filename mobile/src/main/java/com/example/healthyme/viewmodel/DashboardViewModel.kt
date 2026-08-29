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

            } catch (e: SecurityException) {

                // Permission hasn't been granted yet.
                // Keep the default health data.

            }
        }
    }
}