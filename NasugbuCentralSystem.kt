package backend

class NasugbuCentralSystem {

    private val barangayMap = mutableMapOf<String, Barangay>()
    private val barangayList = mutableListOf<Barangay>()
    private val tricycleRegistry = mutableMapOf<String, Tricycle>()
    private val rideLogbook = mutableListOf<Ride>()

    private var nextRideId = 1

    init {
        initializeBarangays()
    }

    // ======================================
    // INITIALIZE ALL 42 BARANGAYS
    // ======================================

    private fun initializeBarangays() {

        val names = listOf(
            "Aga",
            "Balaytigue",
            "Banilad",
            "Barangay 1 (Poblacion)",
            "Barangay 2 (Poblacion)",
            "Barangay 3 (Poblacion)",
            "Barangay 4 (Poblacion)",
            "Barangay 5 (Poblacion)",
            "Barangay 6 (Poblacion)",
            "Barangay 7 (Poblacion)",
            "Barangay 8 (Poblacion)",
            "Barangay 9 (Poblacion)",
            "Barangay 10 (Poblacion)",
            "Barangay 11 (Poblacion)",
            "Barangay 12 (Poblacion)",
            "Bilaran",
            "Bucana",
            "Bulihan",
            "Bunducan",
            "Butucan",
            "Calayo",
            "Catandaan",
            "Cogunan",
            "Dayap",
            "Kaylaway",
            "Kayrilaw",
            "Latag",
            "Looc",
            "Lumbangan",
            "Malapad na Bato",
            "Mataas na Pulo",
            "Maugat",
            "Munting Indan",
            "Natipuan",
            "Pantalan",
            "Papaya",
            "Putat",
            "Reparo",
            "Talangan",
            "Tumalim",
            "Utod",
            "Wawa"
        )

        for (name in names) {

            val barangay = Barangay(name)

            barangayMap[name.lowercase()] = barangay
            barangayList.add(barangay)
        }
    }

    // ======================================
    // BARANGAY OPERATIONS
    // ======================================

    fun getAllBarangays(): List<Barangay> {
        return barangayList.toList()
    }

    fun getBarangay(name: String): Barangay? {
        return barangayMap[name.trim().lowercase()]
    }

    fun getTotalBarangays(): Int {
        return barangayMap.size
    }

    // ======================================
    // SET SERVICE ZONE FARE
    // ======================================

    fun setZoneFare(
        barangayName: String,
        zoneCode: String,
        newFare: Double
    ): Boolean {

        if (newFare <= 0) return false

        val barangay = getBarangay(barangayName) ?: return false
        val zone = barangay.terminal.getZone(zoneCode) ?: return false

        zone.baseFare = newFare

        return true
    }

    // ======================================
    // DRIVER TIME-IN
    // ======================================

    fun timeInTricycle(
        barangayName: String,
        zoneCode: String,
        bodyNumber: String,
        driverName: String,
        driverId: Int,
        passengerCapacity: Int
    ): DispatchResult {

        val barangay = getBarangay(barangayName)
            ?: return DispatchResult(false, "Barangay not found.")

        val zone = barangay.terminal.getZone(zoneCode)
            ?: return DispatchResult(false, "Service zone not found.")

        if (bodyNumber.isBlank() || driverName.isBlank()) {
            return DispatchResult(
                false,
                "Body number and driver name are required."
            )
        }

        if (driverId <= 0) {
            return DispatchResult(false, "Invalid driver ID.")
        }

        if (passengerCapacity !in 1..5) {
            return DispatchResult(
                false,
                "Passenger capacity must be between 1 and 5."
            )
        }

        val cleanBodyNumber = bodyNumber.trim()
        val bodyKey = cleanBodyNumber.lowercase()

        val existingTricycle = tricycleRegistry[bodyKey]

        if (existingTricycle != null &&
            existingTricycle.status != "AVAILABLE"
        ) {
            return DispatchResult(
                false,
                "This tricycle is already waiting or on a trip."
            )
        }

        val driver = Driver(
            driverName.trim(),
            driverId
        )

        val tricycle = Tricycle(
            cleanBodyNumber,
            driver,
            passengerCapacity
        )

        val added = zone.timeIn(tricycle)

        if (added) {

            tricycleRegistry[bodyKey] = tricycle

            return DispatchResult(
                true,
                "Tricycle #${tricycle.bodyNumber} successfully added to Zone $zoneCode queue."
            )
        }

        return DispatchResult(
            false,
            "Unable to add tricycle to the queue."
        )
    }

