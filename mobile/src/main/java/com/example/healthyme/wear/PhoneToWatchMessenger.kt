package com.example.healthyme.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable

class PhoneToWatchMessenger(
    private val context: Context
) {

    fun sendHydrationReminder() {

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
                        Wearable
                            .getMessageClient(context)
                            .sendMessage(
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
}