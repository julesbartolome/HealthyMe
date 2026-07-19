package com.example.healthyme.data.wearable

import android.content.Context
import com.google.android.gms.wearable.Wearable

class WearService(context: Context) {

    private val dataClient = Wearable.getDataClient(context)

}