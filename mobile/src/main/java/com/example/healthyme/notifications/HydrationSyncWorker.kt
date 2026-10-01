package com.example.healthyme.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.healthyme.data.auth.FirestoreHydrationRepository
import com.example.healthyme.data.database.HealthyMeDatabase

class HydrationSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        val database =
            HealthyMeDatabase.getDatabase(applicationContext)

        val repository =
            FirestoreHydrationRepository()

        val unsyncedEvents =
            database
                .hydrationEventDao()
                .getUnsyncedEvents()

        if (unsyncedEvents.isEmpty()) {

            Log.d(
                "HealthyMe",
                "No unsynced hydration events"
            )

            return Result.success()
        }

        Log.d(
            "HealthyMe",
            "Found ${unsyncedEvents.size} unsynced hydration events"
        )

        var failed = false

        for (event in unsyncedEvents) {

            val result =
                repository.uploadHydrationEvent(event)

            result.fold(

                onSuccess = {

                    database
                        .hydrationEventDao()
                        .markAsSynced(event.id)

                    Log.d(
                        "HealthyMe",
                        "Hydration event synced: ${event.id}"
                    )
                },

                onFailure = { error ->

                    failed = true

                    Log.e(
                        "HealthyMe",
                        "Failed to sync hydration event: ${event.id}",
                        error
                    )
                }
            )
        }

        return if (failed) {
            Result.retry()
        } else {
            Result.success()
        }
    }
}