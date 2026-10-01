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
import com.example.healthyme.domain.HealthInterpretation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.healthyme.domain.ActivityContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalContentColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.heightIn

@Composable
fun DashboardScreen(dashboardViewModel: DashboardViewModel = viewModel()) {

    val context = LocalContext.current

    val today = LocalDate.now()

    val formattedDate =
        today.format(
            DateTimeFormatter.ofPattern(
                "EEEE, MMMM d"
            )
        )

    LaunchedEffect(Unit) {
        dashboardViewModel.initialize(context)
    }

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

                    "com.example.healthyme.RESTING_HEART_RATE_SAVED" -> {

                        android.util.Log.d(
                            "HealthyMe",
                            "Dashboard refreshing weekly resting HR"
                        )

                        dashboardViewModel
                            .refreshWeeklyHeartRate()
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
            addAction(
                "com.example.healthyme.RESTING_HEART_RATE_SAVED"
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
    val progressPagerState =
        rememberPagerState(
            pageCount = { 3 }
        )

    val progressPagerScope =
        rememberCoroutineScope()
    val healthData = dashboardViewModel.healthData
    val activityContext =
        dashboardViewModel.activityContext
    val heartRateInterpretation =
        if (healthData.heartRate > 0) {
            HealthInterpretation.interpretHeartRate(
                healthData.heartRate,
                activityContext
            )
        } else {
            null
        }
    val weeklyHydration =
        dashboardViewModel.weeklyHydration
    val weeklySleep =
        dashboardViewModel.weeklySleep
    val weeklyHeartRate =
        dashboardViewModel.weeklyHeartRate
    val sleepMinutes = parseSleepMinutes(healthData.sleepHours)

    val sleepInterpretation =
        if (sleepMinutes != null) {
            HealthInterpretation.interpretSleep(sleepMinutes)
        } else {
            null
        }

    val hydrationInterpretation =
        if (healthData.hydrationMl > 0) {
            HealthInterpretation.interpretHydration(
                healthData.hydrationMl
            )
        } else {
            null
        }

    CompositionLocalProvider(LocalContentColor provides Color(0xFF222222)) {

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
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1F1F1F)
            )

            Text(
                text = "Your daily health overview",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444)
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = formattedDate,
                fontSize = 13.sp,
                color = Color(0xFF888888)
            )

            Spacer(
                modifier = Modifier.height(24.dp)
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
                status = heartRateInterpretation?.status
                    ?: "Waiting for watch",
                accentColor = Color(0xFFE53935)
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Current activity",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF444444)
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            ActivityContextSelector(
                selected = activityContext,
                onSelected = {
                    dashboardViewModel.updateActivityContext(it)
                }
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
                status = sleepInterpretation?.status
                    ?: "No sleep data",
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
                status = hydrationInterpretation?.status
                    ?: "No intake yet",
                accentColor = Color(0xFF00ACC1)
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            SectionHeader(
                title = "Today's Summary",
                subtitle = "A quick look at your health today"
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            DailySummaryCard(
                heartRate = healthData.heartRate,
                sleepHours = healthData.sleepHours,
                hydrationMl = healthData.hydrationMl,
                heartRateInterpretation = heartRateInterpretation,
                sleepInterpretation = sleepInterpretation,
                hydrationInterpretation = hydrationInterpretation
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            SectionHeader(
                title = "7-Day Progress",
                subtitle =
                    "Swipe to explore your recent health trends"
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            WeeklyProgressTabs(
                selectedPage =
                    progressPagerState.currentPage,
                onSelected = { page ->

                    progressPagerScope.launch {

                        progressPagerState
                            .animateScrollToPage(page)
                    }
                }
            )

            Text(
                text = "Tap a category or swipe left/right",
                fontSize = 11.sp,
                color = Color(0xFF888888),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {

                HorizontalPager(
                    state = progressPagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->

                    when (page) {

                        0 -> WeeklyHeartRateCard(
                            weeklyHeartRate = weeklyHeartRate
                        )

                        1 -> WeeklySleepCard(
                            weeklySleep = weeklySleep
                        )

                        2 -> WeeklyHydrationCard(
                            weeklyHydration = weeklyHydration
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                repeat(3) { index ->

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(
                                if (progressPagerState.currentPage == index) {
                                    8.dp
                                } else {
                                    6.dp
                                }
                            )
                            .background(
                                color =
                                    if (
                                        progressPagerState.currentPage == index
                                    ) {
                                        Color(0xFF444444)
                                    } else {
                                        Color(0xFFCCCCCC)
                                    },
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )
        }
    }
}


@Composable
fun DailySummaryCard(
    heartRate: Int,
    sleepHours: String,
    hydrationMl: Int,
    heartRateInterpretation: HealthInterpretation.Result?,
    sleepInterpretation: HealthInterpretation.Result?,
    hydrationInterpretation: HealthInterpretation.Result?
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
            containerColor = Color.White,
            contentColor = Color(0xFF222222)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
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

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFEAF4EC),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {

                Column {

                    Text(
                        text = "HealthyMe Companion",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    if (heartRateInterpretation != null) {

                        CompanionMessage(
                            emoji = "❤️",
                            message = heartRateInterpretation.message
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )
                    }

                    if (sleepInterpretation != null) {

                        CompanionMessage(
                            emoji = "🌙",
                            message = sleepInterpretation.message
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )
                    }

                    if (hydrationInterpretation != null) {

                        CompanionMessage(
                            emoji = "💧",
                            message = hydrationInterpretation.message
                        )
                    }
                }
            }

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
fun CompanionMessage(
    emoji: String,
    message: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.75f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {

        Text(
            text = emoji,
            fontSize = 18.sp
        )

        Spacer(
            modifier = Modifier.size(10.dp)
        )

        Text(
            text = message,
            fontSize = 13.sp,
            color = Color(0xFF3F3F3F),
            modifier = Modifier.weight(1f)
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
            containerColor = Color.White,
            contentColor = Color(0xFF222222)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
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
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF444444)
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = accentColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        )
                ) {

                    Text(
                        text = status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
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
                        fontSize = 28.sp,
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

private fun parseSleepMinutes(
    sleepHours: String
): Long? {

    if (sleepHours == "--") {
        return null
    }

    val parts = sleepHours.split(":")

    if (parts.size != 2) {
        return null
    }

    val hours = parts[0].toLongOrNull()
        ?: return null

    val minutes = parts[1].toLongOrNull()
        ?: return null

    return (hours * 60) + minutes
}

@Composable
fun WeeklyHydrationCard(
    weeklyHydration: List<com.example.healthyme.model.DailyHydration>
) {

    val adequateDays =
        weeklyHydration.count {
            HealthInterpretation.isHydrationAdequate(
                it.amountMl
            )
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color(0xFF222222)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 590.dp)
                .padding(18.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .background(
                        color = Color(0xFFE0F7FA),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Weekly hydration",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF006064)
                )

                Text(
                    text = "$adequateDays/${weeklyHydration.size} adequate days",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00ACC1)
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            if (weeklyHydration.isEmpty()) {

                Text(
                    text = "No hydration history available yet.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                return@Column
            }

            weeklyHydration.forEach { day ->

                WeeklyHydrationRow(
                    day = day
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp)
                    .background(
                        color = Color(0xFFE0F7FA),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.Top
                ) {

                    Text(
                        text = "💧",
                        fontSize = 20.sp
                    )

                    Spacer(
                        modifier = Modifier.size(10.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "HealthyMe Companion",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00838F)
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = weeklyHydrationMessage(
                                adequateDays = adequateDays,
                                totalDays = weeklyHydration.size
                            ),
                            fontSize = 13.sp,
                            color = Color(0xFF444444)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyHydrationRow(
    day: com.example.healthyme.model.DailyHydration
) {

    val interpretation =
        HealthInterpretation.interpretHydration(
            day.amountMl
        )

    val dayName =
        day.date.dayOfWeek
            .name
            .take(3)
            .lowercase()
            .replaceFirstChar {
                it.uppercase()
            }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF8F8F8),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = dayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = interpretation.status,
                fontSize = 11.sp,
                color = Color(0xFF777777)
            )
        }

        Text(
            text = "${day.amountMl} ml",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00ACC1)
        )
    }
}

private fun weeklyHydrationMessage(
    adequateDays: Int,
    totalDays: Int
): String {

    return when {

        adequateDays == totalDays && totalDays > 0 -> {
            "You stayed within the adequate hydration range every day this week. Great consistency!"
        }

        adequateDays >= 5 -> {
            "You stayed within the adequate hydration range on $adequateDays of the last $totalDays days. Nice work!"
        }

        adequateDays >= 3 -> {
            "You reached the adequate hydration range on $adequateDays of the last $totalDays days. Keep building the habit."
        }

        adequateDays > 0 -> {
            "You reached the adequate hydration range on $adequateDays of the last $totalDays days. Try to stay more consistent."
        }

        else -> {
            "You haven't reached the adequate hydration range recently. Try drinking water more regularly throughout the day."
        }
    }
}

@Composable
fun WeeklySleepCard(
    weeklySleep: List<com.example.healthyme.model.DailySleep>
) {

    val recommendedDays =
        weeklySleep.count {
            it.totalMinutes > 0 &&
                    HealthInterpretation
                        .interpretSleep(it.totalMinutes)
                        .status == "Recommended sleep"
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color(0xFF222222)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 590.dp)
                .padding(18.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .background(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Weekly sleep",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0D47A1)
                )

                Text(
                    text = "$recommendedDays/${weeklySleep.size} recommended nights",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E88E5)
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            if (weeklySleep.isEmpty()) {

                Text(
                    text = "No sleep history available yet.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                return@Column
            }

            weeklySleep.forEach { day ->

                WeeklySleepRow(
                    day = day
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp)
                    .background(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.Top
                ) {

                    Text(
                        text = "🌙",
                        fontSize = 20.sp
                    )

                    Spacer(
                        modifier = Modifier.size(10.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "HealthyMe Companion",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = weeklySleepMessage(
                                recommendedDays = recommendedDays,
                                totalDays = weeklySleep.size
                            ),
                            fontSize = 13.sp,
                            color = Color(0xFF444444)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklySleepRow(
    day: com.example.healthyme.model.DailySleep
) {

    val dayName =
        day.date.dayOfWeek
            .name
            .take(3)
            .lowercase()
            .replaceFirstChar {
                it.uppercase()
            }

    val valueText =
        if (day.totalMinutes > 0) {

            val hours =
                day.totalMinutes / 60

            val minutes =
                day.totalMinutes % 60

            String.format(
                "%d:%02d hrs",
                hours,
                minutes
            )

        } else {
            "No data"
        }

    val status =
        if (day.totalMinutes > 0) {

            HealthInterpretation
                .interpretSleep(
                    day.totalMinutes
                )
                .status

        } else {
            "No sleep data"
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF8F8F8),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = dayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = status,
                fontSize = 11.sp,
                color = Color(0xFF777777)
            )
        }

        Text(
            text = valueText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E88E5)
        )
    }
}

private fun weeklySleepMessage(
    recommendedDays: Int,
    totalDays: Int
): String {

    return when {

        recommendedDays == totalDays &&
                totalDays > 0 -> {

            "You stayed within the recommended sleep range every night this week. Great consistency!"
        }

        recommendedDays >= 5 -> {

            "You stayed within the recommended sleep range on $recommendedDays of the last $totalDays nights. Nice work!"
        }

        recommendedDays >= 3 -> {

            "You reached the recommended sleep range on $recommendedDays of the last $totalDays nights. Keep working on a consistent sleep routine."
        }

        recommendedDays > 0 -> {

            "You reached the recommended sleep range on $recommendedDays of the last $totalDays nights. Try to give yourself more consistent rest."
        }

        else -> {

            "You haven't reached the recommended sleep range recently. Try to keep a more consistent bedtime and give yourself enough time to rest."
        }
    }
}

@Composable
fun ActivityContextSelector(
    selected: ActivityContext,
    onSelected: (ActivityContext) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        ActivityContextButton(
            text = "Resting",
            selected = selected == ActivityContext.RESTING,
            onClick = {
                onSelected(ActivityContext.RESTING)
            },
            modifier = Modifier.weight(1f)
        )

        ActivityContextButton(
            text = "Light",
            selected = selected == ActivityContext.LIGHT_ACTIVITY,
            onClick = {
                onSelected(ActivityContext.LIGHT_ACTIVITY)
            },
            modifier = Modifier.weight(1f)
        )

        ActivityContextButton(
            text = "Intense",
            selected = selected == ActivityContext.INTENSE_ACTIVITY,
            onClick = {
                onSelected(ActivityContext.INTENSE_ACTIVITY)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActivityContextButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor =
                if (selected) {
                    Color(0xFFE53935)
                } else {
                    Color(0xFFE8E8E8)
                }
        )
    ) {

        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight =
                if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
            color =
                if (selected) {
                    Color.White
                } else {
                    Color(0xFF333333)
                }
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null
) {

    Column {

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
        )

        if (subtitle != null) {

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF777777)
            )
        }
    }
}

@Composable
fun WeeklyHeartRateCard(
    weeklyHeartRate:
    List<com.example.healthyme.model.DailyHeartRate>
) {

    val withinRangeDays =
        weeklyHeartRate.count { day ->
            day.averageBpm?.let { bpm ->
                bpm in 60..100
            } == true
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color(0xFF222222)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 590.dp)
                .padding(18.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .background(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp
                    ),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "Resting heart rate",
                    fontSize = 13.sp,
                    fontWeight =
                        FontWeight.SemiBold,
                    color = Color(0xFFC62828)
                )

                Text(
                    text =
                        "$withinRangeDays/${weeklyHeartRate.size} within range",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE53935)
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            if (weeklyHeartRate.isEmpty()) {

                Text(
                    text =
                        "No resting heart-rate history available yet.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                return@Column
            }

            weeklyHeartRate.forEach { day ->

                WeeklyHeartRateRow(day)

                Spacer(
                    modifier = Modifier.height(6.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp)
                    .background(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {

                Row(
                    verticalAlignment =
                        Alignment.Top
                ) {

                    Text(
                        text = "❤️",
                        fontSize = 20.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.size(10.dp)
                    )

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text =
                                "HealthyMe Companion",
                            fontSize = 14.sp,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                Color(0xFFC62828)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Text(
                            text =
                                weeklyHeartRateMessage(
                                    withinRangeDays,
                                    weeklyHeartRate.size
                                ),
                            fontSize = 13.sp,
                            color =
                                Color(0xFF444444)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyHeartRateRow(
    day: com.example.healthyme.model.DailyHeartRate
) {

    val dayName =
        day.date.dayOfWeek
            .name
            .take(3)
            .lowercase()
            .replaceFirstChar {
                it.uppercase()
            }

    val bpm =
        day.averageBpm

    val status =
        if (bpm != null) {

            HealthInterpretation
                .interpretHeartRate(
                    bpm,
                    ActivityContext.RESTING
                )
                .status

        } else {
            "No resting data"
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF8F8F8),
                shape =
                    RoundedCornerShape(10.dp)
            )
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = dayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = status,
                fontSize = 11.sp,
                color = Color(0xFF777777)
            )
        }

        Text(
            text =
                if (bpm != null) {
                    "$bpm BPM"
                } else {
                    "No data"
                },
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE53935)
        )
    }
}

private fun weeklyHeartRateMessage(
    withinRangeDays: Int,
    totalDays: Int
): String {

    return when {

        totalDays == 0 -> {
            "No resting heart-rate history is available yet."
        }

        withinRangeDays == totalDays -> {
            "Your recorded resting heart-rate averages stayed within the expected range throughout the week."
        }

        withinRangeDays >= 5 -> {
            "Your resting heart-rate average was within the expected range on $withinRangeDays of the last $totalDays days."
        }

        withinRangeDays > 0 -> {
            "Your resting heart-rate average was within the expected range on $withinRangeDays of the last $totalDays days. Keep tracking consistently."
        }

        else -> {
            "There isn't enough in-range resting heart-rate history yet. Continue tracking while resting."
        }
    }
}

@Composable
fun WeeklyProgressTabs(
    selectedPage: Int,
    onSelected: (Int) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        ProgressTabButton(
            text = "Heart Rate",
            selected = selectedPage == 0,
            accentColor =
                Color(0xFFE53935),
            onClick = {
                onSelected(0)
            },
            modifier =
                Modifier.weight(1f)
        )

        ProgressTabButton(
            text = "Sleep",
            selected = selectedPage == 1,
            accentColor =
                Color(0xFF1E88E5),
            onClick = {
                onSelected(1)
            },
            modifier =
                Modifier.weight(1f)
        )

        ProgressTabButton(
            text = "Hydration",
            selected = selectedPage == 2,
            accentColor =
                Color(0xFF00ACC1),
            onClick = {
                onSelected(2)
            },
            modifier =
                Modifier.weight(1f)
        )
    }
}

@Composable
fun ProgressTabButton(
    text: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    if (selected) {
                        accentColor
                    } else {
                        Color(0xFFE8E8E8)
                    }
            ),
        contentPadding =
            androidx.compose.foundation.layout.PaddingValues(
                horizontal = 6.dp,
                vertical = 9.dp
            )
    ) {

        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight =
                if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
            color =
                if (selected) {
                    Color.White
                } else {
                    Color(0xFF333333)
                }
        )
    }
}

