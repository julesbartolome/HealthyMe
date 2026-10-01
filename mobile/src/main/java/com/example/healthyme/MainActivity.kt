package com.example.healthyme

import android.os.Bundle
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.lifecycle.lifecycleScope
import com.example.healthyme.ui.screens.DashboardScreen
import com.example.healthyme.ui.theme.HealthyMeTheme
import kotlinx.coroutines.launch
import com.example.healthyme.data.database.HealthyMeDatabase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.healthyme.ui.screens.RegisterBottleScreen
import com.example.healthyme.data.BottleEntity
import com.example.healthyme.data.HydrationEventEntity
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.healthyme.notifications.HydrationReminderWorker
import java.util.concurrent.TimeUnit
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.content.pm.PackageManager
import android.Manifest
import com.example.healthyme.data.auth.FirestoreHydrationRepository
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import com.example.healthyme.notifications.HydrationSyncWorker
import com.example.healthyme.notifications.SleepReminderScheduler
import com.example.healthyme.notifications.MovementReminderWorker

class MainActivity : ComponentActivity() {

    private lateinit var database: HealthyMeDatabase
    private val firestoreHydrationRepository =
        FirestoreHydrationRepository()
    private var tagToRegister by mutableStateOf<String?>(null)
    private var nfcAdapter: NfcAdapter? = null

    private val nfcReaderCallback = NfcAdapter.ReaderCallback { tag: Tag ->

        val tagId = tag.id.joinToString("") {
            "%02X".format(it)
        }

        Log.d(
            "HealthyMe",
            "NFC TAG DETECTED: $tagId"
        )

        lifecycleScope.launch {

            try {

                val bottle = database.bottleDao().getBottle(tagId)

                if (bottle == null) {

                    Log.d(
                        "HealthyMe",
                        "Bottle NOT registered: $tagId"
                    )

                    tagToRegister = tagId

                } else {

                    Log.d(
                        "HealthyMe",
                        "Bottle recognized: ${bottle.name}"
                    )

                    Log.d(
                        "HealthyMe",
                        "Amount per scan: ${bottle.amountPerScanMl} ml"
                    )

                    try {

                        val hydrationEvent = HydrationEventEntity(
                            tagId = bottle.tagId,
                            amountMl = bottle.amountPerScanMl,
                            timestamp = System.currentTimeMillis()
                        )

                        val eventId =
                            database
                                .hydrationEventDao()
                                .insertEvent(hydrationEvent)

                        val savedEvent =
                            hydrationEvent.copy(
                                id = eventId.toInt()
                            )

                        val firebaseResult =
                            firestoreHydrationRepository
                                .uploadHydrationEvent(savedEvent)

                        firebaseResult.fold(

                            onSuccess = {

                                database
                                    .hydrationEventDao()
                                    .markAsSynced(savedEvent.id)

                                Log.d(
                                    "HealthyMe",
                                    "Hydration event synced to Firebase: ${savedEvent.id}"
                                )
                            },

                            onFailure = { error ->

                                Log.e(
                                    "HealthyMe",
                                    "Hydration sync failed; event remains unsynced",
                                    error
                                )
                            }
                        )

                        scheduleHydrationSync()

                        val now = java.time.LocalDate
                            .now()
                            .atStartOfDay(java.time.ZoneId.systemDefault())
                            .toInstant()

                        val endOfDay = now
                            .plus(java.time.Duration.ofDays(1))

                        val totalHydration =
                            database
                                .hydrationEventDao()
                                .getTotalHydrationForDay(
                                    now.toEpochMilli(),
                                    endOfDay.toEpochMilli()
                                )

                        Log.d(
                            "HealthyMe",
                            "Updated hydration total: $totalHydration ml"
                        )

                        val intent = Intent("com.example.healthyme.HYDRATION_UPDATED")

                        intent.putExtra(
                            "hydration_ml",
                            totalHydration
                        )

                        sendBroadcast(intent)

                        WatchDataMessenger(this@MainActivity)
                            .sendHydration(totalHydration)


                    } catch (e: Exception) {

                        Log.e(
                            "HealthyMe",
                            "Failed to record hydration",
                            e
                        )
                    }
                }

            } catch (e: Exception) {

                Log.e(
                    "HealthyMe",
                    "Failed to look up NFC bottle",
                    e
                )
            }
        }
    }

