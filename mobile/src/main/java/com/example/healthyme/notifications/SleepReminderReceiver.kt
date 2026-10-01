package com.example.healthyme.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.healthyme.R
import com.example.healthyme.data.repository.HealthRepository
import com.example.healthyme.domain.HealthInterpretation
import com.example.healthyme.wear.PhoneToWatchMessenger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SleepReminderReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val healthRepository =
                    HealthRepository(context)

                val healthData =
                    healthRepository.getTodayHealthData()

                val sleepMinutes =
                    parseSleepMinutes(
                        healthData.sleepHours
                    )

                val message =
                    if (sleepMinutes != null) {

                        HealthInterpretation
                            .interpretSleep(sleepMinutes)
                            .message

                    } else {

                        "Try to keep a consistent bedtime and give yourself enough time to rest tonight."
                    }

                val notification =
                    NotificationCompat.Builder(
                        context,
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
                    context.getSystemService(
                        Context.NOTIFICATION_SERVICE
                    ) as NotificationManager

                notificationManager.notify(
                    2001,
                    notification
                )

                PhoneToWatchMessenger(context)
                    .sendSleepReminder(message)

            } catch (e: Exception) {

                android.util.Log.e(
                    "HealthyMe",
                    "Failed to send sleep reminder",
                    e
                )
            }
        }
    }

    private fun parseSleepMinutes(
        sleepHours: String
    ): Long? {

        if (sleepHours == "--") {
            return null
        }

        val parts =
            sleepHours.split(":")

        if (parts.size != 2) {
            return null
        }

        val hours =
            parts[0].toLongOrNull()
                ?: return null

        val minutes =
            parts[1].toLongOrNull()
                ?: return null

        return (hours * 60) + minutes
    }
}