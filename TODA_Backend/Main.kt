package com.example.tricycledispatchandzoneapp

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
