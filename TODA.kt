// DRIVER CLASS
class Driver(
    val name: String,
    val driverId: Int,
    val passengerCapacity: Int
) {
    var available: Boolean = true
    var currentRideId: Int? = null

    fun acceptRide(rideId: Int): Boolean {
        if (available) {
            available = false
            currentRideId = rideId
            return true
        } else {
            return false
        }
    }

    fun completeRide(rideId: Int): Boolean {
        if (currentRideId == rideId) {
            available = true
            currentRideId = null
            return true
        } else {
            return false
        }
    }
}


// PARENT CLASS
open class TODA(
    val name: String,
    val latitude: Double,
    val longitude: Double
) {
    val drivers = mutableListOf<Driver>()

    open fun getTODAName(): String {
        return name
    }
}


// CHILD CLASS
class BarangayTODA(
    name: String,
    latitude: Double,
    longitude: Double
) : TODA(name, latitude, longitude)


// RIDE CLASS
class Ride(
    val rideId: Int,
    val pickupZone: String,
    val latitude: Double,
    val longitude: Double,
    val passengerCount: Int,
    val assignedDriver: Driver
) {
    var status: String = "ONGOING"

    fun complete(): Boolean {
        if (status == "ONGOING") {
            val completed = assignedDriver.completeRide(rideId)

            if (completed) {
                status = "COMPLETED"
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }
}


// DISPATCH RESULT CLASS
class DispatchResult(
    val successful: Boolean,
    val message: String,
    val ride: Ride? = null
)


// BACKEND SERVICE CLASS
class DispatchService {

    private val todaList = mutableListOf<TODA>()
    private val rideList = mutableListOf<Ride>()

    private var nextRideId = 1

    init {
        initializeTODAs()
        initializeDrivers()
    }


    // INITIALIZE ALL BARANGAY TODAs
    private fun initializeTODAs() {

        todaList.addAll(
            listOf(
                BarangayTODA("Aga", 14.0900, 120.7792),
                BarangayTODA("Balaytigue", 14.1312, 120.6013),
                BarangayTODA("Banilad", 14.0411, 120.7391),
                BarangayTODA("Barangay 1 (Poblacion)", 14.0754, 120.6325),
                BarangayTODA("Barangay 2 (Poblacion)", 14.0735, 120.6310),
                BarangayTODA("Barangay 3 (Poblacion)", 14.0740, 120.6342),
                BarangayTODA("Barangay 4 (Poblacion)", 14.0718, 120.6335),
                BarangayTODA("Barangay 5 (Poblacion)", 14.0760, 120.6300),
                BarangayTODA("Barangay 6 (Poblacion)", 14.0748, 120.6360),
                BarangayTODA("Barangay 7 (Poblacion)", 14.0725, 120.6371),
                BarangayTODA("Barangay 8 (Poblacion)", 14.0705, 120.6358),
                BarangayTODA("Barangay 9 (Poblacion)", 14.0690, 120.6340),
                BarangayTODA("Barangay 10 (Poblacion)", 14.0712, 120.6295),
                BarangayTODA("Barangay 11 (Poblacion)", 14.0681, 120.6280),
                BarangayTODA("Barangay 12 (Poblacion)", 14.0665, 120.6245),
                BarangayTODA("Bilaran", 14.0617, 120.6656),
                BarangayTODA("Bucana", 14.0864, 120.6272),
                BarangayTODA("Bulihan", 14.1293, 120.6720),
                BarangayTODA("Bunducan", 14.1030, 120.6690),
                BarangayTODA("Butucan", 14.1295, 120.6310),
                BarangayTODA("Calayo", 14.1461, 120.6141),
                BarangayTODA("Catandaan", 14.0322, 120.6974),
                BarangayTODA("Cogunan", 14.0531, 120.7512),
                BarangayTODA("Dayap", 14.0970, 120.6980),
                BarangayTODA("Kaylaway", 14.0610, 120.8250),
                BarangayTODA("Kayrilaw", 14.1040, 120.7240),
                BarangayTODA("Latag", 14.1160, 120.6120),
                BarangayTODA("Looc", 14.1610, 120.6130),
                BarangayTODA("Lumbangan", 14.0583, 120.7011),
                BarangayTODA("Malapad na Bato", 14.1027, 120.6521),
                BarangayTODA("Mataas na Pulo", 14.1070, 120.6400),
                BarangayTODA("Maugat", 14.0870, 120.7250),
                BarangayTODA("Munting Indan", 14.0910, 120.7100),
                BarangayTODA("Natipuan", 14.1141, 120.5982),
                BarangayTODA("Pantalan", 14.0833, 120.6417),
                BarangayTODA("Papaya", 14.1950, 120.6080),
                BarangayTODA("Putat", 14.0667, 120.6667),
                BarangayTODA("Reparo", 14.0720, 120.6860),
                BarangayTODA("Talangan", 14.0430, 120.6810),
                BarangayTODA("Tumalim", 14.0520, 120.6550),
                BarangayTODA("Utod", 14.1130, 120.6270),
                BarangayTODA("Wawa", 14.0824, 120.6244)
            )
        )
    }


    // GIVE EVERY TODA ITS OWN TWO DRIVERS
    private fun initializeDrivers() {

        var driverId = 1

        for (toda in todaList) {

            toda.drivers.add(
                Driver("${toda.name} Driver 1", driverId, 3)
            )

            driverId++

            toda.drivers.add(
                Driver("${toda.name} Driver 2", driverId, 5)
            )

            driverId++
        }
    }


    // GET ALL TODA ZONES
    fun getAllTODAs(): List<TODA> {
        return todaList
    }


    // GET ONE TODA
    fun getTODA(name: String): TODA? {

        for (toda in todaList) {
            if (toda.name.equals(name, ignoreCase = true)) {
                return toda
            }
        }

        return null
    }


    // GET DRIVERS OF A SELECTED TODA
    fun getDrivers(todaName: String): List<Driver> {

        val selectedTODA = getTODA(todaName)

        if (selectedTODA != null) {
            return selectedTODA.drivers
        } else {
            return emptyList()
        }
    }


    // BOOK A RIDE AND ASSIGN A DRIVER
    fun requestRide(
        todaName: String,
        passengerCount: Int
    ): DispatchResult {

        if (passengerCount < 1 || passengerCount > 5) {
            return DispatchResult(
                false,
                "Passenger count must be between 1 and 5."
            )
        }

        val selectedTODA = getTODA(todaName)

        if (selectedTODA == null) {
            return DispatchResult(
                false,
                "TODA zone not found."
            )
        }

        var selectedDriver: Driver? = null

        for (driver in selectedTODA.drivers) {

            if (driver.available &&
                passengerCount <= driver.passengerCapacity) {

                selectedDriver = driver
                break
            }
        }

        if (selectedDriver == null) {
            return DispatchResult(
                false,
                "No available driver has enough passenger seats in ${selectedTODA.name} TODA."
            )
        }

        val newRide = Ride(
            nextRideId,
            selectedTODA.name,
            selectedTODA.latitude,
            selectedTODA.longitude,
            passengerCount,
            selectedDriver
        )

        val accepted = selectedDriver.acceptRide(nextRideId)

        if (accepted) {

            rideList.add(newRide)
            nextRideId++

            return DispatchResult(
                true,
                "Ride successfully assigned to ${selectedDriver.name}.",
                newRide
            )

        } else {

            return DispatchResult(
                false,
                "Driver is no longer available."
            )
        }
    }


    // COMPLETE A RIDE
    fun completeRide(rideId: Int): DispatchResult {

        var selectedRide: Ride? = null

        for (ride in rideList) {

            if (ride.rideId == rideId) {
                selectedRide = ride
                break
            }
        }

        if (selectedRide == null) {
            return DispatchResult(
                false,
                "Ride not found."
            )
        }

        val completed = selectedRide.complete()

        if (completed) {

            return DispatchResult(
                true,
                "Ride $rideId completed. ${selectedRide.assignedDriver.name} is now available.",
                selectedRide
            )

        } else {

            return DispatchResult(
                false,
                "This ride is already completed or could not be completed."
            )
        }
    }


    // GET ALL RIDES
    fun getAllRides(): List<Ride> {
        return rideList
    }


    // GET ONGOING RIDES
    fun getOngoingRides(): List<Ride> {

        val ongoingRides = mutableListOf<Ride>()

        for (ride in rideList) {

            if (ride.status == "ONGOING") {
                ongoingRides.add(ride)
            }
        }

        return ongoingRides
    }


    // GET COMPLETED RIDES
    fun getCompletedRides(): List<Ride> {

        val completedRides = mutableListOf<Ride>()

        for (ride in rideList) {

            if (ride.status == "COMPLETED") {
                completedRides.add(ride)
            }
        }

        return completedRides
    }
}


// MAIN FUNCTION - FOR TESTING THE BACKEND
fun main() {

    val dispatchService = DispatchService()

    // CHECK THE NUMBER OF TODA ZONES
    println("Total TODA Zones: ${dispatchService.getAllTODAs().size}")


    // CHECK THE DRIVERS IN WAWA
    val drivers = dispatchService.getDrivers("Wawa")

    println("\nWawa Drivers:")

    for (driver in drivers) {
        println("Name: ${driver.name}")
        println("Driver ID: ${driver.driverId}")
        println("Passenger Capacity: ${driver.passengerCapacity}")
        println("Available: ${driver.available}")
    }


    // TEST RIDE REQUEST
    val result = dispatchService.requestRide("Wawa", 2)

    println("\nRide Request:")
    println(result.message)


    // TEST RIDE COMPLETION
    if (result.successful) {

        val rideId = result.ride!!.rideId

        val completion = dispatchService.completeRide(rideId)

        println("\nRide Completion:")
        println(completion.message)
    }
}
