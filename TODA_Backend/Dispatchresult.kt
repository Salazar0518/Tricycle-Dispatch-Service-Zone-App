package com.example.tricycledispatchandzoneapp.backend

class DispatchResult(
    val successful: Boolean,
    val message: String,
    val ride: Ride? = null
)
