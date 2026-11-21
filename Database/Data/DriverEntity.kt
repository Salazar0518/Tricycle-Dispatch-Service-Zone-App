package com.example.tricycledispatchandzoneapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drivers")
data class DriverEntity(

    @PrimaryKey
    val driverId: String,

    val name: String,
    val barangay: String,
    val bodyNumber: String,
    val zoneCode: String,
    val capacity: Int,
    val available: Boolean,

    val needsSync: Boolean = true
)