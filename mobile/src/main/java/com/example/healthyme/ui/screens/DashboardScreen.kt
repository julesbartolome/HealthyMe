package com.example.healthyme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.healthyme.ui.components.HealthCard
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthyme.viewmodel.DashboardViewModel
import com.example.healthyme.wear.HeartRateState
@Composable
fun DashboardScreen() {

    val dashboardViewModel: DashboardViewModel = viewModel()
    val healthData = dashboardViewModel.healthData

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F8))
            .padding(20.dp)
    ) {

        item {

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "HealthyMe",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Your Daily Wellness",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

        }

        item {

            HealthCard(
                emoji = "❤️",
                title = "Heart Rate",
                value =
                    if (HeartRateState.heartRate.value == "--")
                        healthData.heartRate.toString()
                    else
                        HeartRateState.heartRate.value,
                unit = "BPM",
                subtitle = "Normal",
                accentColor = Color(0xFFE53935)
            )

        }

        item {

            Spacer(modifier = Modifier.height(12.dp))

            HealthCard(
                emoji = "🌙",
                title = "Sleep",
                value = healthData.sleepHours.toString(),
                unit = "hrs",
                subtitle = "Good Sleep",
                accentColor = Color(0xFF1E88E5)
            )

        }

        item {

            Spacer(modifier = Modifier.height(12.dp))

            HealthCard(
                emoji = "💧",
                title = "Hydration",
                value = healthData.hydration.toString(),
                unit = "L",
                subtitle = "Goal: 2.5 L",
                accentColor = Color(0xFF00ACC1)
            )

        }

        item {

            Spacer(modifier = Modifier.height(12.dp))

            HealthCard(
                emoji = "👣",
                title = "Steps",
                value = healthData.steps.toString(),
                unit = "",
                subtitle = "Goal: 10000",
                accentColor = Color(0xFF43A047)
            )

        }

    }

}