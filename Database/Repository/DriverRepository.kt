package com.example.tricycledispatchandzoneapp.repository

import android.util.Log
import com.example.tricycledispatchandzoneapp.data.DriverDao
import com.example.tricycledispatchandzoneapp.data.DriverEntity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DriverRepository(
    private val dao: DriverDao
) {

    private val firebase = FirebaseDatabase
        .getInstance()
        .getReference("drivers")

    // SAVE DRIVER LOCALLY
    suspend fun addDriver(driver: DriverEntity) {
        try {
            dao.insertDriver(driver.copy(needsSync = true))

            Log.d(
                "DriverRepository",
                "Driver saved locally: ${driver.driverId}"
            )

        } catch (e: Exception) {
            Log.e(
                "DriverRepository",
                "Failed to save driver locally",
                e
            )
            throw e
        }
    }

    // GET ALL DRIVERS FROM ROOM
    fun getAllDrivers() = dao.getAllDrivers()

    // UPLOAD PENDING DRIVERS TO FIREBASE
    suspend fun syncDrivers() = withContext(Dispatchers.IO) {

        val pending = dao.getPendingDrivers()

        for (driver in pending) {

            val data = mapOf(
                "driverId" to driver.driverId,
                "name" to driver.name,
                "barangay" to driver.barangay,
                "bodyNumber" to driver.bodyNumber,
                "zoneCode" to driver.zoneCode,
                "capacity" to driver.capacity,
                "available" to driver.available
            )

            try {

                firebase.child(driver.driverId)
                    .setValue(data)
                    .await()

                dao.markAsSynced(driver.driverId)

                Log.d(
                    "DriverRepository",
                    "Driver uploaded successfully: ${driver.driverId}"
                )

            } catch (e: Exception) {

                Log.e(
                    "DriverRepository",
                    "Failed to sync driver: ${driver.driverId}",
                    e
                )
            }
        }
    }
}