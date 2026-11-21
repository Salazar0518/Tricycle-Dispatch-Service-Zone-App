package com.example.tricycledispatchandzoneapp

class DispatchResult(
    val successful: Boolean,
    val message: String,
    val ride: Ride? = null
)
