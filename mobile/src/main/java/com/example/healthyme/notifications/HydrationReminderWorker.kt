package com.example.healthyme.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.healthyme.R
import com.example.healthyme.data.database.HealthyMeDatabase
import kotlinx.coroutines.runBlocking
import com.example.healthyme.wear.PhoneToWatchMessenger

class HydrationReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    private val reminderInterval =
        2 * 60 * 60 * 1000L

    override fun doWork(): Result {

        val database =
            HealthyMeDatabase.getDatabase(applicationContext)

        val latestEvent =
            runBlocking {
                database
                    .hydrationEventDao()
                    .getLatestEvent()
            }

        if (latestEvent == null) {
            return Result.success()
        }

        val currentTime = System.currentTimeMillis()

        val timeSinceLastDrink =
            currentTime - latestEvent.timestamp

        val twoHours =
            1 * 60 * 1000L

        val preferences =
            applicationContext.getSharedPreferences(
                "hydration_reminders",
                Context.MODE_PRIVATE
            )

        val lastReminderTime =
            preferences.getLong("last_reminder_time", 0L)

        val timeSinceLastReminder =
            currentTime - lastReminderTime

        if (
            timeSinceLastDrink >= twoHours &&
            timeSinceLastReminder >= reminderInterval
        ) {

            android.util.Log.d(
                "HealthyMe",
                "Sending hydration reminder"
            )

            val notification =
                NotificationCompat.Builder(
                    applicationContext,
                    "hydration_reminders"
                )
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Time to hydrate 💧")
                    .setContentText(
                        "It's been more than 2 hours since your last drink."
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

            val notificationManager =
                applicationContext.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            notificationManager.notify(
                1001,
                notification
            )

            PhoneToWatchMessenger(applicationContext)
                .sendHydrationReminder()

            preferences
                .edit()
                .putLong(
                    "last_reminder_time",
                    currentTime
                )
                .apply()
        } else {
        android.util.Log.d(
            "HealthyMe",
            "No reminder sent. " +
                    "Minutes since last reminder: " +
                    "${timeSinceLastReminder / 60000}"
        )
    }

        return Result.success()
    }
}