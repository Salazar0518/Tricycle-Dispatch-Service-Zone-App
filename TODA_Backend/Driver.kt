package backend

class Driver(
    name: String,
    val driverId: Int
) : Person(name, driverId) {

    override val role: String = "Driver"

    fun displayDriverInformation(): String {
        return getPersonInformation()
    }
}
