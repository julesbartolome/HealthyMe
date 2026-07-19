package com.example.healthyme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.healthyme.ui.screens.DashboardScreen
import com.example.healthyme.ui.theme.HealthyMeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            HealthyMeTheme {

                DashboardScreen()

            }

        }
    }
}