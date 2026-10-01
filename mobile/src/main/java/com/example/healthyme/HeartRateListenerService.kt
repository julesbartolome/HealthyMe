package com.example.healthyme

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.healthyme.data.repository.HealthRepository
import com.example.healthyme.data.database.HeartRateEntity
import com.example.healthyme.domain.ActivityContext
import com.example.healthyme.data.database.HealthyMeDatabase

class HeartRateListenerService : WearableListenerService() {

    companion object {
        private const val PREFS_NAME = "healthyme_health"
        private const val KEY_HEART_RATE = "heart_rate"
        private const val RESTING_SAMPLE_INTERVAL =
            5 * 60 * 1000L

        private const val ACTIVITY_PREFS =
            "healthyme_preferences"

        private const val KEY_ACTIVITY_CONTEXT =
            "activity_context"

        private const val KEY_LAST_RESTING_SAMPLE =
            "resting_hr_last_saved_at"

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

                val heartRate =
                    String(messageEvent.data).toInt()

                getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )
                    .edit()
                    .putInt(
                        KEY_HEART_RATE,
                        heartRate
                    )
                    .apply()

                val intent =
                    Intent(
                        "com.example.healthyme.HEART_RATE_UPDATED"
                    )

                intent.putExtra(
                    "heart_rate",
                    heartRate
                )

                sendBroadcast(intent)

                Log.d(
                    "HealthyMe",
                    "Heart rate received from watch: $heartRate BPM"
                )

                val activityPreferences =
                    getSharedPreferences(
                        ACTIVITY_PREFS,
                        Context.MODE_PRIVATE
                    )

                val currentActivity =
                    activityPreferences.getString(
                        KEY_ACTIVITY_CONTEXT,
                        ActivityContext.RESTING.name
                    )

                if (
                    currentActivity ==
                    ActivityContext.RESTING.name
                ) {

                    val now =
                        System.currentTimeMillis()

                    val lastSaved =
                        activityPreferences.getLong(
                            KEY_LAST_RESTING_SAMPLE,
                            0L
                        )

                    if (
                        now - lastSaved >=
                        RESTING_SAMPLE_INTERVAL
                    ) {

                        activityPreferences
                            .edit()
                            .putLong(
                                KEY_LAST_RESTING_SAMPLE,
                                now
                            )
                            .apply()

                        CoroutineScope(
                            Dispatchers.IO
                        ).launch {

                            try {

                                val database =
                                    HealthyMeDatabase
                                        .getDatabase(
                                            applicationContext
                                        )

                                database
                                    .heartRateDao()
                                    .insertHeartRate(
                                        HeartRateEntity(
                                            bpm = heartRate,
                                            timestamp = now
                                        )
                                    )

                                Log.d(
                                    "HealthyMe",
                                    "Resting HR sample saved: $heartRate BPM"
                                )

                                sendBroadcast(
                                    Intent(
                                        "com.example.healthyme.RESTING_HEART_RATE_SAVED"
                                    )
                                )

                            } catch (e: Exception) {

                                Log.e(
                                    "HealthyMe",
                                    "Failed to save resting HR sample",
                                    e
                                )

                                activityPreferences
                                    .edit()
                                    .putLong(
                                        KEY_LAST_RESTING_SAMPLE,
                                        0L
                                    )
                                    .apply()
                            }
                        }
                    }
                }

            } catch (e: Exception) {

                Log.e(
                    "HealthyMe",
                    "Failed to read heart rate message",
                    e
                )
            }
        }

        if (messageEvent.path == "/request_health_sync") {

            Log.d(
                "HealthyMe",
                "Watch requested health sync"
            )

            CoroutineScope(Dispatchers.IO).launch {

                try {

                    val healthRepository =
                        HealthRepository(applicationContext)

                    healthRepository.getTodayHealthData()

                    Log.d(
                        "HealthyMe",
                        "Health sync sent to watch"
                    )

                } catch (e: Exception) {

                    Log.e(
                        "HealthyMe",
                        "Failed to respond to health sync request",
                        e
                    )
                }
            }
        }
    }
}