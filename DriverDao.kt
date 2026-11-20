package com.example.todaapp.data


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverEntity)

    @Query("SELECT * FROM drivers")
    fun getAllDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE needsSync = 1")
    suspend fun getPendingDrivers(): List<DriverEntity>

    @Query("""
        UPDATE drivers
        SET needsSync = 0
        WHERE driverId = :id
    """)
    suspend fun markAsSynced(id: String)
}
