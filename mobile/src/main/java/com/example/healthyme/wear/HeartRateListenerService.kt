package com.example.healthyme.wear

import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

object HeartRateState {
    val heartRate = mutableStateOf("--")
}

class HeartRateListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        if (messageEvent.path == "/heart_rate") {
            HeartRateState.heartRate.value =
                String(messageEvent.data)
        }
    }
}