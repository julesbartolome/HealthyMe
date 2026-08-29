package com.example.healthyme

import android.os.Bundle
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

class MainActivity : ComponentActivity() {

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

        setContent {
            HealthyMeTheme {
                DashboardScreen(
                    onRequestSleepPermission = {
                        android.util.Log.d(
                            "HealthyMe",
                            "CALLING requestHealthConnectPermissions"
                        )

                        requestHealthConnectPermissions()
                    }
                )
            }
        }
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
}