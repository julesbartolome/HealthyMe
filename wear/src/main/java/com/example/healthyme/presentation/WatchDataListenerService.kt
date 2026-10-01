package com.example.healthyme.presentation

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WatchDataListenerService : WearableListenerService() {

    companion object {

        private const val PREFS_NAME = "healthyme_watch"
        private const val KEY_SLEEP = "sleep"

        const val ACTION_SLEEP_UPDATED =
            "com.example.healthyme.SLEEP_UPDATED"

        private const val KEY_HYDRATION = "hydration"

        const val ACTION_HYDRATION_UPDATED =
            "com.example.healthyme.HYDRATION_UPDATED"

        fun getLatestHydration(context: Context): Int {
            return context
                .getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )
                .getInt(KEY_HYDRATION, 0)
        }

        fun getLatestSleep(context: Context): String {
            return context
                .getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )
                .getString(KEY_SLEEP, "--") ?: "--"
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {

        if (messageEvent.path == "/sleep_data") {

            try {

                val sleepValue = String(messageEvent.data)

                getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )
                    .edit()
                    .putString(KEY_SLEEP, sleepValue)
                    .apply()

                val intent = Intent(ACTION_SLEEP_UPDATED)
                intent.putExtra("sleep", sleepValue)

                sendBroadcast(intent)

                Log.d(
                    "HealthyMe",
                    "Sleep received from phone: $sleepValue"
                )

            } catch (e: Exception) {

                Log.e(
                    "HealthyMe",
                    "Failed to read sleep message",
                    e
                )
            }
        }

        if (messageEvent.path == "/hydration_data") {

            try {

                val hydrationMl =
                    String(messageEvent.data).toInt()

                getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )
                    .edit()
                    .putInt(KEY_HYDRATION, hydrationMl)
                    .apply()

                val intent = Intent(ACTION_HYDRATION_UPDATED)
                intent.putExtra("hydration", hydrationMl)

                sendBroadcast(intent)

                Log.d(
                    "HealthyMe",
                    "Hydration received from phone: $hydrationMl ml"
                )

            } catch (e: Exception) {

                Log.e(
                    "HealthyMe",
                    "Failed to read hydration message",
                    e
                )
            }
        }
    }
}