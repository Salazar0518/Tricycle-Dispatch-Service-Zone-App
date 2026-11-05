open class TODA(
    val name: String,
    val latitude: Double,
    val longitude: Double
) {
    open fun showInfo() {
        println("TODA Name: $name")
        println("Latitude: $latitude")
        println("Longitude: $longitude")
    }
}

// Child class
class BarangayTODA(
    name: String,
    latitude: Double,
    longitude: Double
) : TODA(name, latitude, longitude)

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
        } else {
            println("Status: On Work")
        }
    }

    fun acceptRide() {
        available = false
        println("$name accepted the ride.")
    }
}

fun main() {

    // List of all 43 Barangay TODAs
    val todaList = listOf(
        BarangayTODA("Aga", 14.0900, 120.7792),
        BarangayTODA("Balaytigue", 14.1312, 120.6013),
        BarangayTODA("Balok-balok", 14.0620, 120.6480),
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

    // Driver objects
    val driver1 = Driver("Driver 1", 101, true)
    val driver2 = Driver("Driver 2", 102, true)

    println("TRICYCLE DISPATCH & SERVICE ZONE APP")
    println()

    // Display all available pickup zones
    println("AVAILABLE PICKUP ZONES:")

    for ((index, toda) in todaList.withIndex()) {
        println("${index + 1}. Zone ${index + 1} - ${toda.name} TODA")
    }

    println()
    println("Enter pickup zone (1-${todaList.size}): ")

    val zone = readLine()?.toIntOrNull()

    if (zone != null && zone in 1..todaList.size) {

        val selectedTODA = todaList[zone - 1]

        println()
        println("PICKUP ZONE INFORMATION")

        selectedTODA.showInfo()

        println()
        println("DRIVER ASSIGNMENT")

        when {
            driver1.available -> {
                driver1.acceptRide()
                println("Assigned Driver: ${driver1.name}")
            }

            driver2.available -> {
                driver2.acceptRide()
                println("Assigned Driver: ${driver2.name}")
            }

            else -> {
                println("No drivers available.")
            }
        }

    } else {
        println("Invalid zone. Please choose from 1-${todaList.size}.")
    }
}
