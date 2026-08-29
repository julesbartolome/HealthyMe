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
    }
}