    // ======================================
    // GET QUEUE
    // ======================================

    fun getZoneQueue(
        barangayName: String,
        zoneCode: String
    ): List<Tricycle> {

        val barangay = getBarangay(barangayName)
            ?: return emptyList()

        return barangay.terminal.getZoneQueue(zoneCode)
    }

    // ======================================
    // PASSENGER PAYMENT AND DISPATCH
    // ======================================

    fun dispatchNext(
        barangayName: String,
        zoneCode: String,
        passengerCount: Int,
        cashReceived: Double
    ): DispatchResult {

        val barangay = getBarangay(barangayName)
            ?: return DispatchResult(false, "Barangay not found.")

        val zone = barangay.terminal.getZone(zoneCode)
            ?: return DispatchResult(false, "Service zone not found.")

        if (passengerCount < 1) {
            return DispatchResult(
                false,
                "Passenger count must be at least 1."
            )
        }

        if (zone.baseFare <= 0) {
            return DispatchResult(
                false,
                "Please configure the base fare for Zone $zoneCode first."
            )
        }

        val nextTricycle = zone.peekNext()
            ?: return DispatchResult(
                false,
                "No available tricycle in Zone $zoneCode."
            )

        if (passengerCount > nextTricycle.passengerCapacity) {
            return DispatchResult(
                false,
                "The next tricycle can only accommodate ${nextTricycle.passengerCapacity} passengers. Please adjust the passenger count."
            )
        }

        if (cashReceived < zone.baseFare) {
            return DispatchResult(
                false,
                "Insufficient payment. Required fare: PHP ${zone.baseFare}."
            )
        }

        val newRideId = nextRideId

        if (nextTricycle.status != "WAITING") {
            return DispatchResult(
                false,
                "Tricycle is not waiting in the queue."
            )
        }

        val selectedTricycle = zone.dispatchNext()
            ?: return DispatchResult(
                false,
                "Unable to dispatch tricycle."
            )

        val dispatched = selectedTricycle.dispatch(newRideId)

        if (!dispatched) {
            return DispatchResult(
                false,
                "Tricycle dispatch failed."
            )
        }

        val newRide = Ride(
            newRideId,
            barangay.barangayName,
            zone.zoneCode,
            zone.zoneName,
            passengerCount,
            selectedTricycle,
            zone.baseFare,
            cashReceived
        )

        rideLogbook.add(newRide)
        nextRideId++

        return DispatchResult(
            true,
            "Tricycle #${selectedTricycle.bodyNumber} successfully dispatched. Payment received.",
            newRide
        )
    }

    // ======================================
    // COMPLETE RIDE
    // ======================================

    fun completeRide(rideId: Int): DispatchResult {

        var selectedRide: Ride? = null

        for (ride in rideLogbook) {

            if (ride.rideId == rideId) {
                selectedRide = ride
                break
            }
        }

        if (selectedRide == null) {
            return DispatchResult(false, "Ride not found.")
        }

        val completed = selectedRide.complete()

        if (completed) {
            return DispatchResult(
                true,
                "Ride completed. Tricycle #${selectedRide.assignedTricycle.bodyNumber} is now available.",
                selectedRide
            )
        }

        return DispatchResult(
            false,
            "Ride is already completed or cannot be completed."
        )
    }

    // ======================================
    // AUTOMATED LOGBOOK OPERATIONS
    // ======================================

    fun getAllRides(): List<Ride> {
        return rideLogbook.toList()
    }

    fun getOngoingRides(): List<Ride> {

        val result = mutableListOf<Ride>()

        for (ride in rideLogbook) {

            if (ride.status == "ONGOING") {
                result.add(ride)
            }
        }

        return result
    }

    fun getCompletedRides(): List<Ride> {

        val result = mutableListOf<Ride>()

        for (ride in rideLogbook) {

            if (ride.status == "COMPLETED") {
                result.add(ride)
            }
        }

        return result
    }

    fun getTotalTrips(): Int {
        return rideLogbook.size
    }

    fun getTotalCollections(): Double {

        var total = 0.0

        for (ride in rideLogbook) {
            total += ride.fare
        }

        return total
    }
}
