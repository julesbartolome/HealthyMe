package com.example.healthyme

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable

class WatchDataMessenger(
    private val context: Context
) {

    fun sendSleep(sleepValue: String) {

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
                                "/sleep_data",
                                sleepValue.toByteArray()
                            )
                    )

                    Log.d(
                        "HealthyMe",
                        "Sleep sent to watch: $sleepValue"
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "HealthyMe",
                    "Failed to send sleep to watch",
                    e
                )
            }

        }.start()
    }

    fun sendHydration(hydrationMl: Int) {

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
                                "/hydration_data",
                                hydrationMl.toString().toByteArray()
                            )
                    )

                    Log.d(
                        "HealthyMe",
                        "Hydration sent to watch: $hydrationMl ml"
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "HealthyMe",
                    "Failed to send hydration to watch",
                    e
                )
            }

        }.start()
    }
}