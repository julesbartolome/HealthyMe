package com.example.healthyme.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthyme.data.auth.FirebaseAuthRepository
import kotlinx.coroutines.launch
import com.example.healthyme.data.auth.FirestoreUserRepository

@Composable
fun FirebaseTestScreen() {

    var name by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var result by remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()

    val authRepository = remember {
        FirebaseAuthRepository()
    }

    val userRepository = remember {
        FirestoreUserRepository()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Firebase Authentication Test")

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },
            label = {
                Text("Name")
            }
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text("Email")
            }
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Password")
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = {

                scope.launch {

                    val registration =
                        authRepository.register(
                            email,
                            password
                        )

                    result =
                        registration.fold(

                            onSuccess = { uid ->

                                val profileResult =
                                    userRepository.createUserProfile(
                                        userId = uid,
                                        name = name,
                                        email = email
                                    )

                                profileResult.fold(

                                    onSuccess = {
                                        "Registration successful!\n" +
                                                "UID: $uid\n" +
                                                "Firestore profile created."
                                    },

                                    onFailure = { error ->
                                        "Account created, but profile failed:\n" +
                                                "${error.message}"
                                    }
                                )
                            },

                            onFailure = { error ->
                                "Registration failed:\n${error.message}"
                            }
                        )
                }
            }
        ) {
            Text("Register Test Account")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(result)
    }
}