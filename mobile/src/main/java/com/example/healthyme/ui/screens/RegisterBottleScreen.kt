package com.example.healthyme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RegisterBottleScreen(
    tagId: String,
    onSave: (
        name: String,
        capacity: Int,
        amountPerScan: Int
    ) -> Unit
) {

    var bottleName by remember { mutableStateOf("") }
    var capacityText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Register Bottle",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "NFC Tag: $tagId"
        )

        OutlinedTextField(
            value = bottleName,
            onValueChange = {
                bottleName = it
            },
            label = {
                Text("Bottle Name")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = capacityText,
            onValueChange = {
                capacityText = it
            },
            label = {
                Text("Bottle Capacity (ml)")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it
            },
            label = {
                Text("Amount Per Scan (ml)")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {

                val capacity =
                    capacityText.toIntOrNull() ?: 0

                val amount =
                    amountText.toIntOrNull() ?: 0

                onSave(
                    bottleName,
                    capacity,
                    amount
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Save Bottle")
        }
    }
}