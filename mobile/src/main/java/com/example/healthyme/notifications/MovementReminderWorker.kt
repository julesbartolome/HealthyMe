package com.example.healthyme.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.healthyme.R
import com.example.healthyme.domain.ActivityContext
import com.example.healthyme.wear.PhoneToWatchMessenger

class MovementReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {

        private const val INACTIVITY_INTERVAL =
            60 * 60 * 1000L

        private const val REMINDER_INTERVAL =
            60 * 60 * 1000L
    }

    override suspend fun doWork(): Result {

        return try {

            val preferences =
                applicationContext.getSharedPreferences(
                    "healthyme_preferences",
                    Context.MODE_PRIVATE
                )

            val activity =
                preferences.getString(
                    "activity_context",
                    ActivityContext.RESTING.name
                )

            if (
                activity !=
                ActivityContext.RESTING.name
            ) {

                android.util.Log.d(
                    "HealthyMe",
                    "No movement reminder: user is active"
                )

                return Result.success()
            }

            val currentTime =
                System.currentTimeMillis()

            val restingSince =
                preferences.getLong(
                    "activity_context_changed_at",
                    currentTime
                )

            val restingDuration =
                currentTime - restingSince

            val reminderPreferences =
                applicationContext.getSharedPreferences(
                    "movement_reminders",
                    Context.MODE_PRIVATE
                )

            val lastReminderTime =
                reminderPreferences.getLong(
                    "last_reminder_time",
                    0L
                )

            val timeSinceReminder =
                currentTime - lastReminderTime

            val shouldRemind =
                restingDuration >= INACTIVITY_INTERVAL &&
                        timeSinceReminder >= REMINDER_INTERVAL

            if (!shouldRemind) {

                android.util.Log.d(
                    "HealthyMe",
                    "No movement reminder. " +
                            "Resting for ${restingDuration / 60000} minutes"
                )

                return Result.success()
            }

            val message =
                "You've been inactive for a while. Take a short movement break."

            val notification =
                NotificationCompat.Builder(
                    applicationContext,
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
                applicationContext.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            notificationManager.notify(
                3001,
                notification
            )

            PhoneToWatchMessenger(
                applicationContext
            ).sendMovementReminder(message)

            reminderPreferences
                .edit()
                .putLong(
                    "last_reminder_time",
                    currentTime
                )
                .apply()

            android.util.Log.d(
                "HealthyMe",
                "Movement reminder sent"
            )

            Result.success()

        } catch (e: Exception) {

            android.util.Log.e(
                "HealthyMe",
                "Movement reminder failed",
                e
            )

            Result.retry()
        }
    }
}