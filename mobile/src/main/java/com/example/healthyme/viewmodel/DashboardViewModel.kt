package com.example.healthyme.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.healthyme.data.repository.HealthRepository
import com.example.healthyme.model.HealthData

class DashboardViewModel : ViewModel() {

    private val repository = HealthRepository()

    var healthData by mutableStateOf(repository.getTodayHealthData())
        private set

    fun refreshData() {
        healthData = repository.getTodayHealthData()
    }
}