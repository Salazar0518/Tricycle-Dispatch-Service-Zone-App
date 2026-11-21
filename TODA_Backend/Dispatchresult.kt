package backend

class DispatchResult(
    val successful: Boolean,
    val message: String,
    val ride: Ride? = null
)
