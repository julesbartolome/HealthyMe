package com.example.healthyme.presentation

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable

class PhoneMessenger(
    private val context: Context
) {

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

}