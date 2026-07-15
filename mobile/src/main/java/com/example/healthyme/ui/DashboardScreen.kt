package com.example.healthyme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen() {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {

            Text(
                text = "HealthyMe",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Good Afternoon 👋",
                color = Color.Gray
            )
        }

        item {

            HealthCard(
                emoji = "❤️",
                title = "Heart Rate",
                value = "78",
                unit = "BPM",
                subtitle = "Normal",
                color = Color(0xFFE53935)
            )
        }

        item {

            HealthCard(
                emoji = "🌙",
                title = "Sleep",
                value = "7.5",
                unit = "hrs",
                subtitle = "Good Sleep",
                color = Color(0xFF1E88E5)
            )
        }

        item {

            HealthCard(
                emoji = "💧",
                title = "Hydration",
                value = "1.2",
                unit = "L",
                subtitle = "Goal: 2.5 L",
                color = Color(0xFF00ACC1)
            )
        }

        item {

            HealthCard(
                emoji = "👣",
                title = "Steps",
                value = "6542",
                unit = "",
                subtitle = "Goal: 10000",
                color = Color(0xFF43A047)
            )
        }

        item {

            HealthCard(
                emoji = "⭐",
                title = "Lifestyle Score",
                value = "84",
                unit = "/100",
                subtitle = "Excellent",
                color = Color(0xFFFF9800)
            )
        }
    }
}