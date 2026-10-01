package com.example.healthyme.presentation

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.healthyme.R


class WatchDataListenerService : WearableListenerService() {

    companion object {

        private const val PREFS_NAME = "healthyme_watch"
        private const val KEY_SLEEP = "sleep"
        const val ACTION_SLEEP_REMINDER =
            "com.example.healthyme.SLEEP_REMINDER"

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

        if (messageEvent.path == "/sleep_reminder") {

            try {

                val message =
                    String(messageEvent.data)

                createSleepNotificationChannel()

                val notification =
                    NotificationCompat.Builder(
                        this,
                        "sleep_reminders"
                    )
                        .setSmallIcon(
                            R.drawable.ic_launcher_foreground
                        )
                        .setContentTitle(
                            "Time to wind down 🌙"
                        )
                        .setContentText(message)
                        .setStyle(
                            NotificationCompat.BigTextStyle()
                                .bigText(message)
                        )
                        .setPriority(
                            NotificationCompat.PRIORITY_DEFAULT
                        )
                        .setAutoCancel(true)
                        .build()

                val notificationManager =
                    getSystemService(
                        Context.NOTIFICATION_SERVICE
                    ) as NotificationManager

                notificationManager.notify(
                    2001,
                    notification
                )

                Log.d(
                    "HealthyMe",
                    "Sleep reminder received from phone: $message"
                )

            } catch (e: Exception) {

                Log.e(
                    "HealthyMe",
                    "Failed to show sleep reminder",
                    e
                )
            }
        }

        if (messageEvent.path == "/movement_reminder") {

            try {

                val message =
                    String(messageEvent.data)

                createMovementNotificationChannel()

                val notification =
                    NotificationCompat.Builder(
                        this,
                        "movement_reminders"
                    )
                        .setSmallIcon(
                            R.drawable.ic_launcher_foreground
                        )
                        .setContentTitle(
                            "Time to move 🚶"
                        )
                        .setContentText(message)
                        .setStyle(
                            NotificationCompat.BigTextStyle()
                                .bigText(message)
                        )
                        .setPriority(
                            NotificationCompat.PRIORITY_DEFAULT
                        )
                        .setAutoCancel(true)
                        .build()

                val notificationManager =
                    getSystemService(
                        Context.NOTIFICATION_SERVICE
                    ) as NotificationManager

                notificationManager.notify(
                    3001,
                    notification
                )

                Log.d(
                    "HealthyMe",
                    "Movement reminder received from phone: $message"
                )

            } catch (e: Exception) {

                Log.e(
                    "HealthyMe",
                    "Failed to show movement reminder",
                    e
                )
            }
        }
    }

    private fun createSleepNotificationChannel() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    "sleep_reminders",
                    "Sleep Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description =
                        "Bedtime and sleep reminders"
                }

            val notificationManager =
                getSystemService(
                    NotificationManager::class.java
                )

            notificationManager
                .createNotificationChannel(channel)
        }
    }

    private fun createMovementNotificationChannel() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    "movement_reminders",
                    "Movement Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description =
                        "Reminders to take movement breaks"
                }

            val notificationManager =
                getSystemService(
                    NotificationManager::class.java
                )

            notificationManager
                .createNotificationChannel(channel)
        }
    }
}