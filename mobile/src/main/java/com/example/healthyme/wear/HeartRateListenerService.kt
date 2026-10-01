package com.example.healthyme.wear

import android.util.Log
import com.example.healthyme.data.database.HeartRateEntity
import com.example.healthyme.data.database.HealthyMeDatabase
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object HeartRateState {
    val heartRate = androidx.compose.runtime.mutableStateOf("--")
}

class HeartRateListenerService : WearableListenerService() {

    private fun showHydrationNotification() {

        val notificationManager =
            getSystemService(
                android.content.Context.NOTIFICATION_SERVICE
            ) as android.app.NotificationManager

        val notification =
            androidx.core.app.NotificationCompat.Builder(
                this,
                "hydration_reminders"
            )
                .setSmallIcon(com.example.healthyme.R.drawable.ic_launcher_foreground)
                .setContentTitle("Time to hydrate 💧")
                .setContentText(
                    "It's been more than 2 hours since your last drink."
                )
                .setPriority(
                    androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .build()

        notificationManager.notify(
            2001,
            notification
        )
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {

        super.onMessageReceived(messageEvent)

        if (messageEvent.path == "/heart_rate") {

            val heartRate =
                String(messageEvent.data).toIntOrNull()

            if (heartRate == null) {
                Log.e(
                    "HealthyMe",
                    "Invalid heart rate received"
                )
                return
            }

            Log.d(
                "HealthyMe",
                "Heart Rate received: $heartRate BPM"
            )

            // Update the live dashboard
            HeartRateState.heartRate.value =
                heartRate.toString()

            // Save to Room
            val database =
                HealthyMeDatabase.getDatabase(applicationContext)

            CoroutineScope(Dispatchers.IO).launch {

                database.heartRateDao().insertHeartRate(
                    HeartRateEntity(
                        bpm = heartRate,
                        timestamp = System.currentTimeMillis()
                    )
                )

                Log.d(
                    "HealthyMe",
                    "Heart Rate saved to database: $heartRate BPM"
                )
            }
        }

        if (messageEvent.path == "/hydration_reminder") {

            Log.d(
                "HealthyMe",
                "Hydration reminder received from phone"
            )
        }
    }
}