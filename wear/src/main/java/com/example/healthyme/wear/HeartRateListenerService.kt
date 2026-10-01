package com.example.healthyme.wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.healthyme.R

class HeartRateListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {

        super.onMessageReceived(messageEvent)

        if (messageEvent.path == "/heart_rate") {

            Log.d(
                "HealthyMe",
                "Heart Rate received: ${String(messageEvent.data)} BPM"
            )
        }

        if (messageEvent.path == "/hydration_reminder") {

            Log.d(
                "HealthyMe",
                "Hydration reminder received from phone"
            )

            val notificationManager =
                getSystemService(NotificationManager::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                val channel = NotificationChannel(
                    "hydration_reminders_test",
                    "Hydration Reminders Test",
                    NotificationManager.IMPORTANCE_HIGH
                )

                notificationManager.createNotificationChannel(channel)
            }

            val notification =
                NotificationCompat.Builder(
                    this,
                    "hydration_reminders_test"
                )
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Time to hydrate 💧")
                    .setContentText(
                        "It's been more than 2 hours since your last drink."
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

            Log.d(
                "HealthyMe",
                "Showing hydration notification on watch"
            )

            notificationManager.notify(
                2001,
                notification
            )
        }
    }
}