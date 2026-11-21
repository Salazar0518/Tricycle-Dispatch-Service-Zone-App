
package com.example.tricycledispatchandzoneapp

// ==========================================
// 1. ADMIN CLASS
// ADMIN LOGIN AUTHENTICATION
// ==========================================

class Admin(
    private val username: String,
    private val password: String
) {

    fun login(
        enteredUsername: String,
        enteredPassword: String
    ): Boolean {

        return enteredUsername == username &&
                enteredPassword == password
    }
}

// ==========================================
// 2. ADMIN AUTHENTICATION
// ==========================================

class AdminAuthentication {

    private val admin = Admin(
        "admin",
        "admin123"
    )

    fun authenticate(
        username: String,
        password: String
    ): Boolean {

        if (username.isBlank() || password.isBlank()) {
            return false
        }

        return admin.login(username.trim(), password)
    }
}

// ==========================================
// 3. ABSTRACT PERSON CLASS
// ABSTRACTION
// ==========================================

abstract class Person(
    val name: String,
    val personId: Int
) {

    abstract val role: String

    fun getPersonInformation(): String {
        return "Name: $name | ID: $personId | Role: $role"
    }
}

// ==========================================
// 4. DRIVER CLASS
// INHERITANCE AND POLYMORPHISM
// ==========================================

class Driver(
    name: String,
    val driverId: Int
) : Person(name, driverId) {

    override val role: String = "Driver"

    fun displayDriverInformation(): String {
        return getPersonInformation()
    }
}

// ==========================================
// 5. TRICYCLE CLASS
// ENCAPSULATION
// ==========================================

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

// ==========================================
// 6. SERVICE ZONE CLASS
// FIFO QUEUE MANAGEMENT
// ==========================================

class ServiceZone(
    val zoneCode: String,
    val zoneName: String,
    val description: String,
    var baseFare: Double
) {

    private val tricycleQueue = mutableListOf<Tricycle>()

    fun timeIn(tricycle: Tricycle): Boolean {

        if (tricycle.passengerCapacity < 1) return false
        if (tricycle.status != "AVAILABLE") return false
        if (containsBodyNumber(tricycle.bodyNumber)) return false

        if (!tricycle.setWaiting()) return false

        tricycleQueue.add(tricycle)
        return true
    }

    fun containsBodyNumber(bodyNumber: String): Boolean {

        for (tricycle in tricycleQueue) {

            if (tricycle.bodyNumber.equals(
                    bodyNumber,
                    ignoreCase = true
                )
            ) {
                return true
            }
        }

        return false
    }

    fun peekNext(): Tricycle? {
        return tricycleQueue.firstOrNull()
    }

    fun dispatchNext(): Tricycle? {

        if (tricycleQueue.isEmpty()) return null

        return tricycleQueue.removeAt(0)
    }

    fun getQueue(): List<Tricycle> {
        return tricycleQueue.toList()
    }

    fun getQueueSize(): Int {
        return tricycleQueue.size
    }
}

// ==========================================
// 7. TODA TERMINAL CLASS
// ==========================================

class TodaTerminal(
    val terminalName: String
) {

    private val serviceZones = mutableMapOf<String, ServiceZone>()

    init {
        initializeServiceZones()
    }

    private fun initializeServiceZones() {

        serviceZones["A"] = ServiceZone(
            "A",
            "Poblacion Core",
            "Short distance routes",
            15.00
        )

        serviceZones["B"] = ServiceZone(
            "B",
            "Highway / Subdivision",
            "Medium distance routes",
            0.00
        )

        serviceZones["C"] = ServiceZone(
            "C",
            "Bukid / Upland Area",
            "Long distance and rough road routes",
            40.00
        )
    }

    fun getZone(zoneCode: String): ServiceZone? {
        return serviceZones[zoneCode.uppercase()]
    }

    fun getAllZones(): List<ServiceZone> {
        return serviceZones.values.toList()
    }

    fun getZoneQueue(zoneCode: String): List<Tricycle> {

        val zone = getZone(zoneCode)

        if (zone != null) {
            return zone.getQueue()
        }

        return emptyList()
    }
}

// ==========================================
// 8. BARANGAY CLASS
// COMPOSITION
// ==========================================

class Barangay(
    val barangayName: String
) {

    val terminal = TodaTerminal(
        "$barangayName TODA Terminal"
    )
}

// ==========================================
// 9. RIDE CLASS
// ==========================================

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

// ==========================================
// 10. DISPATCH RESULT CLASS
// ==========================================

class DispatchResult(
    val successful: Boolean,
    val message: String,
    val ride: Ride? = null
)

// ==========================================
// 11. NASUGBU CENTRAL SYSTEM
// MAIN BACKEND CLASS
// ==========================================

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

// ==========================================
// 12. MAIN FUNCTION
// BACKEND TESTING
// ==========================================

fun main() {

    val system = NasugbuCentralSystem()
    val authentication = AdminAuthentication()

    println("================================")
    println(" NASUGBU CENTRAL DISPATCH SYSTEM")
    println("================================")

    println("\n--- ADMIN LOGIN TEST ---")

    val login = authentication.authenticate(
        "admin",
        "admin123"
    )

    println("Admin Login Successful: $login")

    println("\nTotal Barangays: ${system.getTotalBarangays()}")

    val person: Person = Driver(
        "Sample Driver",
        100
    )

    println("\n--- PERSON INFORMATION ---")
    println(person.getPersonInformation())

    system.setZoneFare(
        "Wawa",
        "B",
        25.00
    )

    println("\n--- DRIVER TIME-IN ---")

    val timeIn1 = system.timeInTricycle(
        "Wawa",
        "C",
        "055",
        "Driver One",
        101,
        3
    )

    println(timeIn1.message)

    val timeIn2 = system.timeInTricycle(
        "Wawa",
        "C",
        "056",
        "Driver Two",
        102,
        5
    )

    println(timeIn2.message)

    println("\n--- ZONE C QUEUE ---")

    for (tricycle in system.getZoneQueue("Wawa", "C")) {
        println(tricycle.getInformation())
    }

    println("\n--- DISPATCH NEXT ---")

    val result = system.dispatchNext(
        "Wawa",
        "C",
        2,
        50.00
    )

    println(result.message)

    if (result.successful && result.ride != null) {

        val ride = result.ride

        println("\n${ride.generateTicket()}")

        println("\n--- COMPLETE RIDE ---")

        val completion = system.completeRide(ride.rideId)

        println(completion.message)
    }

    println("\n--- LOGBOOK SUMMARY ---")

    println("Total Trips: ${system.getTotalTrips()}")
    println("Ongoing Trips: ${system.getOngoingRides().size}")
    println("Completed Trips: ${system.getCompletedRides().size}")

    println(
        "Total Collections: PHP ${system.getTotalCollections()}"
    )
}
