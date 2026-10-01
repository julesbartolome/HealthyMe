package com.example.healthyme.data.auth

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreUserRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun createUserProfile(
        userId: String,
        name: String,
        email: String
    ): Result<Unit> {

        return try {

            val user = hashMapOf(
                "name" to name,
                "email" to email,
                "role" to "REGULAR_USER",
                "status" to "ACTIVE",
                "createdAt" to System.currentTimeMillis()
            )

            firestore
                .collection("users")
                .document(userId)
                .set(user)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}