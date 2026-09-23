package com.example.healthyme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthyme.viewmodel.DashboardViewModel
import androidx.compose.ui.platform.LocalContext
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

@Composable
fun DashboardScreen(
    onRequestSleepPermission: () -> Unit = {},
    dashboardViewModel: DashboardViewModel = viewModel()
) {

    val context = LocalContext.current

    /*
     * Initialize the dashboard and load sleep data.
     */
    LaunchedEffect(Unit) {
        dashboardViewModel.initialize(context)
    }

    /*
     * Listen for heart-rate updates from the Watch.
     *
     * Every time HeartRateListenerService receives a new BPM,
     * it sends the HEART_RATE_UPDATED broadcast.
     */
    DisposableEffect(context) {

        val heartRateReceiver = object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                if (intent?.action == "com.example.healthyme.HEART_RATE_UPDATED") {

                    val heartRate =
                        intent.getIntExtra("heart_rate", 0)

                    if (heartRate > 0) {

                        android.util.Log.d(
                            "HealthyMe",
                            "Dashboard received live heart rate: $heartRate BPM"
                        )

                        dashboardViewModel.updateHeartRate(heartRate)
                    }
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            heartRateReceiver,
            IntentFilter("com.example.healthyme.HEART_RATE_UPDATED"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(heartRateReceiver)
        }
    }

    val scrollState = rememberScrollState()

    val healthData = dashboardViewModel.healthData

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {

        // Header
        Text(
            text = "HealthyMe",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
        )

        Text(
            text = "Your daily health overview",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                android.util.Log.d(
                    "HealthyMe",
                    "SLEEP BUTTON PRESSED"
                )

                onRequestSleepPermission()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Connect Sleep Data")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Heart Rate
        HealthCard(
            emoji = "❤️",
            title = "Heart Rate",
            value = healthData.heartRate.toString(),
            unit = "BPM",
            status = "Good signal",
            accentColor = Color(0xFFE53935)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sleep
        HealthCard(
            emoji = "🌙",
            title = "Sleep",
            value = healthData.sleepHours,
            unit = "hrs",
            status = "Last night",
            accentColor = Color(0xFF1E88E5)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Hydration
        HealthCard(
            emoji = "💧",
            title = "Hydration",
            value = String.format("%,d", healthData.hydrationMl),
            unit = "ml",
            status = "Today's intake",
            accentColor = Color(0xFF00ACC1)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Activity
        HealthCard(
            emoji = "🚶",
            title = "Activity",
            value = String.format("%,d", healthData.steps),
            unit = "steps",
            status = "Today's activity",
            accentColor = Color(0xFF43A047)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Daily Summary
        Text(
            text = "Today's Summary",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "You're doing well!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Keep monitoring your health throughout the day.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}


@Composable
fun HealthCard(
    emoji: String,
    title: String,
    value: String,
    unit: String,
    status: String,
    accentColor: Color
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = emoji,
                    fontSize = 25.sp
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            // Label
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    fontSize = 15.sp,
                    color = Color.Gray
                )

                Text(
                    text = status,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Value
            Column(
                horizontalAlignment = Alignment.End
            ) {

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {

                    Text(
                        text = value,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )

                    Spacer(modifier = Modifier.size(4.dp))

                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

