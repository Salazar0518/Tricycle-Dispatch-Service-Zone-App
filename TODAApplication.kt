package com.example.todaapp

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.database.FirebaseDatabase
import com.example.todaapp.worker.DriverSyncWorker
import java.util.concurrent.TimeUnit

class TODAApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Enable Firebase offline persistence
        FirebaseDatabase.getInstance()
            .setPersistenceEnabled(true)

        // Only run synchronization when internet is available
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest =
            PeriodicWorkRequestBuilder<DriverSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "DriverSync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
    }
}