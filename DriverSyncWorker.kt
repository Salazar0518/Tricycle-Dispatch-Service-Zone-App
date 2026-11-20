package com.example.todaapp.worker


import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todaapp.data.TODADatabase
import com.example.todaapp.repository.DriverRepository

class DriverSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        return try {

            val database =
                TODADatabase.getDatabase(applicationContext)

            val repository =
                DriverRepository(database.driverDao())

            repository.syncDrivers()

            Result.success()

        } catch (e: Exception) {

            Result.retry()

        }
    }
}


