package backend

class Ride(
    val rideId: Int,
    val barangayName: String,
    val zoneCode: String,
    val zoneName: String,
    val passengerCount: Int,
    val assignedTricycle: Tricycle,
    val fare: Double,
    val cashReceived: Double
) {

    var status: String = "ONGOING"
        private set

    val change: Double
        get() = cashReceived - fare

    fun generateTicket(): String {

        return """
            ==========================
                 TODA DISPATCH TICKET
            ==========================
            Ride ID: $rideId
            Barangay: $barangayName
            Terminal: ${barangayName} TODA
            Tricycle: #${assignedTricycle.bodyNumber}
            Driver: ${assignedTricycle.driver.name}
            Zone: $zoneCode - $zoneName
            Passengers: $passengerCount
            Fare: PHP $fare
            Payment: PAID
            Cash Received: PHP $cashReceived
            Change: PHP $change
            Status: $status
            ==========================
        """.trimIndent()
    }

    fun complete(): Boolean {

        if (status == "ONGOING") {

            val completed = assignedTricycle.completeRide(rideId)

            if (completed) {
                status = "COMPLETED"
                return true
            }
        }

        return false
    }
}
