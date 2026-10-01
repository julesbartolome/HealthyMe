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
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.healthyme.presentation.theme.HealthyMeTheme
import kotlinx.coroutines.launch
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class MainActivity : ComponentActivity() {

    private val heartRateValue = mutableStateOf("--")
    private val heartRateStatus = mutableStateOf("Starting...")
    private val sleepValue = mutableStateOf("--")
    private val sleepStatus = mutableStateOf("Waiting for phone")
    private val hydrationValue = mutableStateOf(0)

    private lateinit var heartRateCallback: MeasureCallback
    // Declare measureClient at class level so it's accessible in functions
    private lateinit var measureClient: androidx.health.services.client.MeasureClient
    private lateinit var exerciseClient: androidx.health.services.client.ExerciseClient
    private lateinit var exerciseCallback: androidx.health.services.client.ExerciseUpdateCallback
    private lateinit var phoneMessenger: PhoneMessenger
    private val hydrationReceiver = object : BroadcastReceiver() {

        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {

            hydrationValue.value =
                intent?.getIntExtra(
                    "hydration",
                    0
                ) ?: 0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        sleepValue.value =
            WatchDataListenerService.getLatestSleep(this)

        sleepStatus.value =
            if (sleepValue.value == "--") {
                "No sleep data"
            } else {
                "Last night"
            }

        hydrationValue.value =
            WatchDataListenerService.getLatestHydration(this)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }

        setTheme(android.R.style.Theme_DeviceDefault)

        // 1. Initialize Health Services clients
        val healthClient = HealthServices.getClient(this)
        measureClient = healthClient.measureClient
        exerciseClient = healthClient.exerciseClient
        phoneMessenger = PhoneMessenger(this)
        phoneMessenger.requestHealthSync()

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
                    phoneMessenger.sendHeartRate(point.value.toInt())
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

            val granted = permissions[android.Manifest.permission.BODY_SENSORS] == true

            if (granted) {
                prepareHealthServices()
            } else {
                heartRateStatus.value = "Permission Denied"
            }

        }

        // In onCreate, update the array:
        permissionRequest.launch(
            arrayOf(
                android.Manifest.permission.BODY_SENSORS,
            )
        )

        val sleepFilter =
            IntentFilter(WatchDataListenerService.ACTION_SLEEP_UPDATED)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                sleepReceiver,
                sleepFilter,
                RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(
                sleepReceiver,
                sleepFilter
            )
        }

        setContent {
            HealthyMeTheme {
                HealthyMeDashboard(
                    heartRate = heartRateValue.value,
                    heartRateStatus = heartRateStatus.value,
                    sleepValue = sleepValue.value,
                    sleepStatus = sleepStatus.value,
                    hydrationMl = hydrationValue.value
                )
            }
        }

        val hydrationFilter =
            IntentFilter(
                WatchDataListenerService.ACTION_HYDRATION_UPDATED
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                hydrationReceiver,
                hydrationFilter,
                RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(
                hydrationReceiver,
                hydrationFilter
            )
        }
    }

    // 5. DEFINE THE MISSING FUNCTION
    private fun prepareHealthServices() {
        lifecycleScope.launch {
            try {
                measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, heartRateCallback)
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

        unregisterReceiver(sleepReceiver)
        unregisterReceiver(hydrationReceiver)

        lifecycleScope.launch {
            try {
                measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, heartRateCallback).await()
            } catch (e: Exception) {
                android.util.Log.e("HealthyMe", "Unregister error: ${e.message}")
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
fun HealthyMeDashboard(
    heartRate: String,
    heartRateStatus: String,
    sleepValue: String,
    sleepStatus: String,
    hydrationMl: Int
) {
    // Create a scroll state for the list
    val listState = rememberScalingLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        TimeText() // Keeps time at the top

        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            // Adds padding so the first/last items aren't cut off by the screen curves
            contentPadding = PaddingValues(
                top = 32.dp,
                start = 8.dp,
                end = 8.dp,
                bottom = 32.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header
            item {
                Text(
                    text = "HealthyMe",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Heart Rate Card
            item {
                DashboardCard(
                    emoji = "❤️",
                    label = "Heart Rate",
                    value = heartRate,
                    unit = "BPM",
                    subtext = heartRateStatus,
                    accentColor = CardRed
                )
            }

            // Sleep Card
            item {
                DashboardCard(
                    emoji = "🌙",
                    label = "Sleep",
                    value = sleepValue,
                    unit = "hrs",
                    subtext = sleepStatus,
                    accentColor = CardBlue
                )
            }

            // Hydration Card
            item {
                DashboardCard(
                    emoji = "💧",
                    label = "Hydration",
                    value = hydrationMl.toString(),
                    unit = "ml",
                    subtext = "Today's intake",
                    accentColor = CardCyan
                )
            }
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
    private val sleepReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {

            val sleep = intent?.getStringExtra("sleep") ?: return

            sleepValue.value = sleep

            sleepStatus.value =
                if (sleep == "--") {
                    "No sleep data"
                } else {
                    "Last night"
                }
        }
    }
}