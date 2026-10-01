package com.example.healthyme.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object SleepReminderScheduler {

    fun scheduleDaily(
        context: Context,
        hour: Int = 22,
        minute: Int = 0
    ) {

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                context,
                SleepReminderReceiver::class.java
            )

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val calendar =
            Calendar.getInstance().apply {

                set(
                    Calendar.HOUR_OF_DAY,
                    hour
                )

                set(
                    Calendar.MINUTE,
                    minute
                )

                set(
                    Calendar.SECOND,
                    0
                )

                if (
                    timeInMillis <=
                    System.currentTimeMillis()
                ) {
                    add(
                        Calendar.DAY_OF_YEAR,
                        1
                    )
                }
            }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}