package com.example.healthyme.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.concurrent.futures.await
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.healthyme.presentation.theme.HealthyMeTheme
import kotlinx.coroutines.launch

// ... existing imports ...

class MainActivity : ComponentActivity() {private val heartRateValue = mutableStateOf("--")
    private val heartRateStatus = mutableStateOf("Starting...")

    private lateinit var heartRateCallback: MeasureCallback
    // Declare measureClient at class level so it's accessible in functions
    private lateinit var measureClient: androidx.health.services.client.MeasureClient
    private lateinit var exerciseClient: androidx.health.services.client.ExerciseClient
    private lateinit var exerciseCallback: androidx.health.services.client.ExerciseUpdateCallback

    override fun onCreate(savedInstanceState: Bundle?) {installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        // 1. Initialize Health Services clients
        val healthClient = HealthServices.getClient(this)
        measureClient = healthClient.measureClient
        exerciseClient = healthClient.exerciseClient

        // 2. Define the MeasureCallback (for heart rate)
        heartRateCallback = object : MeasureCallback {
            override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) {
                when (availability) {
                    DataTypeAvailability.AVAILABLE -> heartRateStatus.value = "Measuring..."
                    DataTypeAvailability.ACQUIRING -> heartRateStatus.value = "Acquiring..."
                    DataTypeAvailability.UNAVAILABLE -> heartRateStatus.value = "Unavailable"
                    else -> heartRateStatus.value = "Check fit"
                }
            }

            override fun onDataReceived(data: DataPointContainer) {
                val heartRatePoints = data.getData(DataType.HEART_RATE_BPM)
                heartRatePoints.lastOrNull()?.let { point ->
                    heartRateValue.value = "${point.value.toInt()}"
                    heartRateStatus.value = "Good signal"
                }
            }
        }

        // 3. Define the ExerciseUpdateCallback (Separately!)
        // 3. Define the ExerciseUpdateCallback (Separately!)
        exerciseCallback = object : androidx.health.services.client.ExerciseUpdateCallback {
            override fun onAvailabilityChanged(
                dataType: DataType<*, *>,
                availability: Availability
            ) {
                heartRateStatus.value = availability.toString()
            }

            override fun onExerciseUpdateReceived(
                update: androidx.health.services.client.data.ExerciseUpdate
            ) {
                val hr = update.latestMetrics.getData(DataType.HEART_RATE_BPM)
                hr.lastOrNull()?.let {
                    heartRateValue.value = "${it.value.toInt()}"
                    heartRateStatus.value = "Measuring"
                }
            }

            override fun onLapSummaryReceived(
                lapSummary: androidx.health.services.client.data.ExerciseLapSummary
            ) {}

            override fun onRegistered() {}

            // ADD THIS METHOD TO FIX THE ERROR
            override fun onRegistrationFailed(throwable: Throwable) {
                heartRateStatus.value = "Registration Failed"
            }
        }

        // 4. Setup Permission Request
        val permissionRequest = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[android.Manifest.permission.BODY_SENSORS] == true

            if (granted) {
                prepareHealthServices()
            } else {
                heartRateStatus.value = "Permission Denied"
            }
        }

        // 5. Launch Permission Request
        permissionRequest.launch(
            arrayOf(android.Manifest.permission.BODY_SENSORS)
        )

        setContent {
            HealthyMeTheme {
                HealthyMeDashboard(
                    heartRate = heartRateValue.value,
                    heartRateStatus = heartRateStatus.value
                )
            }
        }
    }

    // 5. DEFINE THE MISSING FUNCTION
    private fun prepareHealthServices() {
        lifecycleScope.launch {
            try {
                val config = androidx.health.services.client.data.ExerciseConfig(
                    exerciseType = androidx.health.services.client.data.ExerciseType.WALKING,                    dataTypes = setOf(DataType.HEART_RATE_BPM),
                    isAutoPauseAndResumeEnabled = false,
                    isGpsEnabled = false
                )

                exerciseClient.startExerciseAsync(config).await()
                exerciseClient.setUpdateCallback(exerciseCallback)

                heartRateStatus.value = "Starting measurement..."

            } catch (e: Exception) {
                heartRateStatus.value = "Error: ${e.message}"
                android.util.Log.e("HealthyMe", "Start error", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch {
            try {
                measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, heartRateCallback).await()
            } catch (e: Exception) {
                android.util.Log.e("HealthyMe", "Unregister error: ${e.message}")
            }
        }
    }
}

// ── Colour palette ─────────────────────────────────────────────
val CardRed  = Color(0xFFE53935)
val CardBlue = Color(0xFF1E88E5)
val CardCyan = Color(0xFF00ACC1)
val DarkBg   = Color(0xFF121212)
val CardBg   = Color(0xFF1E1E1E)

// ── Root dashboard ─────────────────────────────────────────────
@Composable
fun HealthyMeDashboard(heartRate: String, heartRateStatus: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        TimeText()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = "HealthyMe",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            DashboardCard(
                emoji = "❤️",
                label = "Heart Rate",
                value = heartRate,
                unit = "BPM",
                subtext = heartRateStatus,
                accentColor = CardRed
            )
            DashboardCard(
                emoji = "🌙",
                label = "Sleep",
                value = "--",
                unit = "hrs",
                subtext = "Coming soon",
                accentColor = CardBlue
            )
            DashboardCard(
                emoji = "💧",
                label = "Hydration",
                value = "--",
                unit = "ml",
                subtext = "Coming soon",
                accentColor = CardCyan
            )
            DashboardCard(
                emoji = "💧",
                label = "...",
                value = "--",
                unit = "ml",
                subtext = "Coming soon",
                accentColor = CardCyan
            )
        }
    }
}

// ── Reusable card ──────────────────────────────────────────────
@Composable
fun DashboardCard(
    emoji: String,
    label: String,
    value: String,
    unit: String,
    subtext: String,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = emoji, fontSize = 16.sp)
                Text(text = label, fontSize = 10.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = unit,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(text = subtext, fontSize = 9.sp, color = Color.Gray)
            }
        }
    }
}