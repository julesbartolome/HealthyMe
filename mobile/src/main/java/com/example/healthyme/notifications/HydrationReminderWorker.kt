package com.example.healthyme.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.healthyme.R
import com.example.healthyme.data.database.HealthyMeDatabase
import com.example.healthyme.wear.PhoneToWatchMessenger
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

class HydrationReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val MINIMUM_ADEQUATE_HYDRATION_ML = 5000

        private const val INACTIVITY_INTERVAL =
            60 * 60 * 1000L

        private const val REMINDER_INTERVAL =
            60 * 60 * 1000L
    }

    override suspend fun doWork(): Result {

        return try {

            val database =
                HealthyMeDatabase.getDatabase(applicationContext)

            val dao =
                database.hydrationEventDao()

            val currentTime =
                System.currentTimeMillis()

            val startOfDay =
                LocalDate.now()
                    .atStartOfDay(
                        ZoneId.systemDefault()
                    )
                    .toInstant()
                    .toEpochMilli()

            val endOfDay =
                LocalDate.now()
                    .plusDays(1)
                    .atStartOfDay(
                        ZoneId.systemDefault()
                    )
                    .toInstant()
                    .toEpochMilli()

            val hydrationToday =
                dao.getTotalHydrationForDay(
                    startOfDay,
                    endOfDay
                )

            val latestEvent =
                dao.getLatestEvent()

            android.util.Log.d(
                "HealthyMe",
                "Hydration reminder check: " +
                        "$hydrationToday ml today"
            )

            /*
             * Don't remind once the user has reached
             * the adequate hydration benchmark.
             */
            if (
                hydrationToday >=
                MINIMUM_ADEQUATE_HYDRATION_ML
            ) {

                android.util.Log.d(
                    "HealthyMe",
                    "No hydration reminder: " +
                            "adequate intake reached"
                )

                return Result.success()
            }

            /*
             * If there has never been a hydration event,
             * skip for now.
             *
             * We'll improve this later so HealthyMe can
             * give a morning/start-of-day reminder.
             */
            if (latestEvent == null) {

                android.util.Log.d(
                    "HealthyMe",
                    "No hydration reminder: " +
                            "no hydration events yet"
                )

                return Result.success()
            }

            val timeSinceLastDrink =
                currentTime - latestEvent.timestamp

            val preferences =
                applicationContext.getSharedPreferences(
                    "hydration_reminders",
                    Context.MODE_PRIVATE
                )

            val lastReminderTime =
                preferences.getLong(
                    "last_reminder_time",
                    0L
                )

            val timeSinceLastReminder =
                currentTime - lastReminderTime

            val shouldRemind =
                timeSinceLastDrink >= INACTIVITY_INTERVAL &&
                        timeSinceLastReminder >= REMINDER_INTERVAL

            if (shouldRemind) {

                sendReminder(
                    hydrationToday = hydrationToday
                )

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
                    "No hydration reminder. " +
                            "Minutes since drink: " +
                            "${timeSinceLastDrink / 60000}, " +
                            "minutes since reminder: " +
                            "${timeSinceLastReminder / 60000}"
                )
            }

            Result.success()

        } catch (e: Exception) {

            android.util.Log.e(
                "HealthyMe",
                "Hydration reminder check failed",
                e
            )

            Result.retry()
        }
    }

    private fun sendReminder(
        hydrationToday: Int
    ) {

        val remaining =
            (
                    MINIMUM_ADEQUATE_HYDRATION_ML -
                            hydrationToday
                    )
                .coerceAtLeast(0)

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                "hydration_reminders"
            )
                .setSmallIcon(
                    R.drawable.ic_launcher_foreground
                )
                .setContentTitle(
                    "Time for some water 💧"
                )
                .setContentText(
                    "You've had $hydrationToday ml today. " +
                            "Keep hydrating throughout the day."
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "You've had $hydrationToday ml today. " +
                                    "About $remaining ml more would bring " +
                                    "you to the adequate hydration range."
                        )
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
            1001,
            notification
        )

        PhoneToWatchMessenger(
            applicationContext
        ).sendHydrationReminder()

        android.util.Log.d(
            "HealthyMe",
            "Hydration reminder sent. " +
                    "Current intake: $hydrationToday ml"
        )
    }
}