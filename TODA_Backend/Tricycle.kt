package backend

class Tricycle(
    val bodyNumber: String,
    val driver: Driver,
    val passengerCapacity: Int
) {

    var status: String = "AVAILABLE"
        private set

    var currentRideId: Int? = null
        private set

    fun dispatch(rideId: Int): Boolean {

        if (status == "WAITING") {
            status = "ON TRIP"
            currentRideId = rideId
            return true
        }

        return false
    }

    fun completeRide(rideId: Int): Boolean {

        if (currentRideId == rideId && status == "ON TRIP") {
            status = "AVAILABLE"
            currentRideId = null
            return true
        }

        return false
    }

    fun setWaiting(): Boolean {

        if (status == "AVAILABLE") {
            status = "WAITING"
            return true
        }

        return false
    }

    fun getInformation(): String {

        return "Body Number: $bodyNumber | " +
                "Driver: ${driver.name} | " +
                "Capacity: $passengerCapacity | " +
                "Status: $status"
    }
}
