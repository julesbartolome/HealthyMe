package com.example.healthyme.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthyme.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = viewModel()
) {

    val context = LocalContext.current

    /*
     * Initialize dashboard and load health data.
     */
    LaunchedEffect(Unit) {
        dashboardViewModel.initialize(context)
    }

    /*
     * Listen for live updates from the Watch
     * and hydration NFC events.
     */
    DisposableEffect(context) {

        val healthReceiver = object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                when (intent?.action) {

                    "com.example.healthyme.HEART_RATE_UPDATED" -> {

                        val heartRate =
                            intent.getIntExtra("heart_rate", 0)

                        if (heartRate > 0) {

                            android.util.Log.d(
                                "HealthyMe",
                                "Dashboard received live heart rate: $heartRate BPM"
                            )

                            dashboardViewModel.updateHeartRate(
                                heartRate
                            )
                        }
                    }

                    "com.example.healthyme.HYDRATION_UPDATED" -> {

                        val hydration =
                            intent.getIntExtra("hydration_ml", 0)

                        android.util.Log.d(
                            "HealthyMe",
                            "Dashboard received hydration update: $hydration ml"
                        )

                        dashboardViewModel.updateHydration(
                            hydration
                        )
                    }
                }
            }
        }

        val intentFilter = IntentFilter().apply {
            addAction(
                "com.example.healthyme.HEART_RATE_UPDATED"
            )
            addAction(
                "com.example.healthyme.HYDRATION_UPDATED"
            )
        }

        ContextCompat.registerReceiver(
            context,
            healthReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(healthReceiver)
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

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // Heart Rate
        HealthCard(
            emoji = "❤️",
            title = "Heart Rate",
            value = if (healthData.heartRate > 0) {
                healthData.heartRate.toString()
            } else {
                "--"
            },
            unit = "BPM",
            status = if (healthData.heartRate > 0) {
                "Live reading"
            } else {
                "Waiting for watch"
            },
            accentColor = Color(0xFFE53935)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Sleep
        HealthCard(
            emoji = "🌙",
            title = "Sleep",
            value = healthData.sleepHours,
            unit = "hrs",
            status = if (healthData.sleepHours != "--") {
                "Last night"
            } else {
                "No sleep data"
            },
            accentColor = Color(0xFF1E88E5)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Hydration
        HealthCard(
            emoji = "💧",
            title = "Hydration",
            value = String.format(
                "%,d",
                healthData.hydrationMl
            ),
            unit = "ml",
            status = "Today's intake",
            accentColor = Color(0xFF00ACC1)
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // Daily Summary
        Text(
            text = "Today's Summary",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        DailySummaryCard(
            heartRate = healthData.heartRate,
            sleepHours = healthData.sleepHours,
            hydrationMl = healthData.hydrationMl
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )
    }
}


@Composable
fun DailySummaryCard(
    heartRate: Int,
    sleepHours: String,
    hydrationMl: Int
) {

    val completedItems = listOf(
        heartRate > 0,
        sleepHours != "--",
        hydrationMl > 0
    ).count { it }

    val summaryText = when (completedItems) {

        3 -> "Your health data is up to date for today."

        2 -> "Most of your health data is available."

        1 -> "Some health data is available."

        else -> "No health data has been recorded yet."
    }

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

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = summaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            SummaryRow(
                emoji = "❤️",
                label = "Heart Rate",
                value = if (heartRate > 0) {
                    "$heartRate BPM"
                } else {
                    "No data"
                }
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            SummaryRow(
                emoji = "🌙",
                label = "Sleep",
                value = if (sleepHours != "--") {
                    "$sleepHours hrs"
                } else {
                    "No data"
                }
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            SummaryRow(
                emoji = "💧",
                label = "Hydration",
                value = if (hydrationMl > 0) {
                    String.format(
                        "%,d ml",
                        hydrationMl
                    )
                } else {
                    "No intake yet"
                }
            )
        }
    }
}


@Composable
fun SummaryRow(
    emoji: String,
    label: String,
    value: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = emoji,
                fontSize = 18.sp
            )

            Spacer(
                modifier = Modifier.size(8.dp)
            )

            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF222222)
        )
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

            Spacer(
                modifier = Modifier.size(14.dp)
            )

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

                    Spacer(
                        modifier = Modifier.size(4.dp)
                    )

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