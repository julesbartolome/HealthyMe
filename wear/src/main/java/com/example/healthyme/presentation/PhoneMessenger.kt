package com.example.healthyme.presentation

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import android.util.Log

class PhoneMessenger(private val context: Context) {

    private var lastHealthSyncRequest = 0L

    fun sendHeartRate(heartRate: Int) {

        Thread {

            try {

                val nodes = Tasks.await(
                    Wearable.getNodeClient(context).connectedNodes
                )

                for (node in nodes) {

                    Wearable.getMessageClient(context)
                        .sendMessage(
                            node.id,
                            "/heart_rate",
                            heartRate.toString().toByteArray()
                        )

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }.start()

    }

    fun sendHydrationReminder() {

        val messageClient =
            Wearable.getMessageClient(context)

        Thread {
            try {
                val nodes =
                    Tasks.await(
                        Wearable
                            .getNodeClient(context)
                            .connectedNodes
                    )

                for (node in nodes) {

                    Tasks.await(
                        messageClient.sendMessage(
                            node.id,
                            "/hydration_reminder",
                            "reminder".toByteArray()
                        )
                    )

                    Log.d(
                        "HealthyMe",
                        "Hydration reminder sent to watch: ${node.displayName}"
                    )
                }

            } catch (e: Exception) {
                Log.e(
                    "HealthyMe",
                    "Failed to send hydration reminder to watch",
                    e
                )
            }
        }.start()
    }

    fun requestHealthSync() {

        val now = System.currentTimeMillis()

        if (now - lastHealthSyncRequest < 3000) {
            Log.d(
                "HealthyMe",
                "Health sync request skipped: too soon"
            )
            return
        }

        lastHealthSyncRequest = now

        Thread {
            try {

                val nodes = Tasks.await(
                    Wearable.getNodeClient(context).connectedNodes
                )

                for (node in nodes) {

                    Tasks.await(
                        Wearable.getMessageClient(context)
                            .sendMessage(
                                node.id,
                                "/request_health_sync",
                                "sync".toByteArray()
                            )
                    )

                    Log.d(
                        "HealthyMe",
                        "Requested health sync from phone"
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "HealthyMe",
                    "Failed to request health sync",
                    e
                )
            }

        }.start()
    }
}