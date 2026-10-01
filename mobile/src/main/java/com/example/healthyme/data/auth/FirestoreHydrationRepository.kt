package com.example.healthyme.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.healthyme.data.HydrationEventEntity
import kotlinx.coroutines.tasks.await

class FirestoreHydrationRepository {

    private val firestore =
        FirebaseFirestore.getInstance()

    private val auth =
        FirebaseAuth.getInstance()

    suspend fun uploadHydrationEvent(
        event: HydrationEventEntity
    ): Result<Unit> {

        return try {

            val userId =
                auth.currentUser?.uid
                    ?: throw Exception("User is not logged in")

            val hydrationEvent = hashMapOf(
                "userId" to userId,
                "tagId" to event.tagId,
                "amountMl" to event.amountMl,
                "timestamp" to event.timestamp
            )

            firestore
                .collection("hydration_events")
                .document(event.id.toString())
                .set(hydrationEvent)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}