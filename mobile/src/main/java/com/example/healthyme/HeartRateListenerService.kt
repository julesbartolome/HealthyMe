package com.example.healthyme

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import android.content.Intent

class HeartRateListenerService : WearableListenerService() {

    companion object {
        private const val PREFS_NAME = "healthyme_health"
        private const val KEY_HEART_RATE = "heart_rate"

        fun getLatestHeartRate(context: Context): Int {
            val prefs = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

            return prefs.getInt(KEY_HEART_RATE, 0)
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {

        if (messageEvent.path == "/heart_rate") {

            try {

                val heartRate = String(messageEvent.data).toInt()

                getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )
                    .edit()
                    .putInt(KEY_HEART_RATE, heartRate)
                    .apply()

                val intent = Intent("com.example.healthyme.HEART_RATE_UPDATED")
                intent.putExtra("heart_rate", heartRate)

                sendBroadcast(intent)

                Log.d(
                    "HealthyMe",
                    "Heart rate received from watch: $heartRate BPM"
                )

            } catch (e: Exception) {

                Log.e(
                    "HealthyMe",
                    "Failed to read heart rate message",
                    e
                )
            }
        }
    }
}