class Driver(
    val name: String,
    val driverId: Int,
    var available: Boolean
) {
    fun showInfo() {
        println("Driver: $name")
        println("Driver ID: $driverId")

        if (available) {
            println("Status: Available")
        }
        else {
            println("Status: On Work")
        }
    }

    fun acceptRide() {
        available = false
        println("$name accepted the ride.")
    }
}

fun main() {
    val driver1 = Driver("driver 1", 101, true)
    val driver2 = Driver("driver 2", 102, true)

    println("Tricycle Dispatch & Service Zone App")
    println()
    println("Available Pickup Zones:\n" +
            "1. Zone 1 - Barangay Aga TODA\n" +
            "2. Zone 2 - Barangay Balaytigue TODA\n" +
            "3. Zone 3 - Barangay Banilad TODA\n" +
            "4. Zone 4 - Barangay 1 TODA\n" +
            "5. Zone 5 - Barangay 2 TODA\n" +
            "6. Zone 6 - Barangay 3 TODA\n" +
            "7. Zone 7 - Barangay 4 TODA\n" +
            "8. Zone 8 - Barangay 5 TODA\n" +
            "9. Zone 9 - Barangay 6 TODA\n" +
            "10. Zone 10 - Barangay 7 TODA\n" +
            "11. Zone 11 - Barangay 8 TODA\n" +
            "12. Zone 12 - Barangay 9 TODA\n" +
            "13. Zone 13 - Barangay 10 TODA\n" +
            "14. Zone 14 - Barangay 11 TODA\n" +
            "15. Zone 15 - Barangay 12 TODA\n" +
            "16. Zone 16 - Barangay Bilaran TODA\n" +
            "17. Zone 17 - Barangay Bucana TODA\n" +
            "18. Zone 18 - Barangay Bulihan TODA\n" +
            "19. Zone 19 - Barangay Bunducan TODA\n" +
            "20. Zone 20 - Barangay Butucan TODA\n" +
            "21. Zone 21 - Barangay Calayo TODA\n" +
            "22. Zone 22 - Barangay Catandaan TODA\n" +
            "23. Zone 23 - Barangay Cogunan TODA\n" +
            "24. Zone 24 - Barangay Dayap TODA\n" +
            "25. Zone 25 - Barangay Kaylaway TODA\n" +
            "26. Zone 26 - Barangay Kayrilaw TODA\n" +
            "27. Zone 27 - Barangay Latag TODA\n" +
            "28. Zone 28 - Barangay Looc TODA\n" +
            "29. Zone 29 - Barangay Lumbangan TODA\n" +
            "30. Zone 30 - Barangay Malapad Na Bato TODA\n" +
            "31. Zone 31 - Barangay Mataas Na Pulo TODA\n" +
            "32. Zone 32 - Barangay Maugat TODA\n" +
            "33. Zone 33 - Barangay Munting Indan TODA\n" +
            "34. Zone 34 - Barangay Natipuan TODA\n" +
            "35. Zone 35 - Barangay Pantalan TODA\n" +
            "36. Zone 36 - Barangay Papaya TODA\n" +
            "37. Zone 37 - Barangay Putat TODA\n" +
            "38. Zone 38 - Barangay Reparo TODA\n" +
            "39. Zone 39 - Barangay Talangan TODA\n" +
            "40. Zone 40 - Barangay Tumalim TODA\n" +
            "41. Zone 41 - Barangay Utod TODA\n" +
            "42. Zone 42 - Barangay Wawa TODA")
    println()
    println("Enter pickup zone (1-42): ")
    val zone = readLine() ?.toIntOrNull()

    if (zone in 1..42) {
        println("Pickup Zone: Zone $zone")

        println("Driver Assignment")

        if (driver1.available) {
            driver1.acceptRide()
            println("Pickup Zone: Zone $zone")
        }
        else if (driver2.available) {
            driver2.acceptRide()
            println("Pickup Zone: Zone $zone")
        }
        else {
            println("No drivers available.")
        }
    }
    else {
        println("Invalid zone. Please choose 1-5.")
    }
}
