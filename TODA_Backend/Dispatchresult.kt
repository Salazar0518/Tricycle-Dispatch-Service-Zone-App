package backend

import com.example.tricycledispatchandzoneapp.backend.Ride

class DispatchResult(
    val successful: Boolean,
    val message: String,
    val ride: Ride? = null
)
