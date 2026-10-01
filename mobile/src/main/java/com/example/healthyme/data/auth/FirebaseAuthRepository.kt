package com.example.healthyme.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository {

    private val auth = FirebaseAuth.getInstance()

    suspend fun register(
        email: String,
        password: String
    ): Result<String> {
        return try {

            val result =
                auth.createUserWithEmailAndPassword(
                    email,
                    password
                ).await()

            Result.success(
                result.user?.uid
                    ?: throw Exception("User ID is null")
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<String> {
        return try {

            val result =
                auth.signInWithEmailAndPassword(
                    email,
                    password
                ).await()

            Result.success(
                result.user?.uid
                    ?: throw Exception("User ID is null")
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}