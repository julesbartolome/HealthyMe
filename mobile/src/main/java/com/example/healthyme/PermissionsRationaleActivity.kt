package com.example.healthyme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class PermissionsRationaleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "HealthyMe Health Data"
                    )

                    Text(
                        text = "HealthyMe uses your health data to display " +
                                "information such as heart rate and sleep " +
                                "in your personal health dashboard."
                    )
                }
            }
        }
    }
}