    private var isRequestingHealthConnectPermission = false

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(this)
    }

    private val healthConnectPermissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    private val requestPermissions =
        registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { grantedPermissions ->

            isRequestingHealthConnectPermission = false

            android.util.Log.d(
                "HealthyMe",
                "Permission result: $grantedPermissions"
            )

            if (grantedPermissions.containsAll(healthConnectPermissions)) {

                android.util.Log.d(
                    "HealthyMe",
                    "Sleep permission GRANTED"
                )

            } else {

                android.util.Log.d(
                    "HealthyMe",
                    "Sleep permission NOT granted"
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        requestNotificationPermission()
        scheduleHydrationReminder()
        scheduleMovementReminder()

        SleepReminderScheduler.scheduleDaily(this)

        database = HealthyMeDatabase.getDatabase(this)

        Log.d(
            "HealthyMe",
            "Room database initialized"
        )

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Log.e("HealthyMe", "NFC is NOT supported on this phone")
        } else if (!nfcAdapter!!.isEnabled) {
            Log.e("HealthyMe", "NFC is supported but currently DISABLED")
        } else {
            Log.d("HealthyMe", "NFC is supported and ENABLED")
        }

        setContent {
            HealthyMeTheme {

                if (tagToRegister != null) {

                    RegisterBottleScreen(
                        tagId = tagToRegister!!,
                        onSave = { name, capacity, amountPerScan ->

                            lifecycleScope.launch {

                                try {

                                    val bottle = BottleEntity(
                                        tagId = tagToRegister!!,
                                        name = name,
                                        capacityMl = capacity,
                                        amountPerScanMl = amountPerScan
                                    )

                                    database.bottleDao().insertBottle(bottle)

                                    Log.d(
                                        "HealthyMe",
                                        "Bottle registered successfully: ${bottle.name}"
                                    )

                                    tagToRegister = null

                                } catch (e: Exception) {

                                    Log.e(
                                        "HealthyMe",
                                        "Failed to register bottle",
                                        e
                                    )
                                }
                            }
                        }
                    )

                } else {
                    DashboardScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Log.d("HealthyMe", "NFC reader mode STARTING")

        nfcAdapter?.enableReaderMode(
            this,
            nfcReaderCallback,
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V,
            null
        )
    }

    override fun onPause() {
        super.onPause()

        Log.d("HealthyMe", "NFC reader mode STOPPING")

        nfcAdapter?.disableReaderMode(this)
    }

    private fun requestHealthConnectPermissions() {

        if (isRequestingHealthConnectPermission) {
            android.util.Log.d(
                "HealthyMe",
                "Permission request already in progress"
            )
            return
        }

        isRequestingHealthConnectPermission = true

        android.util.Log.d(
            "HealthyMe",
            "requestHealthConnectPermissions() called"
        )

        lifecycleScope.launch {

            try {

                val sdkStatus =
                    HealthConnectClient.getSdkStatus(this@MainActivity)

                android.util.Log.d(
                    "HealthyMe",
                    "Health Connect SDK status: $sdkStatus"
                )

                if (sdkStatus != HealthConnectClient.SDK_AVAILABLE) {

                    android.util.Log.e(
                        "HealthyMe",
                        "Health Connect is NOT available"
                    )

                    return@launch
                }

                val grantedPermissions =
                    healthConnectClient
                        .permissionController
                        .getGrantedPermissions()

                android.util.Log.d(
                    "HealthyMe",
                    "Granted permissions: $grantedPermissions"
                )

                if (!grantedPermissions.containsAll(healthConnectPermissions)) {

                    android.util.Log.d(
                        "HealthyMe",
                        "Launching permission request..."

                    )

                    requestPermissions.launch(
                        healthConnectPermissions
                    )

                } else {

                    android.util.Log.d(
                        "HealthyMe",
                        "Sleep permission already granted"
                    )
                }

            } catch (e: Exception) {

                isRequestingHealthConnectPermission = false

                android.util.Log.e(
                    "HealthyMe",
                    "Health Connect request failed",
                    e
                )
            }
        }
    }

    private fun scheduleHydrationReminder() {

        val reminderRequest =
            PeriodicWorkRequestBuilder<HydrationReminderWorker>(
                15,
                TimeUnit.MINUTES
            ).build()

        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                "hydration_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                reminderRequest
            )
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                "hydration_reminders",
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water"
            }

            val notificationManager =
                getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel)

            val sleepChannel =
                NotificationChannel(
                    "sleep_reminders",
                    "Sleep Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description =
                        "Bedtime and sleep reminders"
                }

            notificationManager.createNotificationChannel(
                sleepChannel
            )

            val movementChannel =
                NotificationChannel(
                    "movement_reminders",
                    "Movement Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description =
                        "Reminders to take movement breaks"
                }

            notificationManager.createNotificationChannel(
                movementChannel
            )
        }
    }

    private fun requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (
                checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    1001
                )
            }
        }
    }

    private fun scheduleHydrationSync() {

        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val syncRequest =
            OneTimeWorkRequestBuilder<HydrationSyncWorker>()
                .setConstraints(constraints)
                .build()

        WorkManager
            .getInstance(this)
            .enqueueUniqueWork(
                "hydration_sync",
                ExistingWorkPolicy.KEEP,
                syncRequest
            )
    }

    private fun scheduleMovementReminder() {

        val request =
            PeriodicWorkRequestBuilder<MovementReminderWorker>(
                15,
                TimeUnit.MINUTES
            ).build()

        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                "movement_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
}